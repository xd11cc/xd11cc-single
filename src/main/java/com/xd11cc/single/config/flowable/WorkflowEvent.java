package com.xd11cc.single.config.flowable;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * @author xd11cc
 * @date 2026-05-29
 */
@Getter
public class WorkflowEvent extends ApplicationEvent {

    public enum Action {
        APPROVED, REJECTED, COMPLETED, CANCELLED, DELEGATED
    }

    private final Action action;
    private final String processInstanceId;
    private final String processDefinitionKey;
    private final String businessKey;
    private final Long tenantId;
    private final Long targetUserId;

    private WorkflowEvent(Object source, Action action, String processInstanceId,
                          String processDefinitionKey, String businessKey,
                          Long tenantId, Long targetUserId) {
        super(source);
        this.action = action;
        this.processInstanceId = processInstanceId;
        this.processDefinitionKey = processDefinitionKey;
        this.businessKey = businessKey;
        this.tenantId = tenantId;
        this.targetUserId = targetUserId;
    }

    public static WorkflowEvent approved(Object source, String processInstanceId,
                                         String processDefinitionKey, String businessKey,
                                         Long tenantId, Long targetUserId) {
        return new WorkflowEvent(source, Action.APPROVED, processInstanceId,
                processDefinitionKey, businessKey, tenantId, targetUserId);
    }

    public static WorkflowEvent rejected(Object source, String processInstanceId,
                                         String processDefinitionKey, String businessKey,
                                         Long tenantId, Long targetUserId) {
        return new WorkflowEvent(source, Action.REJECTED, processInstanceId,
                processDefinitionKey, businessKey, tenantId, targetUserId);
    }

    public static WorkflowEvent completed(Object source, String processInstanceId,
                                          String processDefinitionKey, String businessKey,
                                          Long tenantId, Long targetUserId) {
        return new WorkflowEvent(source, Action.COMPLETED, processInstanceId,
                processDefinitionKey, businessKey, tenantId, targetUserId);
    }

    public static WorkflowEvent cancelled(Object source, String processInstanceId,
                                          String processDefinitionKey, String businessKey,
                                          Long tenantId, Long targetUserId) {
        return new WorkflowEvent(source, Action.CANCELLED, processInstanceId,
                processDefinitionKey, businessKey, tenantId, targetUserId);
    }

    public static WorkflowEvent delegated(Object source, String processInstanceId,
                                          String processDefinitionKey, String businessKey,
                                          Long tenantId, Long targetUserId) {
        return new WorkflowEvent(source, Action.DELEGATED, processInstanceId,
                processDefinitionKey, businessKey, tenantId, targetUserId);
    }
}
