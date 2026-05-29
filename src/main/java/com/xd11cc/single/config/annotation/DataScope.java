package com.xd11cc.single.config.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author xd11cc
 * @date 2026-05-28
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DataScope {

    /**
     * 表别名，设置后自动拼接为 alias.deptColumn / alias.userColumn
     */
    String alias() default "";

    /**
     * 部门字段名（SQL列名）
     */
    String deptColumn() default "dept_id";

    /**
     * 用户字段名（SQL列名），用于 SELF 模式
     */
    String userColumn() default "create_user_id";
}
