package com.xd11cc.single.entity.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@ApiModel("Flowable流程分类更新对象")
public class WorkflowCategoryUpdateVO {

    @ApiModelProperty("主键id")
    @NotNull(message = "请选择流程分类")
    private Long id;

    @ApiModelProperty("分类编码")
    @NotBlank(message = "分类编码不能为空")
    @Size(max = 64, message = "分类编码不能超过64个字符")
    private String categoryCode;

    @ApiModelProperty("分类名称")
    @NotBlank(message = "分类名称不能为空")
    @Size(max = 100, message = "分类名称不能超过100个字符")
    private String categoryName;

    @ApiModelProperty("显示排序")
    private Integer sort = 0;

    @ApiModelProperty("状态：0正常，1停用")
    @NotBlank(message = "状态不能为空")
    private String status = "0";

    @ApiModelProperty("备注")
    @Size(max = 255, message = "备注不能超过255个字符")
    private String remark;
}
