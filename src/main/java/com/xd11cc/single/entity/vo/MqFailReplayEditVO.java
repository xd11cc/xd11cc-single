package com.xd11cc.single.entity.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * MQ 失败消息「编辑后重投」参数。
 *
 * @author xd11cc
 */
@Data
@ApiModel("MQ失败消息编辑重投参数")
public class MqFailReplayEditVO {

    @NotBlank(message = "消息内容不能为空")
    @ApiModelProperty(value = "修改后的消息体 JSON", required = true)
    private String data;
}
