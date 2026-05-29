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
@ApiModel("用户通知列表DTO")
public class SystemNoticeUserDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("关联记录id")
    private Long id;

    @ApiModelProperty("通知id")
    private Long noticeId;

    @ApiModelProperty("已读状态 0-未读 1-已读")
    private Integer readStatus;

    @ApiModelProperty("已读时间")
    private Date readTime;

    @ApiModelProperty("标题")
    private String title;

    @ApiModelProperty("内容")
    private String content;

    @ApiModelProperty("类型 1-通知 2-消息 3-待办")
    private Integer type;

    @ApiModelProperty("发送人姓名")
    private String senderName;

    @ApiModelProperty("发布时间")
    private Date publishTime;
}
