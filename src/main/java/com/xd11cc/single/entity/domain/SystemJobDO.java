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
@TableName("system_job")
@ApiModel(value = "SystemJobDO", description = "系统任务对象")
public class SystemJobDO extends BaseTenantDO implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    @ApiModelProperty(value = "主键id", required = true)
    private Long id;

    @ApiModelProperty(value = "任务名称", required = true)
    private String jobName;

    @ApiModelProperty(value = "任务组名", required = true)
    private String jobGroup;

    @ApiModelProperty(value = "调度目标字符串", required = true)
    private String invokeTarget;

    @ApiModelProperty(value = "cron执行表达式", required = true)
    private String cronExpression;

    @ApiModelProperty(value = "执行策略", required = true)
    private String executionPolicy;

    @ApiModelProperty(value = "是否允许并发执行", required = true)
    private String concurrent;

    @ApiModelProperty(value = "状态", required = true)
    private String status;

    @ApiModelProperty(value = "备注", required = false)
    private String remark;

}