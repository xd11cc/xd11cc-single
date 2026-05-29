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
@TableName("system_notice")
@ApiModel(value = "SystemNoticeDO", description = "系统通知")
public class SystemNoticeDO extends BaseTenantDO implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @ApiModelProperty("主键id")
    private Long id;

    @ApiModelProperty("标题")
    private String title;

    @ApiModelProperty("内容")
    private String content;

    @ApiModelProperty("类型 1-通知 2-消息 3-待办")
    private Integer type;

    @ApiModelProperty("范围 1-全部 2-指定部门 3-指定用户")
    private Integer scope;

    @ApiModelProperty("范围部门id列表，逗号分隔")
    private String scopeDeptIds;

    @ApiModelProperty("范围用户id列表，逗号分隔")
    private String scopeUserIds;

    @ApiModelProperty("发送人id")
    private Long senderId;

    @ApiModelProperty("发送人姓名")
    private String senderName;

    @ApiModelProperty("状态 0-草稿 1-已发布 2-已撤回")
    private Integer status;

    @ApiModelProperty("发布时间")
    private Date publishTime;

    @ApiModelProperty("备注")
    private String remark;
}
