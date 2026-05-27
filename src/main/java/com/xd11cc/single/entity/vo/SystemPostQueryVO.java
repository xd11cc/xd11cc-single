package com.xd11cc.single.entity.vo;

import com.xd11cc.single.entity.base.BasePageVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author xd11cc
 * @date 2026-05-27
 */
@Data
@ApiModel("岗位查询对象")
public class SystemPostQueryVO extends BasePageVO {

    @ApiModelProperty("岗位编码")
    private String postCode;

    @ApiModelProperty("岗位名称")
    private String postName;

    @ApiModelProperty("状态")
    private String status;
}
