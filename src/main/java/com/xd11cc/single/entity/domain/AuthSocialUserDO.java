package com.xd11cc.single.entity.domain;

import com.xd11cc.single.entity.base.BaseTenantDO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import java.util.Date;

import lombok.Data;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.IdType;

/**
 *   @author xd11cc
 *   @date 2026-05-13
 */
@Data
@TableName("auth_social_user")
@ApiModel(value = "AuthSocialUserDO", description = "社交用户对象")
public class AuthSocialUserDO implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    @ApiModelProperty(value = "主键id", required = true)
    private Long id;

    @ApiModelProperty(value = "第三方系统唯一id", required = true)
    private String uuid;

    @ApiModelProperty(value = "用户id", required = false)
    private Long userId;

    @ApiModelProperty(value = "应用类型", required = true)
    private String source;

    @ApiModelProperty(value = "社交openId", required = true)
    private String openId;

    @ApiModelProperty(value = "社交token", required = true)
    private String token;

    @ApiModelProperty(value = "社交token原始信息", required = true)
    private String rowTokenInfo;

    @ApiModelProperty(value = "社交昵称", required = true)
    private String nickname;

    @ApiModelProperty(value = "社交头像", required = true)
    private String avatar;

    @ApiModelProperty(value = "社交用户原始信息", required = true)
    private String rowUserInfo;

    @ApiModelProperty(value = "最后一次认证code", required = true)
    private String code;

    @ApiModelProperty(value = "最后一次认证state", required = true)
    private String state;

    @ApiModelProperty(value = "备注", required = false)
    private String remark;

    @ApiModelProperty(value = "租户id", required = true)
    private Integer tenantId;

    @ApiModelProperty(value = "绑定时间", required = true)
    private Date bindTime;
}