package com.xd11cc.single.entity.vo;

import com.xd11cc.single.entity.base.BasePageVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author xd11cc
 * @date 2026-05-28
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel("操作日志查询对象")
public class SystemOperateLogQueryVO extends BasePageVO {

    @ApiModelProperty("操作模块")
    private String module;

    @ApiModelProperty("操作类型")
    private String operateType;

    @ApiModelProperty("操作状态")
    private String status;
}
