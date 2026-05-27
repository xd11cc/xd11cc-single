package com.xd11cc.single.entity.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * @author xd11cc
 * @date 2026-05-26
 */
@Data
@ApiModel("部门更新对象")
public class SystemDeptUpdateVO {

    @ApiModelProperty("主键id")
    @NotNull(message = "请选择部门")
    private Long id;

    @ApiModelProperty("父部门id")
    private Long parentId;

    @ApiModelProperty("部门编码")
    @NotBlank(message = "部门编码不能为空")
    @Size(max = 50, message = "部门编码不能超过50个字符")
    private String deptCode;

    @ApiModelProperty("部门名称")
    @NotBlank(message = "部门名称不能为空")
    @Size(max = 50, message = "部门名称不能超过50个字符")
    private String deptName;

    @ApiModelProperty("部门负责人")
    private Long leaderId;

    @ApiModelProperty("排序")
    private Integer sort;

    @ApiModelProperty("部门状态，字典类型system_status")
    private String status;

    @ApiModelProperty("备注")
    private String remark;
}
