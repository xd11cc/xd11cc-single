package com.xd11cc.single.entity.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author xd11cc
 * @date 2026-02-03 11:07:49
 * @description
 */
@Data
@ApiModel("菜单新增对象")
public class SystemMenuAddVO {

    @ApiModelProperty("父菜单id")
    @NotNull(message = "父菜单不能为空")
    private Long parentId;

    @ApiModelProperty("菜单名称")
    @NotBlank(message = "菜单名称不能为空")
    private String menuName;

    @ApiModelProperty("排序")
    @NotNull(message = "排序不能为空")
    private Integer sort;

    @ApiModelProperty("路由地址")
    private String path;

    @ApiModelProperty("组件路径")
    private String component;

    @ApiModelProperty("路由名称")
    private String routeName;

    @ApiModelProperty("路由参数")
    private String query;

    @ApiModelProperty("菜单图标")
    private String icon;

    @ApiModelProperty("菜单类型，字典类型system_menu_type")
    private String menuType;

    @ApiModelProperty("菜单状态，字典类型system_status")
    private String status;

    @ApiModelProperty("权限字符")
    private String permission;

    @ApiModelProperty("是否显示，0-否 1-是")
    private String visible;

    @ApiModelProperty("备注")
    private String remark;
}
