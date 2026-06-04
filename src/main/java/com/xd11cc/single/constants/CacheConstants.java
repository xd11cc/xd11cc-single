package com.xd11cc.single.constants;

/**
 * @Author: xd11cc
 * @Date: 2025/6/16 21:31
 **/
public class CacheConstants {

    /**
     * 自动识别json对象反序列化白名单配置
     */
    public static final String[] JSON_WHITE_LIST = {
            "org.springframework", "com.xd11cc.single", "me.zhyd.oauth"
    };

    /**
     * 登录用户令牌
     */
    public static final String LOGIN_TOKEN_KEY = "login_tokens:";

    /**
     * 登录用户
     */
    public static final String LOGIN_USER_KEY = "login_users:";

    /**
     * 登录密码错误次数
     */
    public static final String PASSWORD_ERROR_COUNT_KEY = "password_error_count:";

    /**
     * 接口限流key
     */
    public static final String RATE_LIMIT_KEY = "rate_limit:";

    /**
     * 租户域名key
     */
    public static final String TENANT_DOMAIN_KEY = "tenant_domain";

    /**
     * 字典数据key
     */
    public static final String DICT_TYPE_KEY = "dict_type:";

    /**
     * 验证码key
     */
    public static final String CAPTCHA_KEY = "captcha:";

    /**
     * 授权验证key
     */
    public static final String AUTH_STATE_KEY = "auth_state:";

    /**
     * 系统配置key
     */
    public static final String SYSTEM_CONFIG_KEY = "system_config:";

    /**
     * 通知未读数key
     */
    public static final String NOTICE_UNREAD_COUNT_KEY = "notice_unread_count:";

    /**
     * 分布式锁 key 前缀
     */
    public static final String REDLOCK_KEY_PREFIX = "redlock:";
}
