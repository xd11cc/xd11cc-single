package com.xd11cc.single.entity.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.xd11cc.single.entity.vo.base.BaseVO;
import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * @Author: xd11cc
 * @Date: 2025/6/13 23:25
 **/
@Data
@ApiModel("角色")
@TableName("system_role")
public class SystemRoleDO extends BaseVO {

    private Long id;

    private String roleKey;

    private String roleName;

    private String status;
}
