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
@ApiModel("授权应用配置查询对象")
public class AuthClientConfigQueryVO extends BasePageVO {

    @ApiModelProperty("应用类型")
    private String source;

    @ApiModelProperty("应用名称")
    private String name;

    @ApiModelProperty("状态")
    private String status;
}
