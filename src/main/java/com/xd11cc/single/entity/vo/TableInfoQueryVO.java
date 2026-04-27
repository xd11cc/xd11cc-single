package com.xd11cc.single.entity.vo;

import com.xd11cc.single.entity.base.BasePageVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author xd11cc
 * @date 2026-04-22 17:34:30
 * @description
 */
@ApiModel("表信息查询对象")
@Data
public class TableInfoQueryVO extends BasePageVO {

    @ApiModelProperty("表名称")
    private String tableName;

    @ApiModelProperty("表注释")
    private String tableComment;
}
