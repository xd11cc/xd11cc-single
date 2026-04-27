package com.xd11cc.single.entity.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * @author xd11cc
 * @date 2026-04-22 16:24:41
 * @description
 */
@Data
@ApiModel("表信息")
public class TableInfoVO {

    @ApiModelProperty("表名称")
    private String tableName;

    @ApiModelProperty("表注释")
    private String tableComment;

    @ApiModelProperty("创建日期")
    private Date createTime;
}
