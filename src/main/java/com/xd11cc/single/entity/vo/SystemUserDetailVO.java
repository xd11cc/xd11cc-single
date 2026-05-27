package com.xd11cc.single.entity.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author xd11cc
 * @date 2026-05-27
 */
@Data
@ApiModel("用户详情对象")
public class SystemUserDetailVO {

    @ApiModelProperty("主键id")
    private Long id;

    @ApiModelProperty("用户名")
    private String username;

    @ApiModelProperty("昵称")
    private String nickname;

    @ApiModelProperty("性别")
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

    @ApiModelProperty("账号状态")
    private String status;

    @ApiModelProperty("头像路径")
    private String headUrl;

    @ApiModelProperty("备注")
    private String remark;

    @ApiModelProperty("角色id列表")
    private List<Long> roleIds;

    @ApiModelProperty("创建时间")
    private Date createTime;
}
