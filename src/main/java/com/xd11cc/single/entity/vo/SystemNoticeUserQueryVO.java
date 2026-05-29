package com.xd11cc.single.entity.vo;

import com.xd11cc.single.entity.base.BasePageVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author xd11cc
 * @date 2026-05-29
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel("我的通知查询")
public class SystemNoticeUserQueryVO extends BasePageVO {

    @ApiModelProperty("类型 1-通知 2-消息 3-待办")
    private Integer type;

    @ApiModelProperty("已读状态 0-未读 1-已读")
    private Integer readStatus;

    @ApiModelProperty("标题关键字")
    private String title;
}
