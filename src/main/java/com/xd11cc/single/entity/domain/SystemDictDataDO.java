package com.xd11cc.single.entity.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.xd11cc.single.entity.base.BaseDO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author: xd11cc
 * @Date: 2025/6/13 23:59
 **/
@Data
@ApiModel("字典数据")
@TableName("system_dict_data")
public class SystemDictDataDO extends BaseDO {

    @ApiModelProperty("主键id")
    private Long id;

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
