package com.xd11cc.single.entity.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author xd11cc
 * @date 2026-01-28 15:37:29
 * @description
 */
@Data
@ApiModel("字典数据新增对象")
public class SystemDictDataAddVO {

    @ApiModelProperty("字典数据类型")
    private String type;

    @ApiModelProperty("标签")
    private String label;

    @ApiModelProperty("键值")
    private String value;

    @ApiModelProperty("键值颜色")
    private String valueColor;

    @ApiModelProperty("背景颜色")
    private String backgroundColor;

    @ApiModelProperty("排序")
    private String sort;

    @ApiModelProperty("备注")
    private String remark;
}
