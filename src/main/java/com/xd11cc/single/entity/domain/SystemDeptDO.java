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
@TableName("system_dept")
@ApiModel(value = "SystemDeptDO", description = "部门对象")
public class SystemDeptDO extends BaseTenantDO implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    @ApiModelProperty(value = "主键id", required = true)
    private Long id;

    @ApiModelProperty(value = "父部门id", required = true)
    private Long parentId;

    @ApiModelProperty(value = "部门编码", required = true)
    private String deptCode;

    @ApiModelProperty(value = "部门名称", required = true)
    private String deptName;

    @ApiModelProperty(value = "部门负责人", required = true)
    private Long leaderId;

    @ApiModelProperty(value = "排序", required = true)
    private Integer sort;

    @ApiModelProperty(value = "部门状态，字典类型system_status", required = true)
    private String status;

    @ApiModelProperty(value = "备注", required = false)
    private String remark;

}