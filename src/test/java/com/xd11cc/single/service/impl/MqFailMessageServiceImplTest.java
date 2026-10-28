package com.xd11cc.single.service.impl;

import com.xd11cc.single.common.exception.ServiceException;
import com.xd11cc.single.entity.domain.MqFailMessageDO;
import com.xd11cc.single.enums.MqFailStatus;
import com.xd11cc.single.enums.SystemErrorEnum;
import com.xd11cc.single.mapper.MqFailMessageMapper;
import com.xd11cc.single.util.BaseUnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

/**
 * {@link MqFailMessageServiceImpl} 的状态校验测试。
 * <p>
 * 重点覆盖「重投只允许待处理状态」这条不变式。
 * status 由 {@code Integer} 改为 {@code String} 后，此处的比较若写成
 * {@code !=} 会退化为引用比较、条件恒真，导致所有记录都无法重投。
 * 该测试就是为了钉死这一点 —— 改动前这个服务是零测试。
 * </p>
 *
 * @author xd11cc
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MqFailMessageServiceImplTest extends BaseUnitTest {

    @Mock
    private MqFailMessageMapper baseMapper;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private MqFailMessageServiceImpl mqFailMessageService;

    @Test
    @DisplayName("replay：已重放状态不允许再次重投")
    void replay_replayedStatusRejected() {
        MqFailMessageDO record = new MqFailMessageDO();
        record.setId(1L);
        record.setStatus(MqFailStatus.REPLAYED.getCode());
        given(baseMapper.selectById(1L)).willReturn(record);

        assertThatThrownBy(() -> mqFailMessageService.replay(1L))
                .isInstanceOf(ServiceException.class)
                .satisfies(e -> assertThat(((ServiceException) e).getErrorCode().getErrorCode())
                        .isEqualTo(SystemErrorEnum.MQ_FAIL_REPLAY_NOT_ALLOWED.getErrorCode()));
        // 状态不合法时绝不能发出消息
        then(rabbitTemplate).should(never()).send(anyString(), anyString(), any());
    }

    @Test
    @DisplayName("replay：已忽略状态不允许重投")
    void replay_ignoredStatusRejected() {
        MqFailMessageDO record = new MqFailMessageDO();
        record.setId(2L);
        record.setStatus(MqFailStatus.IGNORED.getCode());
        given(baseMapper.selectById(2L)).willReturn(record);

        assertThatThrownBy(() -> mqFailMessageService.replay(2L))
                .isInstanceOf(ServiceException.class)
                .satisfies(e -> assertThat(((ServiceException) e).getErrorCode().getErrorCode())
                        .isEqualTo(SystemErrorEnum.MQ_FAIL_REPLAY_NOT_ALLOWED.getErrorCode()));
    }

    @Test
    @DisplayName("replay：记录不存在时抛 MQ_FAIL_RECORD_NOT_FOUND")
    void replay_recordNotFound() {
        given(baseMapper.selectById(99L)).willReturn(null);

        assertThatThrownBy(() -> mqFailMessageService.replay(99L))
                .isInstanceOf(ServiceException.class)
                .satisfies(e -> assertThat(((ServiceException) e).getErrorCode().getErrorCode())
                        .isEqualTo(SystemErrorEnum.MQ_FAIL_RECORD_NOT_FOUND.getErrorCode()));
    }
}
