package com.xd11cc.single.entity.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.xd11cc.single.entity.base.BaseDO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author: xd11cc
 * @Date: 2025/6/13 23:29
 **/
@Data
@ApiModel("菜单")
@TableName("system_menu")
public class SystemMenuDO extends BaseDO {

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
    private boolean visible;

    @ApiModelProperty("备注")
    private String remark;

}
