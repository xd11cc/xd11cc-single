package com.xd11cc.single.entity.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.xd11cc.single.entity.base.BaseDO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author: xd11cc
 * @Date: 2025/6/13 23:54
 **/
@Data
@ApiModel("字典类型")
@TableName("system_dict_type")
public class SystemDictTypeDO extends BaseDO {

    @ApiModelProperty("主键id")
    private Long id;

    @ApiModelProperty("字典类型")
    private String type;

    @ApiModelProperty("字典类型名称")
    private String name;

    @ApiModelProperty("备注")
    private String remark;
}
