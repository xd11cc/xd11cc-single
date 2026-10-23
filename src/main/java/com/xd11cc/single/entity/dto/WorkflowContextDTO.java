package com.xd11cc.single.entity.dto;

import lombok.Data;

import java.util.Map;

/**
 * @author xd11cc
 * @date 2026-05-29
 */
@Data
public class WorkflowContextDTO {

    private String processInstanceId;

    private String processDefinitionKey;

    private String businessKey;

    private String taskId;

    private String taskDefinitionKey;

    private Long operatorUserId;

    private String operatorUsername;

    private Long tenantId;

    private String comment;

    private Map<String, Object> variables;
}
