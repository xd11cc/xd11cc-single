package com.xd11cc.single.entity.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * @author xd11cc
 * @date 2026-01-28 15:37:29
 * @description
 */
@Data
@ApiModel("字典数据更新对象")
public class SystemDictDataUpdateVO {

    @ApiModelProperty("主键id")
    @NotNull(message = "请选择字典数据")
    private Long id;

    @ApiModelProperty("字典类型")
    @NotBlank(message = "请选择字典类型")
    private String dictType;

    @ApiModelProperty("标签")
    @NotBlank(message = "标签不能为空")
    @Size(max = 100, message = "标签不能超过100个字符")
    private String label;

    @ApiModelProperty("键值")
    @NotBlank(message = "键值不能为空")
    @Size(max = 100, message = "键值不能超过100个字符")
    private String value;

    @ApiModelProperty("样式属性")
    private String cssClass;

    @ApiModelProperty("表格回显样式")
    private String listClass;

    @ApiModelProperty("状态，system_status")
    private String status;

    @ApiModelProperty("排序")
    private String sort;

    @ApiModelProperty("备注")
    private String remark;
}
