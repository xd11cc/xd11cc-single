package com.xd11cc.single.service;

import com.xd11cc.single.entity.vo.SocialUserBindVO;
import me.zhyd.oauth.model.AuthUser;

import javax.servlet.http.HttpServletResponse;

/**
 * @Author: xd11cc
 * @Date: 2025/6/23 21:55
 *
 * 社交登录服务
 **/
public interface SocialLoginService {

    /**
     * @param source 认证来源
     * @return 授权地址
     */
    String getRedirectUri(String source);

    /**
     * @param source 认证来源
     * @param code 授权码
     * @param state 状态
     * @param response 响应
     */
    void callback(String source, String code, String state, HttpServletResponse response);

    /**
     * @param socialUserBindVO 绑定参数
     * @return token
     */
    String socialUserBind(SocialUserBindVO socialUserBindVO);
}
