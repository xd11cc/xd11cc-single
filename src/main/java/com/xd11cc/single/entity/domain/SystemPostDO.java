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
@TableName("system_post")
@ApiModel(value = "SystemPostDO", description = "岗位对象")
public class SystemPostDO extends BaseTenantDO implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    @ApiModelProperty(value = "主键id", required = true)
    private Long id;

    @ApiModelProperty(value = "岗位编码", required = true)
    private String postCode;

    @ApiModelProperty(value = "岗位名称", required = true)
    private String postName;

    @ApiModelProperty(value = "岗位状态，字典类型system_status", required = true)
    private String status;

    @ApiModelProperty(value = "备注", required = false)
    private String remark;

}