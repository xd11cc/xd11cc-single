package com.xd11cc.single.entity.vo;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author xd11cc
 * @date 2026-08-18
 */
@Data
public class WorkflowDraftSaveVO {

    @NotBlank(message = "流程名称不能为空")
    private String name;

    private String key;

    private String category;

    private String description;

    /**
     * BPMN XML 内容
     */
    @NotBlank(message = "BPMN XML 不能为空")
    private String editorSource;

    /**
     * bpmn.js 附加属性 JSON
     */
    private String editorSourceExtraValue;

    /**
     * 已有模型ID（更新时传入，新建时不传）
     */
    private String modelId;
}
