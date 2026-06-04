package com.xd11cc.single.config.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * @author xd11cc
 * @date 2026-05-30
 * 基于 Redisson 实现的分布式锁注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Lock {

    /**
     * 锁的前缀 key（业务自定义）
     */
    String prefix() default "lock";

    /**
     * 锁 key 的 SpEL 表达式，用于从方法入参中提取锁维度
     * 不设置时，KEY 模式下使用所有入参拼接
     *
     * 示例：
     *   key = "#userId"              → 按 userId 参数加锁
     *   key = "#vo.id"               → 按 vo 对象的 id 字段加锁
     *   key = "#userId + ':' + #vo.id" → 组合多个参数
     */
    String key() default "";

    /**
     * 锁持有时间（超时自动释放，防止死锁）
     */
    long leaseTime() default 0;

    /**
     * 超时时间单位
     */
    TimeUnit unit() default TimeUnit.SECONDS;

    /**
     * 获取锁等待超时时间（0 = 立即返回）
     */
    long waitTime() default 0;

    /**
     * 重试次数（仅在 waitTime > 0 时生效）
     */
    int retryTimes() default 3;

    /**
     * 锁的粒度类型
     * ALL - 全局一把锁（所有参数共享一个锁）
     * KEY - 按 key 分锁（不同 key 不互斥）
     */
    LockMode mode() default LockMode.KEY;

    enum LockMode {
        ALL,
        KEY
    }
}
