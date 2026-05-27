package com.xd11cc.single.entity.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * @author xd11cc
 * @date 2026-05-27
 */
@Data
@ApiModel("角色更新对象")
public class SystemRoleUpdateVO {

    @ApiModelProperty("主键id")
    @NotNull(message = "请选择角色")
    private Long id;

    @ApiModelProperty("角色编码")
    @NotBlank(message = "角色编码不能为空")
    @Size(max = 20, message = "角色编码不能超过20个字符")
    private String roleCode;

    @ApiModelProperty("角色名称")
    @NotBlank(message = "角色名称不能为空")
    @Size(max = 20, message = "角色名称不能超过20个字符")
    private String roleName;

    @ApiModelProperty("角色状态，字典类型system_status")
    private String status;

    @ApiModelProperty("菜单id列表")
    @NotNull(message = "菜单权限不能为空")
    private List<Long> menuIds;

    @ApiModelProperty("备注")
    private String remark;
}
