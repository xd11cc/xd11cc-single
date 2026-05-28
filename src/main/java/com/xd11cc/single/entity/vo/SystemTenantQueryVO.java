package com.xd11cc.single.entity.vo;

import com.xd11cc.single.entity.base.BasePageVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author xd11cc
 * @date 2026-05-28
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel("租户查询")
public class SystemTenantQueryVO extends BasePageVO {

    @ApiModelProperty("租户名称")
    private String name;

    @ApiModelProperty("状态")
    private String status;
}
