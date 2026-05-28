package com.xd11cc.single.entity.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author xd11cc
 * @date 2026-05-28
 */
@Data
@ApiModel("修改密码对象")
public class SystemUserChangePasswordVO {

    @NotBlank(message = "旧密码不能为空")
    @ApiModelProperty("旧密码")
    private String oldPassword;

    @NotBlank(message = "新密码不能为空")
    @ApiModelProperty("新密码")
    private String newPassword;
}
