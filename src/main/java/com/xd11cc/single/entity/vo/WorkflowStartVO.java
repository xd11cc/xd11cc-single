package com.xd11cc.single.entity.vo;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.Map;

/**
 * @author xd11cc
 * @date 2026-05-29
 */
@Data
public class WorkflowStartVO {

    @NotBlank(message = "流程定义key不能为空")
    private String processDefinitionKey;

    @NotBlank(message = "业务主键不能为空")
    private String businessKey;

    private Map<String, Object> variables;
}
