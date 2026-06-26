package com.xd11cc.single.entity.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * @author xd11cc
 * @date 2026-06-11
 */
@Data
@ApiModel("系统任务新增对象")
public class SystemJobAddVO {

    @NotBlank(message = "任务名称不能为空")
    @Size(max = 64, message = "任务名称不能超过64个字符")
    @ApiModelProperty("任务名称")
    private String jobName;

    @NotBlank(message = "任务组名不能为空")
    @Size(max = 64, message = "任务组名不能超过64个字符")
    @ApiModelProperty("任务组名")
    private String jobGroup;

    @NotBlank(message = "调用目标不能为空")
    @Size(max = 500, message = "调用目标不能超过500个字符")
    @ApiModelProperty("调用目标")
    private String invokeTarget;

    @NotBlank(message = "Cron表达式不能为空")
    @Size(max = 255, message = "Cron表达式不能超过255个字符")
    @ApiModelProperty("Cron表达式")
    private String cronExpression;

    @ApiModelProperty("执行策略 1-默认 2-立即执行 3-忽略")
    private String executionPolicy;

    @ApiModelProperty("是否允许并发 0-不允许 1-允许")
    private String concurrent;

    @ApiModelProperty("状态 0-正常 1-暂停")
    private String status;

    @ApiModelProperty("备注")
    private String remark;
}
