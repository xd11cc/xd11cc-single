package com.xd11cc.single.entity.vo;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author xd11cc
 * @date 2026-08-18
 */
@Data
public class WorkflowDraftDeployVO {

    @NotBlank(message = "模型ID不能为空")
    private String modelId;
}
