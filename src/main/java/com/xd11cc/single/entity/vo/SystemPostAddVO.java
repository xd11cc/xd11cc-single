package com.xd11cc.single.entity.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * @author xd11cc
 * @date 2026-05-27
 */
@Data
@ApiModel("岗位新增对象")
public class SystemPostAddVO {

    @ApiModelProperty("岗位编码")
    @NotBlank(message = "岗位编码不能为空")
    @Size(max = 20, message = "岗位编码不能超过20个字符")
    private String postCode;

    @ApiModelProperty("岗位名称")
    @NotBlank(message = "岗位名称不能为空")
    @Size(max = 20, message = "岗位名称不能超过20个字符")
    private String postName;

    @ApiModelProperty("状态（0正常 1停用）")
    @NotBlank(message = "状态不能为空")
    private String status;

    @ApiModelProperty("部门id列表")
    @NotNull(message = "所属部门不能为空")
    private List<Long> deptIds;

    @ApiModelProperty("备注")
    private String remark;
}
