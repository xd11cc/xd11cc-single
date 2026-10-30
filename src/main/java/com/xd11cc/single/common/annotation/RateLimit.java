package com.xd11cc.single.common.annotation;

import com.xd11cc.single.common.ratelimit.Subject;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 接口限流。
 * <p>
 * 表达的是「什么维度、多少时间、允许多少次」，不涉及任何 Redisson 细节。
 * 多个维度同时存在时，<b>任一维度超限即拒绝</b>，且按声明顺序短路评估。
 * </p>
 * <p>
 * <b>注意：只在 Spring 代理的外部调用上生效。</b>同类内部自调用会绕过 AOP，
 * 限流不会执行——需要限流的内部路径请直接调用
 * {@link com.xd11cc.single.common.ratelimit.RateLimiter}。
 * </p>
 * <p>
 * 超限时抛 {@code ServiceException(TOO_MANY_REQUESTS)}，响应体为 HTTP 200 + 业务码，
 * 前端按 code 判断。Redis 故障时按 fail-closed 处理（拒绝），保护而非放行。
 * </p>
 *
 * @author xd11cc
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {

    /**
     * 窗口内允许的次数
     */
    int permits() default 10;

    /**
     * 窗口长度（秒）
     */
    int windowSeconds() default 60;

    /**
     * 限流维度，可多选
     */
    Subject[] subject() default {Subject.METHOD};

    /**
     * subject 含 {@link Subject#EXPRESSION} 时必填，如 {@code #p0.username}
     */
    String expression() default "";
}
