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
     * 限流维度，可多选；与 {@link #argField()} 可组合
     */
    Subject[] subject() default {Subject.METHOD};

    /**
     * 额外按「请求内容」限流：填写参数名或参数对象的字段名，例如 {@code "username"}、{@code "phone"}。
     * <p>
     * 与 {@link #subject()} 是两个独立的轴：subject 限「谁在调用」（IP / 用户 / 接口），
     * argField 限「调用的是谁的数据」（登录用户名、手机号、订单号……）。
     * 只要填了，就多一个按该值计数的桶。
     * </p>
     * <p>
     * 留空表示不按请求内容限流。配置是否正确由启动期的
     * {@code RateLimitConfigValidator} 校验。
     * </p>
     */
    String argField() default "";
}
