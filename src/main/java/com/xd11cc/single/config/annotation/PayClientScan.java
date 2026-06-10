package com.xd11cc.single.config.annotation;

import com.xd11cc.single.config.pay.PayClientScannerRegistrar;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @author xd11cc
 * @date 2026-06-09 17:25:18
 * @description
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import(PayClientScannerRegistrar.class)
public @interface PayClientScan {

    /**
     * 扫描的包路径，支持多个
     */
    String[] basePackages() default {};
}
