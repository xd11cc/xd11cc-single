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
    public static final String LOGIN_TOKEN = "login_tokens:";

    /**
     * 登录密码错误次数
     */
    public static final String PASSWORD_ERROR_COUNT = "password_error_count:";

    /**
     * 接口限流key
     */
    public static final String RATE_LIMIT = "rate_limit:";

    /**
     * 租户域名key
     */
    public static final String TENANT_DOMAIN = "tenant_domain";
}
