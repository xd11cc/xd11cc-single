package com.xd11cc.single.constants;

/**
 * @Author: xd11cc
 * @Date: 2025/6/16 15:51
 **/
public class SecurityConstants {

    /**
     * 生成token密钥
     */
    public static final String TOKEN_SECRET = "SKCW2dMYWJS3M8upfECJGBXdmXjA7Qet";

    /**
     * token前缀
     */
    public static final String TOKEN_PREFIX = "Bearer ";

    /**
     * 授权
     */
    public static final String AUTHORIZATION = "Authorization";

    /**
     * 获取用户token的key
     */
    public static final String LOGIN_USER_KEY = "login_user_key";

    /**
     * token过期时间1h
     */
    public static final long EXPIRE_TIME = 60 * 60 * 1000;

    /**
     * token过期时间小于20min刷新token
     */
    public static final long EXPIRE_REFRESH_TOKEN_TIME = 20 * 60 * 1000;

    /**
     * 租户id
     */
    public static final String TENANT_ID = "tenant_id";

    /**
     * 用户token
     */
    public static final String USER_TOKEN = "user_token";

}
