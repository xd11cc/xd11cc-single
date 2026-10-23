package com.xd11cc.single.entity.vo;

import lombok.Data;

import java.util.Date;

/**
 * @author xd11cc
 * @date 2026-08-18
 */
@Data
public class WorkflowModelActivityVO {

    private String activityId;

    private String activityName;

    private String activityType;

    private String assignee;

    private Date startTime;

    private Date endTime;

    private Long durationMs;
}
