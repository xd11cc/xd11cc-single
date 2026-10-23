package com.xd11cc.single.entity.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@ApiModel("Flowable表达式配置新增对象")
public class WorkflowExpressionConfigAddVO {
    @NotBlank(message = "配置键不能为空")
    @Size(max = 128, message = "配置键不能超过128个字符")
    private String configKey;
    @NotBlank(message = "配置名称不能为空")
    private String configName;
    @NotBlank(message = "表达式使用场景不能为空")
    private String expressionType;
    @NotBlank(message = "表达式模板不能为空")
    private String expressionTemplate;
    @NotBlank(message = "参数定义不能为空")
    private String parameterSchema;
    @NotBlank(message = "返回类型不能为空")
    private String returnType;
    private String status = "0";
    private String remark;
}
