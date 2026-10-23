package com.xd11cc.single.config.flowable;


import com.xd11cc.single.entity.dto.WorkflowContextDTO;

/**
 * @author xd11cc
 * @date 2026-05-29
 */
public abstract class AbstractWorkflowHandler {

    public abstract String getProcessDefinitionKey();

    public abstract String getStartActivityId();

    public void validateBeforeStart(WorkflowContextDTO context) {}

    public void onProcessStart(WorkflowContextDTO context) {}

    public void onTaskApprove(WorkflowContextDTO context) {}

    public void onTaskReject(WorkflowContextDTO context) {}

    public void onProcessComplete(WorkflowContextDTO context) {}

    public void onProcessCancel(WorkflowContextDTO context) {}

    public void onTaskDelegate(WorkflowContextDTO context) {}

    public String resolveTitle(WorkflowContextDTO context) {
        return getProcessDefinitionKey() + ":" + context.getBusinessKey();
    }
}
