package com.xd11cc.single.entity.vo;

import lombok.Data;

import java.util.Date;

/**
 * @author xd11cc
 * @date 2026-05-29
 */
@Data
public class WorkflowTaskDetailVO {

    private String taskId;

    private String taskName;

    private String taskDefinitionKey;

    private String assignee;

    private String assigneeName;

    private String processInstanceId;

    private String processDefinitionKey;

    private String businessKey;

    private String title;

    private String comment;

    private String commentType;

    private Date createTime;

    private Date endTime;
}
