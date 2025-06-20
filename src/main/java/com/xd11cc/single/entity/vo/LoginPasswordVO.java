package com.xd11cc.single.entity.vo;

import com.xd11cc.single.entity.vo.base.BaseLoginVO;
import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * @Author: xd11cc
 * @Date: 2025/6/14 00:39
 **/
@Data
@ApiModel("密码登录对象")
public class LoginPasswordVO extends BaseLoginVO {

    private String username;

    private String password;

}
