package com.xd11cc.single.entity.vo;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.Map;

/**
 * @author xd11cc
 * @date 2026-05-29
 */
@Data
public class WorkflowTaskApproveVO {

    @NotBlank(message = "任务ID不能为空")
    private String taskId;

    private String comment;

    private Map<String, Object> variables;
}
