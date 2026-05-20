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
*   @date 2026-05-20
*/
@Data
@TableName("system_config")
@ApiModel(value = "SystemConfigDO", description = "系统配置")
public class SystemConfigDO extends BaseTenantDO implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    @ApiModelProperty(value = "主键id", required = true)
    private Long id;

    @ApiModelProperty(value = "配置键", required = true)
    private String configKey;

    @ApiModelProperty(value = "配置值", required = true)
    private String configValue;

    @ApiModelProperty(value = "配置名称", required = true)
    private String configName;

    @ApiModelProperty(value = "备注", required = false)
    private String remark;

}