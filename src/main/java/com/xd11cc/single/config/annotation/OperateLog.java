package com.xd11cc.single.config.annotation;

import com.xd11cc.single.enums.OperateTypeEnum;

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
public @interface OperateLog {

    /**
     * 操作模块
     */
    String module() default "";

    /**
     * 操作类型
     */
    OperateTypeEnum operateType() default OperateTypeEnum.OTHER;

    /**
     * 操作描述（为空时取@ApiOperation的值）
     */
    String operateDesc() default "";

    /**
     * 是否保存请求参数
     */
    boolean saveRequestParam() default true;

    /**
     * 是否保存响应结果
     */
    boolean saveResponseResult() default true;
}
