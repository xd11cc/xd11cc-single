package com.xd11cc.single.entity.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

/**
 * @author xd11cc
 * @date 2026-01-15 09:09:17
 */
@Data
@Builder
@ApiModel("其他元素")
public class MetaVO {

    @ApiModelProperty("标题")
    private String title;

    @ApiModelProperty("图标")
    private String icon;

    @ApiModelProperty("路由参数")
    private String query;

    @ApiModelProperty("是否隐藏 0-显示 1-隐藏")
    private boolean hidden;

    @ApiModelProperty("权限字符")
    private String permission;

    @ApiModelProperty("是否显示根目录")
    private boolean alwaysShow;

}
