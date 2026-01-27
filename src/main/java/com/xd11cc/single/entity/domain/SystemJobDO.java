package com.xd11cc.single.entity.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.xd11cc.single.entity.base.BaseDO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author xd11cc
 * @date 2025-07-22 18:50:43
 */
@Data
@ApiModel("系统任务")
@TableName("system_job")
public class SystemJobDO extends BaseDO {

    @ApiModelProperty("主键id")
    private Long id;

    @ApiModelProperty("任务名称")
    private String jobName;

    @ApiModelProperty("任务组")
    private String jobGroup;

    @ApiModelProperty("调用目标")
    private String invokeTarget;

    @ApiModelProperty("corn表达式")
    private String cronExpression;

    @ApiModelProperty("执行策略")
    private String executionPolicy;

    @ApiModelProperty("是否并发执行")
    private String concurrent;

    @ApiModelProperty("状态")
    private String status;

    @ApiModelProperty("备注")
    private String remark;
}
