package com.xd11cc.single.constants;

/**
 * @Author: xd11cc
 * @Date: 2025/6/16 21:31
 **/
public class CacheConstants {

    /**
     * 自动识别json对象反序列化白名单配置
     */
    public static final String[] JSON_WHITE_LIST = {"org.springframework", "com.xd11cc.single"};

    /**
     * 登录用户令牌
     */
    public static final String LOGIN_TOKEN_KEY = "login_tokens:";
}
