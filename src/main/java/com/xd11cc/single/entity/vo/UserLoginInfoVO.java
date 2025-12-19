package com.xd11cc.single.entity.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author xd11cc
 * @date 2025-12-18 11:29:47
 */
@Data
public class
UserLoginInfoVO {

    @ApiModelProperty("主键id")
    private Long id;

    @ApiModelProperty("用户名")
    private String username;

    @ApiModelProperty("昵称")
    private String nickname;

    @ApiModelProperty("身份证")
    private String idCard;

    @ApiModelProperty("性别，字典类型system_user_sex")
    private String sex;

    @ApiModelProperty("手机号码")
    private String phone;

    @ApiModelProperty("邮箱")
    private String email;

    @ApiModelProperty("头像路径")
    private String headUrl;

    @ApiModelProperty("用户菜单信息")
    private List<TreeMenuVO> treeMenuVOS;

}
