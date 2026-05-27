package com.xd11cc.single.entity.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author xd11cc
 * @date 2026-05-26
 */
@Data
@ApiModel("部门查询对象")
public class SystemDeptQueryVO {

    @ApiModelProperty("部门名称")
    private String deptName;

    @ApiModelProperty("部门状态")
    private String status;
}
