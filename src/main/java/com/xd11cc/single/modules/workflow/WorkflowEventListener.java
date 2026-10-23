package com.xd11cc.single.modules.workflow;

import com.alibaba.fastjson2.JSON;
import com.xd11cc.single.infrastructure.netty.ChannelManager;
import com.xd11cc.single.entity.dto.WorkflowWebSocketDTO;
import com.xd11cc.single.entity.dto.WsPushMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 工作流事件监听器：把流程动作转换为 WebSocket 推送给相关用户。
 * <p>
 * 与 {@code NoticeEventListener} 保持一致的落点设计：
 * <ul>
 *     <li>{@code @TransactionalEventListener(AFTER_COMMIT)}：业务事务提交后才推送，
 *         避免“推送成功但数据库回滚”的假通知</li>
 *     <li>{@code @Async("noticeTaskExecutor")}：异步执行，不阻塞审批主流程</li>
 *     <li>异常全部吞掉并记录日志：推送属于旁路能力，不能反向影响审批结果</li>
 * </ul>
 * </p>
 *
 * @author xd11cc
 * @date 2026-05-29
 */
@Slf4j
@Component
public class WorkflowEventListener {

    /**
     * 事件动作 → 前端推送 action 的映射
     */
    private static final Map<WorkflowEvent.Action, String> PUSH_ACTION_MAP = new HashMap<>();

    static {
        PUSH_ACTION_MAP.put(WorkflowEvent.Action.APPROVED, "WORKFLOW_APPROVED");
        PUSH_ACTION_MAP.put(WorkflowEvent.Action.REJECTED, "WORKFLOW_REJECTED");
        PUSH_ACTION_MAP.put(WorkflowEvent.Action.COMPLETED, "WORKFLOW_COMPLETED");
        PUSH_ACTION_MAP.put(WorkflowEvent.Action.CANCELLED, "WORKFLOW_CANCELLED");
        PUSH_ACTION_MAP.put(WorkflowEvent.Action.DELEGATED, "WORKFLOW_DELEGATED");
    }

    @Autowired
    private ChannelManager channelManager;

    @Async("noticeTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onWorkflowEvent(WorkflowEvent event) {
        try {
            String pushAction = PUSH_ACTION_MAP.get(event.getAction());
            if (pushAction == null) {
                log.debug("[工作流事件] 无需推送的 action={}", event.getAction());
                return;
            }

            WorkflowWebSocketDTO dto = new WorkflowWebSocketDTO();
            dto.setAction(pushAction);
            dto.setProcessInstanceId(event.getProcessInstanceId());
            dto.setProcessDefinitionKey(event.getProcessDefinitionKey());
            dto.setBusinessKey(event.getBusinessKey());
            dto.setTargetUserId(event.getTargetUserId());
            dto.setSendTime(new Date());

            String message = JSON.toJSONString(WsPushMessage.of(pushAction, dto));

            // 定向推送优先；COMPLETED/CANCELLED 这类“通知发起人”的事件同样走单播
            if (event.getTargetUserId() != null) {
                channelManager.pushToUser(event.getTargetUserId(), message);
                log.debug("[工作流事件] 已推送给用户 {}，action={}, processInstanceId={}",
                        event.getTargetUserId(), pushAction, event.getProcessInstanceId());
            } else if (event.getTenantId() != null) {
                // 无明确接收人时退化为租户广播（例如流程完成且未指定回调用户）
                channelManager.broadcastToTenant(event.getTenantId(), message);
                log.debug("[工作流事件] 已广播到租户 {}，action={}, processInstanceId={}",
                        event.getTenantId(), pushAction, event.getProcessInstanceId());
            } else {
                log.debug("[工作流事件] 无接收人且无租户，跳过推送 action={}", pushAction);
            }
        } catch (Exception e) {
            // 推送失败不影响审批结果，仅记录日志
            log.error("[工作流事件] 推送失败, action={}, processInstanceId={}",
                    event.getAction(), event.getProcessInstanceId(), e);
        }
    }
}
