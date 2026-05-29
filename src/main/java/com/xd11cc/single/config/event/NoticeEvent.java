package com.xd11cc.single.config.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.List;

/**
 * @author xd11cc
 * @date 2026-05-29
 */
@Getter
public class NoticeEvent extends ApplicationEvent {

    private final Action action;
    private final Long noticeId;
    private final String title;
    private final Integer type;
    private final Integer scope;
    private final Long tenantId;
    private final String senderName;
    private final List<Long> targetUserIds;

    public enum Action {
        NEW_NOTICE,
        REVOKE_NOTICE
    }

    private NoticeEvent(Object source, Action action, Long noticeId, String title,
                        Integer type, Integer scope, Long tenantId,
                        String senderName, List<Long> targetUserIds) {
        super(source);
        this.action = action;
        this.noticeId = noticeId;
        this.title = title;
        this.type = type;
        this.scope = scope;
        this.tenantId = tenantId;
        this.senderName = senderName;
        this.targetUserIds = targetUserIds;
    }

    public static NoticeEvent publish(Object source, Long noticeId, String title,
                                      Integer type, Integer scope, Long tenantId,
                                      String senderName, List<Long> targetUserIds) {
        return new NoticeEvent(source, Action.NEW_NOTICE, noticeId, title,
                type, scope, tenantId, senderName, targetUserIds);
    }

    public static NoticeEvent revoke(Object source, Long noticeId, Integer type, Long tenantId) {
        return new NoticeEvent(source, Action.REVOKE_NOTICE, noticeId, null,
                type, null, tenantId, null, null);
    }
}
