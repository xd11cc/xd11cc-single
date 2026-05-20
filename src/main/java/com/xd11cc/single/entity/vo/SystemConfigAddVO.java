package com.xd11cc.single.entity.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * @author xd11cc
 * @date 2026-05-20
 */
@Data
@ApiModel("系统配置新增对象")
public class SystemConfigAddVO {

    @ApiModelProperty("配置键")
    @NotBlank(message = "配置键不能为空")
    @Size(max = 100, message = "配置键不能超过100个字符")
    private String configKey;

    @ApiModelProperty("配置值")
    @NotBlank(message = "配置值不能为空")
    @Size(max = 500, message = "配置值不能超过500个字符")
    private String configValue;

    @ApiModelProperty("配置名称")
    @NotBlank(message = "配置名称不能为空")
    @Size(max = 100, message = "配置名称不能超过100个字符")
    private String configName;

    @ApiModelProperty("备注")
    private String remark;
}
