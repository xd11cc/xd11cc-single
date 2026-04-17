package com.xd11cc.single.service;

import com.xd11cc.single.entity.vo.CaptchaVO;
import com.xd11cc.single.entity.vo.LoginPasswordVO;
import com.xd11cc.single.entity.vo.UserLoginInfoVO;

/**
 * @Author: xd11cc
 * @Date: 2025/6/23 21:55
 **/
public interface LoginService {

    /**
     * 账号密码登录
     * @param loginPasswordVO
     * @return
     */
    String loginByPassword(LoginPasswordVO loginPasswordVO);

    /**
     * 获取当前用户登录信息
     * @param userId
     * @return
     */
    UserLoginInfoVO getUserLoginInfo(Long userId);

    /**
     * 获取验证码
     * @return
     */
    CaptchaVO getCaptcha();

}
