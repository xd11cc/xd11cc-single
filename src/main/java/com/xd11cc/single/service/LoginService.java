package com.xd11cc.single.service;

import com.xd11cc.single.entity.vo.CaptchaVO;
import com.xd11cc.single.entity.vo.LoginPasswordVO;
import com.xd11cc.single.entity.vo.SocialUserBindVO;
import com.xd11cc.single.entity.vo.UserLoginInfoVO;

import javax.servlet.http.HttpServletResponse;

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

    /**
     * 第三方授权登录，获取重定向地址
     * @param source
     * @return
     */
    String getRedirectUri(String source);

    /**
     * 授权成功，三方回调
     * @param source
     * @param code
     * @param state
     * @return
     */
    void callback(String source, String code, String state, HttpServletResponse response);

    /**
     * 社交用户绑定
     * @param socialUserBindVO
     * @return
     */
    String socialUserBind(SocialUserBindVO socialUserBindVO);
}

