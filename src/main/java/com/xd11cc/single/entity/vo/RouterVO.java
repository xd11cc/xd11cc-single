package com.xd11cc.single.entity.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author xd11cc
 * @date 2026-01-15 08:45:33
 */
@Data
@ApiModel("路由信息")
public class RouterVO {

    @ApiModelProperty("路由名称")
    private String name;

    @ApiModelProperty("路由路径")
    private String path;

    @ApiModelProperty("组件名称")
    private String component;

    @ApiModelProperty("重定向")
    private String redirect;

    @ApiModelProperty("其他元素")
    private MetaVO meta;

    @ApiModelProperty("子菜单")
    private List<RouterVO> children;
}
