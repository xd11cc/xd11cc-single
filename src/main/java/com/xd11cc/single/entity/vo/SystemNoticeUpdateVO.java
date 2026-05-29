package com.xd11cc.single.entity.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author xd11cc
 * @date 2026-05-29
 */
@Data
@ApiModel("修改通知")
public class SystemNoticeUpdateVO {

    @NotNull(message = "id不能为空")
    @ApiModelProperty("主键id")
    private Long id;

    @NotBlank(message = "标题不能为空")
    @ApiModelProperty("标题")
    private String title;

    @ApiModelProperty("内容")
    private String content;

    @NotNull(message = "类型不能为空")
    @ApiModelProperty("类型 1-通知 2-消息 3-待办")
    private Integer type;

    @NotNull(message = "范围不能为空")
    @ApiModelProperty("范围 1-全部 2-指定部门 3-指定用户")
    private Integer scope;

    @ApiModelProperty("范围部门id列表")
    private List<Long> scopeDeptIds;

    @ApiModelProperty("范围用户id列表")
    private List<Long> scopeUserIds;

    @ApiModelProperty("备注")
    private String remark;
}
