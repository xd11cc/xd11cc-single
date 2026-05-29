package com.xd11cc.single.config.event;

import com.alibaba.fastjson2.JSON;
import com.xd11cc.single.config.netty.ChannelManager;
import com.xd11cc.single.entity.dto.NoticeWebSocketDTO;
import com.xd11cc.single.entity.dto.WsPushMessage;
import com.xd11cc.single.enums.NoticeScopeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Date;

/**
 * @author xd11cc
 * @date 2026-05-29
 */
@Slf4j
@Component
public class NoticeEventListener {

    @Autowired
    private ChannelManager channelManager;

    @Async("noticeTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onNoticeEvent(NoticeEvent event) {
        try {
            switch (event.getAction()) {
                case NEW_NOTICE:
                    handleNewNotice(event);
                    break;
                case REVOKE_NOTICE:
                    handleRevokeNotice(event);
                    break;
            }
        } catch (Exception e) {
            log.error("[Notice] 事件处理失败, action={}, noticeId={}", event.getAction(), event.getNoticeId(), e);
        }
    }

    private void handleNewNotice(NoticeEvent event) {
        NoticeWebSocketDTO dto = new NoticeWebSocketDTO();
        dto.setAction("NEW_NOTICE");
        dto.setNoticeId(event.getNoticeId());
        dto.setTitle(event.getTitle());
        dto.setType(event.getType());
        dto.setSenderName(event.getSenderName());
        dto.setSendTime(new Date());

        String message = JSON.toJSONString(WsPushMessage.of("NEW_NOTICE", dto));

        if (NoticeScopeEnum.ALL.getCode().equals(event.getScope())) {
            channelManager.broadcastToTenant(event.getTenantId(), message);
        } else {
            if (event.getTargetUserIds() != null) {
                for (Long userId : event.getTargetUserIds()) {
                    channelManager.pushToUser(userId, message);
                }
            }
        }
    }

    private void handleRevokeNotice(NoticeEvent event) {
        NoticeWebSocketDTO dto = new NoticeWebSocketDTO();
        dto.setAction("REVOKE_NOTICE");
        dto.setNoticeId(event.getNoticeId());
        dto.setType(event.getType());

        String message = JSON.toJSONString(WsPushMessage.of("REVOKE_NOTICE", dto));
        channelManager.broadcastToTenant(event.getTenantId(), message);
    }
}
