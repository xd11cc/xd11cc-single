package com.xd11cc.single.entity.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.xd11cc.single.entity.base.BaseTenantDO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author: xd11cc
 * @Date: 2025/6/13 23:43
 **/
@Data
@ApiModel("角色菜单")
@TableName("system_role_menu")
public class SystemRoleMenuDO extends BaseTenantDO {

    @ApiModelProperty("主键id")
    private Long id;

    @ApiModelProperty("角色id")
    private Long roleId;

    @ApiModelProperty("菜单id")
    private Long menuId;

    @ApiModelProperty("备注")
    private String remark;
}
