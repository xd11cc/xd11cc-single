package com.xd11cc.single.config.flowable;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * @author xd11cc
 * @date 2026-05-29
 */
@Slf4j
@Component
public class WorkflowEventListener {

    @Async("noticeTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onWorkflowEvent(WorkflowEvent event) {
        log.info("[工作流事件] action={}, processInstanceId={}, targetUserId={}",
                event.getAction(), event.getProcessInstanceId(), event.getTargetUserId());
        // TODO 对接 WebSocket 推送通知
    }
}
