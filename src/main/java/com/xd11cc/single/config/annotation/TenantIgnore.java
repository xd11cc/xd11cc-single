package com.xd11cc.single.config.annotation;

import java.lang.annotation.*;

/**
 * @author xd11cc
 * @date 2026-01-23 17:21:34
 * @description 忽略租户，标记指定方法不进行租户的自动过滤
 * 注：只有DB的场景会过滤，其他场景暂时不过滤
 * 1、Redis场景：基于 Key 实现多租户的能力，所以忽略没有意义，不像 DB 是一个 column 实现的
 * 2、MQ场景：todo
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface TenantIgnore {
}
