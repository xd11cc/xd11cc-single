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
@TableName("system_dept_post")
@ApiModel(value = "SystemDeptPostDO", description = "部门岗位对象")
public class SystemDeptPostDO extends BaseTenantDO implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    @ApiModelProperty(value = "主键id", required = true)
    private Long id;

    @ApiModelProperty(value = "部门id", required = true)
    private Long deptId;

    @ApiModelProperty(value = "岗位id", required = true)
    private Long postId;

    @ApiModelProperty(value = "备注", required = false)
    private String remark;

}