package com.xd11cc.single.entity.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.xd11cc.single.entity.vo.base.BaseVO;
import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * @Author: xd11cc
 * @Date: 2025/6/13 23:43
 **/
@Data
@ApiModel("角色菜单")
@TableName("system_role_menu")
public class SystemRoleMenuDO extends BaseVO {

    private Long id;

    private Long roleId;

    private Long menuId;
}
