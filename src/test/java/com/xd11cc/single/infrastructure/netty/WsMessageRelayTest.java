package com.xd11cc.single.infrastructure.netty;

import com.xd11cc.single.entity.domain.SystemUserDO;
import com.xd11cc.single.entity.dto.LoginUserDTO;
import com.xd11cc.single.entity.dto.WsPushMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

/**
 * {@link WsMessageRelay} 的测试。
 * <p>
 * 这一层不需要通道、不需要 EmbeddedChannel：直接构造发送者调 {@code relay}，
 * 断言「信封里是谁」和「用哪个租户去推」。这正是把转发逻辑从 handler 抽出来的收益。
 * </p>
 *
 * @author xd11cc
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class WsMessageRelayTest {

    @Mock
    private ChannelManager channelManager;

    @Mock
    private WsRateLimiter wsRateLimiter;

    @Test
    @DisplayName("转发：信封注入连接上的真实身份，并按发送方租户推送")
    void relay_injectsSenderAndTenant() {
        given(wsRateLimiter.tryAcquire(anyLong(), anyLong())).willReturn(true);
        WsMessageRelay relay = new WsMessageRelay(channelManager, wsRateLimiter);
        given(channelManager.pushToUser(anyLong(), anyLong(), any())).willReturn(true);

        boolean sent = relay.relay(loginUser(1L, 10L), 2L, "payload");

        ArgumentCaptor<WsPushMessage> captor = ArgumentCaptor.forClass(WsPushMessage.class);
        then(channelManager).should().pushToUser(org.mockito.ArgumentMatchers.eq(2L),
                org.mockito.ArgumentMatchers.eq(10L), captor.capture());

        WsPushMessage envelope = captor.getValue();
        assertThat(sent).isTrue();
        assertThat(envelope.getAction()).isEqualTo("BUSINESS");
        assertThat(envelope.getFrom()).isNotNull();
        assertThat(envelope.getFrom().getUserId()).isEqualTo(1L);
        assertThat(envelope.getFrom().getUsername()).isEqualTo("admin");
        assertThat(envelope.getData()).isEqualTo("payload");
    }

    @Test
    @DisplayName("被限流：不投递，也不触碰 ChannelManager")
    void relay_rateLimited_dropsBeforeDispatch() {
        given(wsRateLimiter.tryAcquire(10L, 1L)).willReturn(false);
        WsMessageRelay relay = new WsMessageRelay(channelManager, wsRateLimiter);

        boolean sent = relay.relay(loginUser(1L, 10L), 2L, "payload");

        assertThat(sent).isFalse();
        then(channelManager).should(never()).pushToUser(anyLong(), anyLong(), any());
    }

    @Test
    @DisplayName("发送者缺失：不推送")
    void relay_nullSender_returnsFalse() {
        WsMessageRelay relay = new WsMessageRelay(channelManager, wsRateLimiter);

        assertThat(relay.relay(null, 2L, "payload")).isFalse();
        then(channelManager).should(never()).pushToUser(anyLong(), anyLong(), any());
    }

    @Test
    @DisplayName("目标用户为空：不推送")
    void relay_nullTarget_returnsFalse() {
        WsMessageRelay relay = new WsMessageRelay(channelManager, wsRateLimiter);

        assertThat(relay.relay(loginUser(1L, 10L), null, "payload")).isFalse();
        then(channelManager).should(never()).pushToUser(anyLong(), anyLong(), any());
    }

    @Test
    @DisplayName("systemUserDO 缺失：不推送，避免取租户时 NPE")
    void relay_missingSystemUser_returnsFalse() {
        WsMessageRelay relay = new WsMessageRelay(channelManager, wsRateLimiter);
        LoginUserDTO broken = new LoginUserDTO();
        broken.setUserId(1L);

        assertThat(relay.relay(broken, 2L, "payload")).isFalse();
        then(channelManager).should(never()).pushToUser(anyLong(), anyLong(), any());
    }

    private static LoginUserDTO loginUser(Long userId, Long tenantId) {
        LoginUserDTO dto = new LoginUserDTO();
        dto.setUserId(userId);
        SystemUserDO userDO = new SystemUserDO();
        userDO.setId(userId);
        userDO.setUsername("admin");
        userDO.setTenantId(tenantId);
        dto.setSystemUserDO(userDO);
        return dto;
    }
}
