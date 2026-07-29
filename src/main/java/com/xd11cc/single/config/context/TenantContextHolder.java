package com.xd11cc.single.config.context;

import com.alibaba.ttl.TransmittableThreadLocal;
import com.xd11cc.single.config.exception.ServiceException;
import com.xd11cc.single.enums.SystemErrorEnum;

import java.util.function.Supplier;

/**
 * @author xd11cc
 * @date 2026-01-23 16:28:01
 * @description 多租户上下文
 */
public class TenantContextHolder {

    private static final ThreadLocal<Long> TENANT_ID = new TransmittableThreadLocal<>();

    private static final ThreadLocal<Boolean> IGNORE = new TransmittableThreadLocal<>();

    private static final ThreadLocal<Boolean> TENANT_AWARE = new TransmittableThreadLocal<Boolean>() {
        @Override
        protected Boolean initialValue() {
            return Boolean.TRUE;
        }
    };

    public static Long getTenantId() {
        return TENANT_ID.get();
    }

    public static Long getRequiredTenantId() {
        Long tenantId = getTenantId();
        if (tenantId == null) {
            throw new ServiceException(SystemErrorEnum.NOT_FOUND_TENANT);
        }
        return tenantId;
    }

    public static void setTenantId(Long tenantId) {
        TENANT_ID.set(tenantId);
    }

    public static void setIgnore(Boolean ignore) {
        IGNORE.set(ignore);
    }

    public static boolean isIgnore() {
        return Boolean.TRUE.equals(IGNORE.get());
    }

    public static boolean isTenantAware() {
        return Boolean.TRUE.equals(TENANT_AWARE.get());
    }

    /**
     * @param aware 是否自动拼接租户 ID
     */
    public static void setTenantAware(boolean aware) {
        TENANT_AWARE.set(aware);
    }

    /**
     * @param runnable 需要关闭租户 key 拼接的逻辑
     */
    public static void runWithoutTenantAwareness(Runnable runnable) {
        boolean wasAware = Boolean.TRUE.equals(TENANT_AWARE.get());
        TENANT_AWARE.set(false);
        try {
            runnable.run();
        } finally {
            TENANT_AWARE.set(wasAware);
        }
    }

    /**
     * @param supplier 需要关闭租户 key 拼接的逻辑
     * @param <T> 返回值类型
     * @return 返回值
     */
    public static <T> T runWithoutTenantAwareness(Supplier<T> supplier) {
        boolean wasAware = Boolean.TRUE.equals(TENANT_AWARE.get());
        TENANT_AWARE.set(false);
        try {
            return supplier.get();
        } finally {
            TENANT_AWARE.set(wasAware);
        }
    }

    /**
     * @param tenantId 目标租户 ID
     * @param runnable 跨租户查询逻辑
     */
    public static void runAsTenant(Long tenantId, Runnable runnable) {
        Long previousTenantId = TENANT_ID.get();
        TENANT_ID.set(tenantId);
        try {
            runnable.run();
        } finally {
            TENANT_ID.set(previousTenantId);
        }
    }

    /**
     * @param tenantId 目标租户 ID
     * @param supplier 跨租户查询逻辑
     * @param <T> 返回值类型
     * @return 返回值
     */
    public static <T> T runAsTenant(Long tenantId, Supplier<T> supplier) {
        Long previousTenantId = TENANT_ID.get();
        TENANT_ID.set(tenantId);
        try {
            return supplier.get();
        } finally {
            TENANT_ID.set(previousTenantId);
        }
    }

    public static void clear() {
        TENANT_ID.remove();
        IGNORE.remove();
        TENANT_AWARE.remove();
    }
}
