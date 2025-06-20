package com.xd11cc.single.entity.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.xd11cc.single.entity.vo.base.BaseVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author: xd11cc
 * @Date: 2025/6/13 23:46
 **/
@Data
@ApiModel("部门")
@TableName("system_dept")
public class SystemDeptDO extends BaseVO {

    @ApiModelProperty("主键id")
    private Long id;

    @ApiModelProperty("父部门id")
    private String parentId;

    @ApiModelProperty("部门编码")
    private String deptCode;

    @ApiModelProperty("部门名称")
    private String deptName;

    @ApiModelProperty("部门负责人")
    private Long leaderId;

    @ApiModelProperty("排序")
    private Integer sort;

    @ApiModelProperty("部门状态，字典类型system_enable_status")
    private String status;
}
