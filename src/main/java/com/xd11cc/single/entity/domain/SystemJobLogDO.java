package com.xd11cc.single.entity.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.xd11cc.single.entity.vo.base.BaseVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author xd11cc
 * @date 2025-07-22 18:53:22
 */
@Data
@ApiModel("系统任务日志")
@TableName("system_job_log")
public class SystemJobLogDO extends BaseVO {

    @ApiModelProperty("主键id")
    private Long id;

    @ApiModelProperty("任务id")
    private Long jobId;

    @ApiModelProperty("任务名称")
    private String jobName;

    @ApiModelProperty("任务组")
    private String jobGroup;

    @ApiModelProperty("任务日志")
    private String jobMessage;

    @ApiModelProperty("状态")
    private String status;
}
