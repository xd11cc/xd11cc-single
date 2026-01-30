package com.xd11cc.single.entity.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.xd11cc.single.entity.base.BaseTenantDO;
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
public class SystemDictDataDO extends BaseTenantDO {

    @ApiModelProperty("主键id")
    private Long id;

    @ApiModelProperty("字典类型")
    private String dictType;

    @ApiModelProperty("标签")
    private String label;

    @ApiModelProperty("键值")
    private String value;

    @ApiModelProperty("css样式")
    private String cssClass;

    @ApiModelProperty("列表样式")
    private String listClass;

    @ApiModelProperty("排序")
    private Integer sort;

    @ApiModelProperty("备注")
    private String remark;

    @ApiModelProperty("状态，字典system_status")
    private String status;

}
