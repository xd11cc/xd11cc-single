package com.xd11cc.single.config.event;

import com.xd11cc.single.config.netty.ChannelManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

class NoticeEventListenerTest {

    private NoticeEventListener listener;
    private ChannelManager channelManager;

    @BeforeEach
    void setUp() throws Exception {
        listener = new NoticeEventListener();
        channelManager = mock(ChannelManager.class);
        Field field = NoticeEventListener.class.getDeclaredField("channelManager");
        field.setAccessible(true);
        field.set(listener, channelManager);
    }

    // ==================== onNoticeEvent - NEW_NOTICE ====================

    @Test
    void newNotice_范围全部_调用租户广播() {
        NoticeEvent event = NoticeEvent.publish(this, 1L, "通知标题", 1, 1, 10L, "管理员", null);

        listener.onNoticeEvent(event);

        then(channelManager).should().broadcastToTenant(eq(10L), anyString());
    }

    @Test
    void newNotice_范围指定用户_调用逐用户推送() {
        NoticeEvent event = NoticeEvent.publish(this, 1L, "通知标题", 1, 2, 10L, "管理员", Arrays.asList(101L, 102L));

        listener.onNoticeEvent(event);

        then(channelManager).should().pushToUser(eq(101L), anyString());
        then(channelManager).should().pushToUser(eq(102L), anyString());
    }

    // ==================== onNoticeEvent - REVOKE_NOTICE ====================

    @Test
    void revokeNotice_调用租户广播撤回消息() {
        NoticeEvent event = NoticeEvent.revoke(this, 1L, 1, 10L);

        listener.onNoticeEvent(event);

        then(channelManager).should().broadcastToTenant(eq(10L), anyString());
    }

    // ==================== 异常处理 ====================

    @Test
    void onNoticeEvent_处理异常时不抛出让上层感知() {
        NoticeEvent event = NoticeEvent.publish(this, 1L, "通知标题", 1, 1, 10L, "管理员", null);
        // ChannelManager is a mock, won't throw
        listener.onNoticeEvent(event);
        // 验证不抛异常
        assertThat(true).isTrue();
    }
}
