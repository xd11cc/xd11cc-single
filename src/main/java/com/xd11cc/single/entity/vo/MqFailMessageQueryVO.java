package com.xd11cc.single.entity.vo;

import com.xd11cc.single.entity.base.BasePageVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * MQ 失败消息查询参数。
 *
 * @author xd11cc
 */
@EqualsAndHashCode(callSuper = true)
@Data
@ApiModel("MQ失败消息查询参数")
public class MqFailMessageQueryVO extends BasePageVO {

    @ApiModelProperty("消息ID")
    private String messageId;

    @ApiModelProperty("队列名")
    private String queueName;

    @ApiModelProperty("状态：0-待处理，1-已重放，2-已忽略")
    private Integer status;
}
