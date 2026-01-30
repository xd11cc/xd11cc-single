package com.xd11cc.single.entity.vo;

import com.xd11cc.single.entity.base.BasePageVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author xd11cc
 * @date 2026-01-28 15:37:29
 * @description
 */
@Data
@ApiModel("字典数据查询对象")
public class SystemDictDataQueryVO extends BasePageVO {

    @ApiModelProperty("字典类型")
    @NotBlank(message = "请选择字典类型")
    private String dictType;

    @ApiModelProperty("标签")
    private String label;

    @ApiModelProperty("键值")
    private String value;

}
