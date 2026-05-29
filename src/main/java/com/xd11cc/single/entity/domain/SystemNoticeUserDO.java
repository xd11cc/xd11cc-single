package com.xd11cc.single.entity.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.xd11cc.single.entity.base.BaseTenantDO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * @author xd11cc
 * @date 2026-05-29
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("system_notice_user")
@ApiModel(value = "SystemNoticeUserDO", description = "系统通知用户关联")
public class SystemNoticeUserDO extends BaseTenantDO implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @ApiModelProperty("主键id")
    private Long id;

    @ApiModelProperty("通知id")
    private Long noticeId;

    @ApiModelProperty("用户id")
    private Long userId;

    @ApiModelProperty("已读状态 0-未读 1-已读")
    private Integer readStatus;

    @ApiModelProperty("已读时间")
    private Date readTime;
}
