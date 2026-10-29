package com.xd11cc.single.infrastructure.netty;

import com.xd11cc.single.common.properties.NettyProperties;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link WsFrameGuard} 的测试。
 * <p>
 * 守卫的价值在于覆盖「按发送者限流管不到」的帧：心跳、非法帧、缺字段帧。
 * 它按窗口计数，超限让调用方断开连接。
 * </p>
 *
 * @author xd11cc
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class WsFrameGuardTest {

    private NettyProperties properties;

    private WsFrameGuard guard;

    private EmbeddedChannel channel;

    @BeforeEach
    void setUp() {
        properties = new NettyProperties();
        properties.getFrameLimit().setEnabled(true);
        properties.getFrameLimit().setCount(3);
        properties.getFrameLimit().setIntervalSecond(10);

        guard = new WsFrameGuard(properties);
        channel = new EmbeddedChannel();
    }

    @Test
    @DisplayName("窗口内未超限：逐帧放行")
    void allow_withinLimit() {
        assertThat(guard.allow(channel)).isTrue();
        assertThat(guard.allow(channel)).isTrue();
        assertThat(guard.allow(channel)).isTrue();
    }

    @Test
    @DisplayName("超过窗口额度：拒绝")
    void allow_overLimit() {
        for (int i = 0; i < 3; i++) {
            assertThat(guard.allow(channel)).isTrue();
        }

        assertThat(guard.allow(channel)).isFalse();
    }

    @Test
    @DisplayName("窗口滚动后重新计数")
    void allow_windowRollover() throws Exception {
        properties.getFrameLimit().setCount(1);
        properties.getFrameLimit().setIntervalSecond(1);
        guard = new WsFrameGuard(properties);

        assertThat(guard.allow(channel)).isTrue();
        assertThat(guard.allow(channel)).isFalse();

        Thread.sleep(1100);
        assertThat(guard.allow(channel)).as("新窗口应重新可用").isTrue();
    }

    @Test
    @DisplayName("不同连接互不影响")
    void allow_perChannelIsolated() {
        EmbeddedChannel other = new EmbeddedChannel();

        for (int i = 0; i < 3; i++) {
            guard.allow(channel);
        }

        assertThat(guard.allow(channel)).isFalse();
        assertThat(guard.allow(other)).as("另一个连接有自己的额度").isTrue();
    }

    @Test
    @DisplayName("关闭时一律放行")
    void allow_disabled() {
        properties.getFrameLimit().setEnabled(false);
        guard = new WsFrameGuard(properties);

        for (int i = 0; i < 100; i++) {
            assertThat(guard.allow(channel)).isTrue();
        }
    }
}
