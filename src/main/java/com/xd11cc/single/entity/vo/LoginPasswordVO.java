package com.xd11cc.single.entity.vo;

import com.xd11cc.single.entity.base.BaseLoginVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;

/**
 * @Author: xd11cc
 * @Date: 2025/6/14 00:39
 **/
@Data
@ApiModel("密码登录对象")
@EqualsAndHashCode(callSuper = true)
public class LoginPasswordVO extends BaseLoginVO {

    @NotBlank(message = "请输入用户名")
    @ApiModelProperty("用户名")
    private String username;

    @NotBlank(message = "请输入密码")
    @ApiModelProperty("密码")
    private String password;

    @NotBlank(message = "非法请求")
    @ApiModelProperty("验证码id")
    private String captchaId;

    @NotBlank(message = "请输入验证码")
    @ApiModelProperty("验证码")
    private String captcha;

}
