package com.xd11cc.single.entity.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @Author: xd11cc
 * @Date: 2025/7/9 16:21
 **/
@Data
@ApiModel("菜单树形结构")
public class TreeMenuVO {

    @ApiModelProperty("主键id")
    private Long id;

    @ApiModelProperty("父菜单id")
    private Long parentId;

    @ApiModelProperty("菜单名称")
    private String menuName;

    @ApiModelProperty("排序")
    private Integer sort;

    @ApiModelProperty("路由地址")
    private String path;

    @ApiModelProperty("组件路径")
    private String component;

    @ApiModelProperty("是否为外链，字典类型system_logic_status")
    private String ifFrame;

    @ApiModelProperty("菜单类型，字典类型system_menu_type")
    private String menuType;

    @ApiModelProperty("是否隐藏，字典类型system_logic_status")
    private String visible;

    @ApiModelProperty("菜单状态，字典类型system_enable_status")
    private String status;

    @ApiModelProperty("菜单图标")
    private String icon;

    @ApiModelProperty("子菜单")
    private List<TreeMenuVO> children;
}
