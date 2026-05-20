package com.xd11cc.single.entity.vo;

import com.xd11cc.single.entity.base.BaseLoginVO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author xd11cc
 * @date 2026-05-19 14:48:10
 * @description
 */
@Data
public class SocialUserBindVO extends BaseLoginVO {

    @NotBlank(message = "请输入用户名")
    private String username;

    @NotBlank(message = "请输入密码")
    private String password;

    private String state;

    private String source;
}
