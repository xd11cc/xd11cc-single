package com.xd11cc.single.entity.vo;

import lombok.Data;

import java.util.Date;

/**
 * @author xd11cc
 * @date 2026-08-18
 */
@Data
public class WorkflowModelVO {

    private String id;

    private String name;

    private String key;

    private String category;

    /**
     * 部署ID，草稿状态为 null
     */
    private String deploymentId;

    private String processDefinitionId;

    /**
     * 流程版本，草稿状态为 null
     */
    private Integer version;

    /**
     * 状态：DRAFT-草稿、SUSPENDED-挂起、ACTIVE-激活、CONFLICT-异常
     */
    private String status;

    private Boolean suspended;

    private Date deployTime;

    private Date createTime;

    private Date lastUpdateTime;

    /**
     * 描述
     */
    private String description;

    private String editorSource;

    private String editorSourceExtraValue;
}
