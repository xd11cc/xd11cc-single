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
@ApiModel("登录日志查询对象")
public class SystemLoginLogQueryVO extends BasePageVO {

    @ApiModelProperty("登录账号")
    private String username;

    @ApiModelProperty("登录类型")
    private String loginType;

    @ApiModelProperty("登录状态")
    private String status;
}
