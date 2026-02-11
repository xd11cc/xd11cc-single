package com.xd11cc.single.entity.vo;

import com.xd11cc.single.entity.base.BasePageVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author xd11cc
 * @date 2026-02-10 10:58:58
 * @description
 */
@Data
@ApiModel("菜单查询对象")
public class SystemMenuQueryVO extends BasePageVO {

    @ApiModelProperty("菜单名称")
    private String menuName;

    @ApiModelProperty("菜单状态")
    private String status;
}
