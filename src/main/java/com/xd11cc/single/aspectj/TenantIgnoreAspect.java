package com.xd11cc.single.aspectj;

import com.xd11cc.single.annotation.TenantIgnore;
import com.xd11cc.single.config.context.TenantContextHolder;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * @author xd11cc
 * @date 2026-01-23 17:24:44
 * @description 忽略多租户的切面，基于{@link TenantIgnore}注解实现，用于一些全局的逻辑
 * 如：一个定时任务，读取所有数据，进行处理
 *    或缓存所有数据，进行缓存
 * 整体逻辑的实现，与{@link TenantUtils.executeIgnore(Runnable)}需保持一致
 */
@Component
@Aspect
@Order(3)
public class TenantIgnoreAspect {

    @Around("@annotation(tenantIgnore)")
    public Object around(ProceedingJoinPoint joinPoint, TenantIgnore tenantIgnore) throws Throwable {
        Boolean oldIgnore = TenantContextHolder.isIgnore();
        try {
            TenantContextHolder.setIgnore(true);
            // 执行逻辑
            return joinPoint.proceed();
        }finally {
            TenantContextHolder.setIgnore(oldIgnore);
        }
    }
}
