package com.xd11cc.single.service;

import com.xd11cc.single.entity.vo.LoginPasswordVO;
import com.xd11cc.single.entity.vo.LoginResultVO;

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
    LoginResultVO loginByPassword(LoginPasswordVO loginPasswordVO);
}
