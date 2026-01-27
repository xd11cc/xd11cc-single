package com.xd11cc.single.config.context;

import com.alibaba.ttl.TransmittableThreadLocal;

/**
 * @Author: xd11cc
 * @Date: 2025/6/27 11:31
 *
 * 多数据源上下文
 **/
public class DynamicDataSourceContextHolder {

    private static final ThreadLocal<String> CONTEXT_HOLDER = new TransmittableThreadLocal<>();

    public static void setDataSourceType(String dataSourceType) {
        CONTEXT_HOLDER.set(dataSourceType);
    }

    public static String getDataSourceType() {
        return CONTEXT_HOLDER.get();
    }

    public static void clearDataSourceType() {
        CONTEXT_HOLDER.remove();
    }
}
