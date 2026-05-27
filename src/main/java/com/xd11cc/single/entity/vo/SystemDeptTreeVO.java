package com.xd11cc.single.entity.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author xd11cc
 * @date 2026-05-26
 */
@Data
@ApiModel("部门树形结构")
public class SystemDeptTreeVO {

    @ApiModelProperty("主键id")
    private Long id;

    @ApiModelProperty("父部门id")
    private Long parentId;

    @ApiModelProperty("部门编码")
    private String deptCode;

    @ApiModelProperty("部门名称")
    private String deptName;

    @ApiModelProperty("部门负责人")
    private Long leaderId;

    @ApiModelProperty("排序")
    private Integer sort;

    @ApiModelProperty("部门状态，字典类型system_status")
    private String status;

    @ApiModelProperty("备注")
    private String remark;

    @ApiModelProperty("创建时间")
    private Date createTime;

    @ApiModelProperty("子部门")
    private List<SystemDeptTreeVO> children;
}
