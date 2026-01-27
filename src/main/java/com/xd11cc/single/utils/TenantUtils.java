package com.xd11cc.single.utils;

import com.xd11cc.single.config.context.TenantContextHolder;

import java.util.function.Supplier;

/**
 * @author xd11cc
 * @date 2026-01-26 17:49:05
 * @description 多租户utils
 */
public class TenantUtils {

    /**
     * 多租户Util
     * 注：如果当前是忽略租户的情况下，会被强制设置成不忽略租户
     * 执行完成后，恢复成原租户
     * @param tenantId 租户编号
     * @param runnable 逻辑
     */
    public static void execute(Long tenantId, Runnable runnable){
        Long oldTenantId = TenantContextHolder.getTenantId();
        Boolean oldIgnore = TenantContextHolder.isIgnore();
        try {
            TenantContextHolder.setTenantId(tenantId);
            TenantContextHolder.setIgnore(false);
            // 执行逻辑
            runnable.run();
        } finally {
            TenantContextHolder.setTenantId(oldTenantId);
            TenantContextHolder.setIgnore(oldIgnore);
        }
    }

    public static <T> T execute(Long tenantId, Supplier<T> supplier){
        Long oldTenantId = TenantContextHolder.getTenantId();
        Boolean oldIgnore = TenantContextHolder.isIgnore();
        try {
            TenantContextHolder.setTenantId(tenantId);
            TenantContextHolder.setIgnore(false);
            // 执行逻辑
            return supplier.get();
        } finally {
            TenantContextHolder.setTenantId(oldTenantId);
            TenantContextHolder.setIgnore(oldIgnore);
        }
    }

    /**
     * 忽略租户，执行对应的逻辑
     * @param tenantId 租户编号
     * @param runnable 逻辑
     */
    public static void executeIgnore(Long tenantId, Runnable runnable){
        Boolean oldIgnore = TenantContextHolder.isIgnore();
        try {
            TenantContextHolder.setIgnore(true);
            // 执行逻辑
            runnable.run();
        }finally {
            TenantContextHolder.setIgnore(oldIgnore);
        }
    }

    public static <T> T executeIgnore(Supplier<T> supplier){
        Boolean oldIgnore = TenantContextHolder.isIgnore();
        try {
            TenantContextHolder.setIgnore(true);
            // 执行逻辑
            return supplier.get();
        } finally {
            TenantContextHolder.setIgnore(oldIgnore);
        }
    }

    /**
     * 执行完毕后删除租户上下文
     * 适用于没有上下文，却想要去执行依赖于上下文的方法，如在authFilter中获取登录的租户
     * @param tenantId
     * @param runnable
     */
    public static void executeAndClear(Long tenantId, Runnable runnable){
        try {
            TenantContextHolder.setTenantId(tenantId);
            // 执行逻辑
            runnable.run();
        } finally {
            TenantContextHolder.clear();
        }
    }

    public static <T> T executeAndClear(Long tenantId, Supplier<T> supplier){
        try {
            TenantContextHolder.setTenantId(tenantId);
            // 执行逻辑
            return supplier.get();
        } finally {
            TenantContextHolder.clear();
        }
    }
}
