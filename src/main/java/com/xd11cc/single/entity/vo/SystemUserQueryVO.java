package com.xd11cc.single.entity.vo;

import com.xd11cc.single.entity.base.BasePageVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author xd11cc
 * @date 2026-05-26
 */
@Data
@ApiModel("用户查询对象")
public class SystemUserQueryVO extends BasePageVO {

    @ApiModelProperty("用户名")
    private String username;

    @ApiModelProperty("昵称")
    private String nickname;

    @ApiModelProperty("手机号码")
    private String phone;

    @ApiModelProperty("账号状态")
    private String status;

    @ApiModelProperty("部门id")
    private Long deptId;
}
