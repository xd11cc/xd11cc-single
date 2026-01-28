package com.xd11cc.single.entity.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.xd11cc.single.entity.base.BaseTenantDO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author: xd11cc
 * @Date: 2025/6/13 23:25
 **/
@Data
@ApiModel("角色")
@TableName("system_role")
public class SystemRoleDO extends BaseTenantDO {

    @ApiModelProperty("主键id")
    private Long id;

    @ApiModelProperty("角色key")
    private String roleKey;

    @ApiModelProperty("角色名称")
    private String roleName;

    @ApiModelProperty("角色状态，字典类型system_status")
    private String status;

    @ApiModelProperty("备注")
    private String remark;
}
