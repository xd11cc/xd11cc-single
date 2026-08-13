package com.xd11cc.single.config.event;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class NoticeEventTest {

    // ==================== publish ====================

    @Test
    void publish_创建NEW_NOTICE事件() {
        NoticeEvent event = NoticeEvent.publish(
                new Object(), 1L, "系统通知", 1, 2, 10L, "管理员", Collections.singletonList(1L));

        assertThat(event.getAction()).isEqualTo(NoticeEvent.Action.NEW_NOTICE);
        assertThat(event.getNoticeId()).isEqualTo(1L);
        assertThat(event.getTitle()).isEqualTo("系统通知");
        assertThat(event.getType()).isEqualTo(1);
        assertThat(event.getScope()).isEqualTo(2);
        assertThat(event.getTenantId()).isEqualTo(10L);
        assertThat(event.getSenderName()).isEqualTo("管理员");
        assertThat(event.getTargetUserIds()).containsExactly(1L);
    }

    // ==================== publish 多用户 ====================

    @Test
    void publish_多目标用户_正确存储() {
        NoticeEvent event = NoticeEvent.publish(
                new Object(), 2L, "重要通知", 2, 1, 20L, "系统",
                Arrays.asList(1L, 2L, 3L));

        assertThat(event.getAction()).isEqualTo(NoticeEvent.Action.NEW_NOTICE);
        assertThat(event.getNoticeId()).isEqualTo(2L);
        assertThat(event.getTargetUserIds()).containsExactly(1L, 2L, 3L);
    }

    // ==================== revoke ====================

    @Test
    void revoke_创建REVOKE_NOTICE事件() {
        NoticeEvent event = NoticeEvent.revoke(new Object(), 3L, 1, 30L);

        assertThat(event.getAction()).isEqualTo(NoticeEvent.Action.REVOKE_NOTICE);
        assertThat(event.getNoticeId()).isEqualTo(3L);
        assertThat(event.getType()).isEqualTo(1);
        assertThat(event.getTenantId()).isEqualTo(30L);
        assertThat(event.getTitle()).isNull();
        assertThat(event.getScope()).isNull();
        assertThat(event.getSenderName()).isNull();
        assertThat(event.getTargetUserIds()).isNull();
    }

    // ==================== getSource ====================

    @Test
    void getSource_返回父类ApplicationEvent的source() {
        Object source = new Object();
        NoticeEvent event = NoticeEvent.publish(source, 1L, "标题", 1, 2, 10L, "发送者", Collections.emptyList());

        assertThat(event.getSource()).isSameAs(source);
    }
}
