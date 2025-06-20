package com.xd11cc.single.entity.vo.base;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author: xd11cc
 * @Date: 2025/6/14 00:43
 **/
@Data
@ApiModel("基础登录信息")
public class BaseLoginVO {

    @ApiModelProperty("登录途径 0-用户名+密码 1-手机号+密码")
    private Integer way;

    @ApiModelProperty("登录设备 0-电脑")
    private Integer device;

    @ApiModelProperty("登录程序 0-网页")
    private Integer app;

    @ApiModelProperty("操作系统")
    private String optSystem;

    @ApiModelProperty("浏览器")
    private String browser;

    @ApiModelProperty("纬度")
    private String lat;

    @ApiModelProperty("经度")
    private String lng;
}
