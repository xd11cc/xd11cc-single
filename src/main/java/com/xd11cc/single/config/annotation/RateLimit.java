package com.xd11cc.single.config.annotation;

import com.xd11cc.single.enums.RateLimitEnum;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * @Author: xd11cc
 * @Date: 2025/6/27 20:27
 **/
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {

    /**
     * 限流前缀
     * @return
     */
    String key() default "default:";

    /**
     * 时间窗口大小
     * @return
     */
    int time() default 60;

    /**
     * 时间单位
     * @return
     */
    TimeUnit timeUnit() default TimeUnit.SECONDS;

    /**
     * 允许请求数
     * @return
     */
    int count() default 100;

    /**
     * 限流类型
     * @return
     */
    RateLimitEnum type() default RateLimitEnum.DEFAULT;

    /**
     * 限流提示信息
     * @return
     */
    String message() default "请求过于频繁，请稍后重试";
}
