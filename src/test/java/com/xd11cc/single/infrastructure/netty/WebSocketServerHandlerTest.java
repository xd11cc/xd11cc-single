package com.xd11cc.single.infrastructure.netty;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.xd11cc.single.common.properties.NettyProperties;
import com.xd11cc.single.entity.domain.SystemUserDO;
import com.xd11cc.single.entity.dto.LoginUserDTO;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

/**
 * {@link WebSocketServerHandler} 的传输层测试。
 * <p>
 * 本类只关心「帧怎么解析、什么时候回错误帧、把什么交给 relay」。
 * 发送者身份注入与租户规则分别在 {@link WsMessageRelayTest} 与 {@link ChannelManagerTest} 覆盖，
 * 每层只测自己的接缝。
 * </p>
 *
 * @author xd11cc
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class WebSocketServerHandlerTest {

    private static final Long SENDER_ID = 1L;
    private static final Long TENANT_ID = 10L;

    @Mock
    private WsMessageRelay wsMessageRelay;

    @Mock
    private ChannelManager channelManager;

    @Mock
    private NettyProperties nettyProperties;

    @Mock
    private WsFrameGuard wsFrameGuard;

    @InjectMocks
    private WebSocketServerHandler handler;

    private EmbeddedChannel channel;

    @BeforeEach
    void setUp() {
        given(nettyProperties.getReadIdleCloseCount()).willReturn(3);
        given(wsFrameGuard.allow(any())).willReturn(true);
        channel = new EmbeddedChannel(handler);
        channel.attr(ChannelManager.LOGIN_USER).set(loginUser(SENDER_ID, TENANT_ID));
    }

    @AfterEach
    void tearDown() {
        channel.finishAndReleaseAll();
    }

    @Test
    @DisplayName("正常报文：解析后交给 relay，不做任何广播")
    void normalMessage_delegatesToRelay() {
        channel.writeInbound(new TextWebSocketFrame(
                "{\"userId\":2,\"data\":{\"type\":\"CHAT\",\"text\":\"hi\"}}"));

        ArgumentCaptor<Object> payload = ArgumentCaptor.forClass(Object.class);
        then(wsMessageRelay).should().relay(any(LoginUserDTO.class), eq(2L), payload.capture());
        assertThat(payload.getValue()).isInstanceOf(java.util.Map.class);
        assertThat(((java.util.Map<?, ?>) payload.getValue()).get("text")).isEqualTo("hi");
    }

    @Test
    @DisplayName("旧 TENANT 报文（无 userId）被拒，不产生任何广播")
    void legacyTenantFrame_rejected() {
        channel.writeInbound(new TextWebSocketFrame(
                "{\"webSocketEnum\":\"TENANT\",\"tenantId\":999,\"data\":{}}"));

        then(wsMessageRelay).should(never()).relay(any(), anyLong(), any());
        then(channelManager).should(never()).broadcastToTenant(anyLong(), any());
        assertThat(readError()).contains("缺少目标用户");
    }

    @Test
    @DisplayName("旧 GLOBAL 报文（无 userId）被拒")
    void legacyGlobalFrame_rejected() {
        channel.writeInbound(new TextWebSocketFrame(
                "{\"webSocketEnum\":\"GLOBAL\",\"data\":{}}"));

        then(wsMessageRelay).should(never()).relay(any(), anyLong(), any());
        assertThat(readError()).contains("缺少目标用户");
    }

    @Test
    @DisplayName("非法 JSON：回 ERROR 信封，不是裸文本")
    void invalidJson_returnsErrorEnvelope() {
        channel.writeInbound(new TextWebSocketFrame("{bad json"));

        String raw = readRawOutbound();
        assertThat(raw).startsWith("{");
        JSONObject json = JSON.parseObject(raw);
        assertThat(json.getString("action")).isEqualTo("ERROR");
        assertThat(json.getJSONObject("data").getString("message")).isNotBlank();
    }

    @Test
    @DisplayName("未认证连接：回 ERROR 信封且不转发")
    void unauthenticated_rejected() {
        channel.attr(ChannelManager.LOGIN_USER).set(null);
        channel.writeInbound(new TextWebSocketFrame("{\"userId\":2,\"data\":{}}"));

        then(wsMessageRelay).should(never()).relay(any(), anyLong(), any());
        assertThat(readError()).contains("未认证");
    }

    // ==================== 心跳与空闲 ====================

    @Test
    @DisplayName("心跳：收到 PING 回 PONG，且不触发转发")
    void pingRepliesPong() {
        channel.writeInbound(new TextWebSocketFrame("PING"));

        TextWebSocketFrame out = channel.readOutbound();
        assertThat(out.text()).isEqualTo("PONG");
        then(wsMessageRelay).should(never()).relay(any(), anyLong(), any());
    }

    @Test
    @DisplayName("心跳：收到 PONG 不回包")
    void pongIsIgnored() {
        channel.writeInbound(new TextWebSocketFrame("PONG"));

        assertThat((Object) channel.readOutbound()).isNull();
    }

    @Test
    @DisplayName("读空闲：未达阈值不断开，达到阈值后关闭")
    void readerIdle_closesAfterThreshold() {
        // 前两次不关
        channel.pipeline().fireUserEventTriggered(new TestIdleEvent(IdleState.READER_IDLE));
        channel.pipeline().fireUserEventTriggered(new TestIdleEvent(IdleState.READER_IDLE));
        assertThat(channel.isActive()).isTrue();

        // 第三次达到阈值（配置为 3）后关闭
        channel.pipeline().fireUserEventTriggered(new TestIdleEvent(IdleState.READER_IDLE));
        assertThat(channel.isActive()).isFalse();
    }

    @Test
    @DisplayName("写空闲：主动发送 PING 探活")
    void writerIdle_sendsPing() {
        channel.pipeline().fireUserEventTriggered(new TestIdleEvent(IdleState.WRITER_IDLE));

        TextWebSocketFrame out = channel.readOutbound();
        assertThat(out.text()).isEqualTo("PING");
    }

    // ==================== 握手完成后注册 ====================

    @Test
    @DisplayName("握手完成：注册通道")
    void handshakeComplete_registersChannel() {
        given(channelManager.addChannel(any(), any())).willReturn(true);

        channel.pipeline().fireUserEventTriggered(handshakeComplete());

        then(channelManager).should().addChannel(any(), any(LoginUserDTO.class));
        assertThat(channel.isActive()).isTrue();
    }

    @Test
    @DisplayName("握手完成但注册失败：关闭连接，避免「已连接但收不到推送」")
    void handshakeComplete_registerFails_closesChannel() {
        given(channelManager.addChannel(any(), any())).willReturn(false);

        channel.pipeline().fireUserEventTriggered(handshakeComplete());

        assertThat(channel.isActive()).isFalse();
    }

    @Test
    @DisplayName("握手完成但缺少登录用户：关闭连接")
    void handshakeComplete_withoutLoginUser_closesChannel() {
        channel.attr(ChannelManager.LOGIN_USER).set(null);

        channel.pipeline().fireUserEventTriggered(handshakeComplete());

        then(channelManager).should(never()).addChannel(any(), any());
        assertThat(channel.isActive()).isFalse();
    }

    /**
     * IdleStateEvent 的构造器是 protected，测试里用子类实例化
     */
    private static class TestIdleEvent extends IdleStateEvent {
        TestIdleEvent(IdleState state) {
            super(state, true);
        }
    }

    private static WebSocketServerProtocolHandler.HandshakeComplete handshakeComplete() {
        return new WebSocketServerProtocolHandler.HandshakeComplete(
                "/ws", new io.netty.handler.codec.http.DefaultHttpHeaders(), null);
    }

    // ==================== 连接总闸 ====================

    @Test
    @DisplayName("帧速率超限：直接断开，不回 PONG 也不转发")
    void frameGuardExceeded_closesChannel() {
        given(wsFrameGuard.allow(any())).willReturn(false);

        channel.writeInbound(new TextWebSocketFrame("PING"));

        assertThat(channel.isActive()).isFalse();
        assertThat((Object) channel.readOutbound()).isNull();
        then(wsMessageRelay).should(never()).relay(any(), anyLong(), any());
    }

    @Test
    @DisplayName("非法帧不重置读空闲，防止靠垃圾帧保活")
    void malformedFrame_doesNotResetReadIdle() {
        channel.writeInbound(new TextWebSocketFrame("{bad json"));

        assertThat(readIdleCount()).as("垃圾帧不应刷新读空闲计数").isNull();
    }

    @Test
    @DisplayName("合法业务帧才重置读空闲")
    void validFrame_resetsReadIdle() {
        channel.writeInbound(new TextWebSocketFrame("{\"userId\":2,\"data\":{}}"));

        assertThat(readIdleCount()).isEqualTo(0);
    }

    /**
     * AttributeKey.valueOf 按名字返回同一实例，因此可以只读地观察 handler 的私有计数
     */
    private Integer readIdleCount() {
        return channel.attr(io.netty.util.AttributeKey.<Integer>valueOf("READ_IDLE_COUNT")).get();
    }

    // ==================== 辅助 ====================

    private String readError() {
        JSONObject json = JSON.parseObject(readRawOutbound());
        assertThat(json.getString("action")).isEqualTo("ERROR");
        return json.getJSONObject("data").getString("message");
    }

    private String readRawOutbound() {
        TextWebSocketFrame frame = channel.readOutbound();
        assertThat(frame).as("应有一条出站帧").isNotNull();
        return frame.text();
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
