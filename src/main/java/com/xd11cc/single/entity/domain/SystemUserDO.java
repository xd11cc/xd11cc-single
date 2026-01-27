package com.xd11cc.single.entity.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.xd11cc.single.entity.base.BaseDO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author: xd11cc
 * @Date: 2025/6/13 23:10
 **/
@Data
@ApiModel("用户")
@TableName("system_user")
public class SystemUserDO extends BaseDO {

    @ApiModelProperty("主键id")
    private Long id;

    @ApiModelProperty("用户名")
    private String username;

    @ApiModelProperty("密码")
    private String password;

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

    @ApiModelProperty("部门id")
    private Long deptId;

    @ApiModelProperty("部门名称")
    private String deptName;

    @ApiModelProperty("岗位id")
    private Long postId;

    @ApiModelProperty("岗位名称")
    private String postName;

    @ApiModelProperty("账号状态，字典类型system_status")
    private Integer status;

    @ApiModelProperty("头像路径")
    private String headUrl;

    @ApiModelProperty("备注")
    private String remark;

    public static boolean isAdmin(Long userId) {
        return null != userId && userId == 1L;
    }
}
