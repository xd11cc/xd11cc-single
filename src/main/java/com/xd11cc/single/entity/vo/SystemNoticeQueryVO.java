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
@ApiModel("通知管理查询")
public class SystemNoticeQueryVO extends BasePageVO {

    @ApiModelProperty("标题")
    private String title;

    @ApiModelProperty("类型 1-通知 2-消息 3-待办")
    private Integer type;

    @ApiModelProperty("状态 0-草稿 1-已发布 2-已撤回")
    private Integer status;
}
