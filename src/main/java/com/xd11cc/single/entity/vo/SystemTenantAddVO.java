package com.xd11cc.single.entity.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * @author xd11cc
 * @date 2026-05-28
 */
@Data
@ApiModel("新增租户")
public class SystemTenantAddVO {

    @NotBlank(message = "租户名称不能为空")
    @ApiModelProperty("租户名称")
    private String name;

    @NotBlank(message = "绑定域名不能为空")
    @ApiModelProperty("绑定域名")
    private String domain;

    @ApiModelProperty("联系人")
    private String contactName;

    @ApiModelProperty("联系电话")
    private String contactPhone;

    @ApiModelProperty("账号额度（0=不限）")
    private Integer accountCount;

    @ApiModelProperty("状态 0-正常 1-停用")
    private String status;

    @NotNull(message = "过期时间不能为空")
    @ApiModelProperty("过期时间")
    private Date expireTime;
}
