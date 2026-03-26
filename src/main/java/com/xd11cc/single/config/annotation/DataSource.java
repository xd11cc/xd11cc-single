package com.xd11cc.single.config.annotation;

import com.xd11cc.single.enums.DataSourceEnum;

import java.lang.annotation.*;

/**
 * @Author: xd11cc
 * @Date: 2025/6/27 10:32
 *
 * 优先级：先方法，后类。如果方法覆盖了类上的数据源，以方法为准，否则以类上为准
 **/
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface DataSource {

    DataSourceEnum value() default DataSourceEnum.MASTER;
}
