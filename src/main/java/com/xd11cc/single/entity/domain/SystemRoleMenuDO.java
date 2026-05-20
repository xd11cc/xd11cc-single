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
*   @date 2026-04-28
*/
@Data
@TableName("system_role_menu")
@ApiModel(value = "SystemRoleMenuDO", description = "角色菜单对象")
public class SystemRoleMenuDO extends BaseTenantDO implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    @ApiModelProperty(value = "主键id", required = true)
    private Long id;

    @ApiModelProperty(value = "角色id", required = true)
    private Long roleId;

    @ApiModelProperty(value = "菜单id", required = true)
    private Long menuId;

    @ApiModelProperty(value = "备注", required = true)
    private String remark;

}