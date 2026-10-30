package com.xd11cc.single.common.ratelimit;

/**
 * 限流维度：被限的是"谁"。
 *
 * @author xd11cc
 */
public enum Subject {

    /**
     * 请求来源 IP
     */
    IP,

    /**
     * 已登录用户（需接口在认证之后）
     */
    USER,

    /**
     * 接口本身（类名.方法名），全局共享配额
     */
    METHOD,

    /**
     * 由 SpEL 表达式从方法参数中取值，例如 {@code #p0.username}
     */
    EXPRESSION,
}
