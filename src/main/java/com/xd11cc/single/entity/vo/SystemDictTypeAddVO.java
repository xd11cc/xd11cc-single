package com.xd11cc.single.entity.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * @author xd11cc
 * @date 2026-01-27 15:53:52
 * @description
 */
@Data
@ApiModel("字典新增对象")
public class SystemDictTypeAddVO {

    @ApiModelProperty("字典类型")
    @NotBlank(message = "字典类型不能为空")
    @Pattern(regexp = "^[a-z][a-z0-9_]*$", message = "字典类型必须以字母开头，且只能为（小写字母，数字，下滑线）")
    private String type;

    @ApiModelProperty("字典类型名称")
    @NotBlank(message = "字典名称不能为空")
    @Size(max = 100, message = "字典名称不能超过100个字符")
    private String name;

    @ApiModelProperty("备注")
    @Size(max = 200, message = "备注不能超过200个字符")
    private String remark;
}
