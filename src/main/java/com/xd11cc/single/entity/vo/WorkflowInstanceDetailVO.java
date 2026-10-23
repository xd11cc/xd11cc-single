package com.xd11cc.single.entity.vo;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author xd11cc
 * @date 2026-05-29
 */
@Data
public class WorkflowInstanceDetailVO {

    private String processInstanceId;

    private String processDefinitionKey;

    private String businessKey;

    private String title;

    private Integer status;

    private Long starterUserId;

    private String starterUsername;

    private Date createTime;

    private Date endTime;

    private List<WorkflowTaskDetailVO> taskHistory;

    private List<WorkflowTaskDetailVO> currentTasks;
}
