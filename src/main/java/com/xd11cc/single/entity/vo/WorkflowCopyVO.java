package com.xd11cc.single.entity.vo;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author xd11cc
 * @date 2026-05-29
 */
@Data
public class WorkflowCopyVO {

    @NotBlank(message = "源流程部署ID不能为空")
    private String sourceDeploymentId;

    @NotBlank(message = "新流程名称不能为空")
    private String newName;
}
