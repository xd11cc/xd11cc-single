package com.xd11cc.single.entity.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * @author xd11cc
 * @date 2026-05-26
 */
@Data
@ApiModel("用户新增对象")
public class SystemUserAddVO {

    @ApiModelProperty("用户名")
    @NotBlank(message = "用户名不能为空")
    @Size(max = 30, message = "用户名不能超过30个字符")
    private String username;

    @ApiModelProperty("密码")
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度在6到20个字符之间")
    private String password;

    @ApiModelProperty("昵称")
    @NotBlank(message = "昵称不能为空")
    @Size(max = 30, message = "昵称不能超过30个字符")
    private String nickname;

    @ApiModelProperty("手机号码")
    @Size(max = 11, message = "手机号码不能超过11个字符")
    private String phone;

    @ApiModelProperty("邮箱")
    @Size(max = 50, message = "邮箱不能超过50个字符")
    private String email;

    @ApiModelProperty("性别，字典类型system_user_sex")
    private String sex;

    @ApiModelProperty("部门id")
    private Long deptId;

    @ApiModelProperty("部门名称")
    private String deptName;

    @ApiModelProperty("岗位id")
    private Long postId;

    @ApiModelProperty("岗位名称")
    private String postName;

    @ApiModelProperty("角色id列表")
    @NotNull(message = "角色不能为空")
    private List<Long> roleIds;

    @ApiModelProperty("状态")
    private String status;

    @ApiModelProperty("备注")
    private String remark;
}
