package com.xd11cc.single.entity.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.xd11cc.single.entity.base.BaseTenantDO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * MQ 消费失败消息。
 *
 * @author xd11cc
 */
@Data
@TableName("mq_fail_message")
@ApiModel("MQ消费失败消息")
public class MqFailMessageDO extends BaseTenantDO implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    @ApiModelProperty("主键id")
    private Long id;

    @ApiModelProperty("消息唯一标识")
    private String messageId;

    @ApiModelProperty("来源队列名")
    private String queueName;

    @ApiModelProperty("原始业务消息JSON")
    private String data;

    @ApiModelProperty("消息体全限定类名（Jackson __TypeId__），重放时用于还原类型")
    private String messageType;

    @ApiModelProperty("最后一次异常摘要")
    private String errorMsg;

    @ApiModelProperty("首次失败时的实际重试次数")
    private Integer retryCount;

    @ApiModelProperty("状态：0-待处理，1-已重放，2-已忽略")
    private String status;
}
