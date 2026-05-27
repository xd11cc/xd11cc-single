package com.xd11cc.single.entity.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * @author xd11cc
 * @date 2026-05-27
 */
@Data
@ApiModel("授权应用配置更新对象")
public class AuthClientConfigUpdateVO {

    @ApiModelProperty("主键id")
    @NotNull(message = "请选择授权应用")
    private Long id;

    @ApiModelProperty("应用类型")
    @NotBlank(message = "应用类型不能为空")
    @Size(max = 32, message = "应用类型不能超过32个字符")
    private String source;

    @ApiModelProperty("应用id")
    @NotBlank(message = "应用id不能为空")
    private String clientId;

    @ApiModelProperty("应用密钥")
    @NotBlank(message = "应用密钥不能为空")
    private String clientSecret;

    @ApiModelProperty("重定向地址")
    private String redirectUri;

    @ApiModelProperty("应用名称")
    private String name;

    @ApiModelProperty("图标")
    @NotBlank(message = "图标不能为空")
    private String icon;

    @ApiModelProperty("状态（0正常 1停用）")
    @NotBlank(message = "状态不能为空")
    private String status;

    @ApiModelProperty("排序")
    private Integer sort;

    @ApiModelProperty("备注")
    private String remark;
}
