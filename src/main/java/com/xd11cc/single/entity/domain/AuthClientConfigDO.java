package com.xd11cc.single.entity.domain;

import com.xd11cc.single.entity.base.BaseTenantDO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import lombok.Data;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.IdType;

/**
*   @author xd11cc
*   @date 2026-05-20
*/
@Data
@TableName("auth_client_config")
@ApiModel(value = "AuthClientConfigDO", description = "授权应用配置对象")
public class AuthClientConfigDO extends BaseTenantDO implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    @ApiModelProperty(value = "主键id", required = true)
    private Long id;

    @ApiModelProperty(value = "应用类型", required = true)
    private String source;

    @ApiModelProperty(value = "应用id", required = true)
    private String clientId;

    @ApiModelProperty(value = "应用密钥", required = true)
    private String clientSecret;

    @ApiModelProperty(value = "重定向地址", required = false)
    private String redirectUri;

    @ApiModelProperty(value = "应用名称", required = false)
    private String name;

    @ApiModelProperty(value = "图标", required = true)
    private String icon;

    @ApiModelProperty(value = "排序", required = false)
    private Integer sort;

    @ApiModelProperty(value = "状态 system_status", required = false)
    private String status;

    @ApiModelProperty(value = "备注", required = false)
    private String remark;

}