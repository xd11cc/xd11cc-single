package com.xd11cc.single.entity.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author xd11cc
 * @date 2026-05-29
 */
@Data
@ApiModel("WebSocket通知推送DTO")
public class NoticeWebSocketDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("动作 NEW_NOTICE/REVOKE_NOTICE")
    private String action;

    @ApiModelProperty("通知id")
    private Long noticeId;

    @ApiModelProperty("标题")
    private String title;

    @ApiModelProperty("类型 1-通知 2-消息 3-待办")
    private Integer type;

    @ApiModelProperty("发送人姓名")
    private String senderName;

    @ApiModelProperty("发送时间")
    private Date sendTime;
}
