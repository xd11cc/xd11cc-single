package com.xd11cc.single.entity.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author xd11cc
 * @date 2026-04-23 14:28:20
 * @description
 */
@ApiModel("表详细信息")
@Data
public class ColumnInfoVO {

    @ApiModelProperty("列名称")
    private String columnName;

    @ApiModelProperty("是否为空")
    private String isNullable;

    @ApiModelProperty("数据类型")
    private String dataType;

    @ApiModelProperty("列注释")
    private String columnComment;

    @ApiModelProperty("键")
    private String columnKey;

    @ApiModelProperty("额外信息")
    private String extra;

    @ApiModelProperty("表注释")
    private String tableComment;
}
