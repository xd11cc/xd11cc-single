package com.xd11cc.single.entity.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 工作流推送消息体（随 {@link WsPushMessage} 的 data 字段下发）。
 * <p>
 * 前端按 {@code action} 区分业务类型，例如：
 * <ul>
 *     <li>{@code WORKFLOW_APPROVED}  有新任务待我审批</li>
 *     <li>{@code WORKFLOW_REJECTED}  我提交的流程被驳回，需重新处理</li>
 *     <li>{@code WORKFLOW_COMPLETED} 我发起的流程已审批完成</li>
 *     <li>{@code WORKFLOW_CANCELLED} 流程已被发起人撤回</li>
 *     <li>{@code WORKFLOW_DELEGATED} 任务已转办给我</li>
 * </ul>
 * </p>
 *
 * @author xd11cc
 */
@Data
public class WorkflowWebSocketDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 事件动作，与 {@code WorkflowEvent.Action} 对应
     */
    private String action;

    private String processInstanceId;

    private String processDefinitionKey;

    private String businessKey;

    /**
     * 接收该消息的用户 ID
     */
    private Long targetUserId;

    private Date sendTime;
}
