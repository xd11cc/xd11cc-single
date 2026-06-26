package com.xd11cc.single.entity.vo;

import com.xd11cc.single.entity.base.BasePageVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author xd11cc
 * @date 2026-06-11
 */
@Data
@ApiModel("系统任务查询对象")
public class SystemJobQueryVO extends BasePageVO {

    @ApiModelProperty("任务名称")
    private String jobName;

    @ApiModelProperty("任务组名")
    private String jobGroup;

    @ApiModelProperty("状态")
    private String status;
}
