package com.xd11cc.single.service.impl;

import com.xd11cc.single.entity.domain.SystemOperateLogDO;
import com.xd11cc.single.entity.vo.SystemOperateLogQueryVO;
import com.xd11cc.single.mapper.SystemOperateLogMapper;
import com.xd11cc.single.service.ISystemOperateLogService;
import com.xd11cc.single.util.BaseUnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class SystemOperateLogServiceImplTest extends BaseUnitTest {

    @Mock
    private SystemOperateLogMapper baseMapper;

    @InjectMocks
    private SystemOperateLogServiceImpl operateLogService;

    // ==================== saveLog ====================

    @Test
    void saveLog_写入操作日志() {
        SystemOperateLogDO log = buildLog(1L, "system:user:add", "新增用户", "0", null, null);
        given(baseMapper.insert(any(SystemOperateLogDO.class))).willReturn(1);

        operateLogService.saveLog(log);

        then(baseMapper).should().insert(log);
    }

    // ==================== getList ====================

    @Test
    void getList_带模块和类型筛选_拼接条件() {
        SystemOperateLogQueryVO vo = new SystemOperateLogQueryVO();
        vo.setModule("system");
        vo.setOperateType("add");
        vo.setStatus("0");
        SystemOperateLogDO expected = buildLog(1L, "system:user:add", "新增用户", "0", null, null);
        given(baseMapper.selectList(any())).willReturn(Collections.singletonList(expected));

        List<SystemOperateLogDO> result = operateLogService.getList(vo);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getModule()).isEqualTo("system");
        assertThat(result.get(0).getOperateType()).isEqualTo("add");
    }

    @Test
    void getList_带时间范围_拼接起止时间() {
        SystemOperateLogQueryVO vo = new SystemOperateLogQueryVO();
        vo.setBeginTime(new Date(946684800000L));
        vo.setEndTime(new Date());
        given(baseMapper.selectList(any())).willReturn(Collections.emptyList());

        List<SystemOperateLogDO> result = operateLogService.getList(vo);

        assertThat(result).isEmpty();
        then(baseMapper).should().selectList(any());
    }

    @Test
    void getList_空筛选_返回全部() {
        SystemOperateLogQueryVO vo = new SystemOperateLogQueryVO();
        SystemOperateLogDO l1 = buildLog(1L, "sys:user:add", "新增用户", "0", null, null);
        SystemOperateLogDO l2 = buildLog(2L, "sys:role:add", "新增角色", "0", null, null);
        given(baseMapper.selectList(any())).willReturn(Arrays.asList(l1, l2));

        List<SystemOperateLogDO> result = operateLogService.getList(vo);

        assertThat(result).hasSize(2);
    }

    // ==================== deleteByIds ====================

    @Test
    void deleteByIds_返回删除行数() {
        given(baseMapper.deleteBatchIds(Arrays.asList(1L, 2L))).willReturn(2);

        int row = operateLogService.deleteByIds(Arrays.asList(1L, 2L));

        assertThat(row).isEqualTo(2);
        then(baseMapper).should().deleteBatchIds(Arrays.asList(1L, 2L));
    }

    // ==================== clean ====================

    @Test
    void clean_清空所有操作日志() {
        operateLogService.clean();

        then(baseMapper).should().delete(any());
    }

    // ==================== 辅助方法 ====================

    private static SystemOperateLogDO buildLog(Long id, String operateId, String operateName,
                                               String status, Date beginTime, Date endTime) {
        SystemOperateLogDO log = new SystemOperateLogDO();
        log.setId(id);
        log.setModule("system");
        log.setOperateType("add");
        log.setOperateDesc(operateName);
        log.setMethod(operateId);
        log.setRequestMethod("POST");
        log.setRequestUrl("/system/user");
        log.setRequestParam("{}");
        log.setStatus(status);
        log.setOperateIp("127.0.0.1");
        return log;
    }
}
