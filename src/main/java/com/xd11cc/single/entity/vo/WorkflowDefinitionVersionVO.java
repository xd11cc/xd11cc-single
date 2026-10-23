package com.xd11cc.single.entity.vo;

import lombok.Data;

import java.util.Date;

/** 流程定义版本详情。 */
@Data
public class WorkflowDefinitionVersionVO {
    private String processDefinitionId;
    private String deploymentId;
    private String key;
    private String name;
    private String category;
    private Integer version;
    private String status;
    private Boolean suspended;
    private Date deployTime;
}
