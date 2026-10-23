package com.xd11cc.single.entity.vo;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author xd11cc
 * @date 2026-05-29
 */
@Data
public class WorkflowTaskDelegateVO {

    @NotBlank(message = "任务ID不能为空")
    private String taskId;

    @NotBlank(message = "转办目标用户ID不能为空")
    private String targetUserId;

    private String comment;
}
