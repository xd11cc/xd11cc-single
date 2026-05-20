package com.xd11cc.single.entity.vo;

import com.xd11cc.single.entity.base.BasePageVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author xd11cc
 * @date 2026-05-20
 */
@Data
@ApiModel("系统配置查询对象")
public class SystemConfigQueryVO extends BasePageVO {

    @ApiModelProperty("配置键")
    private String configKey;

    @ApiModelProperty("配置名称")
    private String configName;
}
