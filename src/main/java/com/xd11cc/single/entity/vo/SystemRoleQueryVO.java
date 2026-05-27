package com.xd11cc.single.entity.vo;

import com.xd11cc.single.entity.base.BasePageVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author xd11cc
 * @date 2026-05-27
 */
@Data
@ApiModel("角色查询对象")
public class SystemRoleQueryVO extends BasePageVO {

    @ApiModelProperty("角色编码")
    private String roleCode;

    @ApiModelProperty("角色名称")
    private String roleName;

    @ApiModelProperty("角色状态")
    private String status;
}
