package com.xd11cc.single.entity.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.xd11cc.single.entity.vo.base.BaseVO;
import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * @Author: xd11cc
 * @Date: 2025/6/13 23:27
 **/
@Data
@ApiModel("用户角色关联")
@TableName("system_user_role")
public class SystemUserRoleDO extends BaseVO {

    private Long id;

    private Long userId;

    private Long roleId;
}
