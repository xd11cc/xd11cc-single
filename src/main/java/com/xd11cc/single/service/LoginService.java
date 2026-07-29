package com.xd11cc.single.service;

import com.xd11cc.single.entity.vo.CaptchaVO;
import com.xd11cc.single.entity.vo.LoginPasswordVO;
import com.xd11cc.single.entity.vo.UserLoginInfoVO;

/**
 * @Author: xd11cc
 * @Date: 2025/6/23 21:55
 *
 * 登录服务（账号密码 + 验证码）
 **/
public interface LoginService {

    /**
     * @param loginPasswordVO 登录参数
     * @return token
     */
    String loginByPassword(LoginPasswordVO loginPasswordVO);

    /**
     * @param userId 用户ID
     * @return 登录信息
     */
    UserLoginInfoVO getUserLoginInfo(Long userId);

    /**
     * @return 验证码
     */
    CaptchaVO getCaptcha();
}

