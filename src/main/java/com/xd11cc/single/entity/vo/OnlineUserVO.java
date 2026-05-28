package com.xd11cc.single.entity.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * @author xd11cc
 * @date 2026-05-28
 */
@Data
@ApiModel("在线用户")
public class OnlineUserVO {

    @ApiModelProperty("会话token")
    private String tokenId;

    @ApiModelProperty("用户id")
    private Long userId;

    @ApiModelProperty("用户名")
    private String username;

    @ApiModelProperty("登录IP")
    private String ipAddr;

    @ApiModelProperty("浏览器")
    private String browser;

    @ApiModelProperty("操作系统")
    private String os;

    @ApiModelProperty("登录时间")
    private Date loginTime;
}
