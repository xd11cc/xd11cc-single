package com.xd11cc.single.service;

import com.xd11cc.single.entity.dto.LoginUserDTO;

import javax.servlet.http.HttpServletRequest;

/**
 * @Author: xd11cc
 * @Date: 2025/6/16 09:21
 **/
public interface TokenService {

    /**
     * 获取用户身份信息
     * @param request
     * @return
     */
    LoginUserDTO getLoginUser(HttpServletRequest request);

    /**
     * 验证token有效期，刷新令牌
     * @param loginUserDTO
     */
    void verifyToken(LoginUserDTO loginUserDTO);

    /**
     * 移除token，移除用户信息
     * @param token
     */
    void removeLoginUser(String token);
}
