package com.xd11cc.single.entity.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.xd11cc.single.entity.base.BaseTenantDO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@TableName("workflow_expression_config")
@ApiModel("Flowable表达式配置")
public class WorkflowExpressionConfigDO extends BaseTenantDO implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("配置键")
    private String configKey;

    @ApiModelProperty("配置名称")
    private String configName;

    @ApiModelProperty("表达式使用场景")
    private String expressionType;

    @ApiModelProperty("表达式模板")
    private String expressionTemplate;

    @ApiModelProperty("参数定义JSON")
    private String parameterSchema;

    @ApiModelProperty("返回类型")
    private String returnType;

    @ApiModelProperty("状态：0正常，1停用")
    private String status;

    private String remark;
}
