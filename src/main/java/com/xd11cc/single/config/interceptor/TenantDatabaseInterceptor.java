package com.xd11cc.single.config.interceptor;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import com.xd11cc.single.config.context.TenantContextHolder;
import com.xd11cc.single.config.properties.TenantIgnoreProperties;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author xd11cc
 * @date 2026-01-23 16:56:23
 * @description 租户拦截器
 */
@Component
public class TenantDatabaseInterceptor implements TenantLineHandler {

    @Autowired
    private TenantIgnoreProperties tenantIgnoreProperties;

    @Override
    public Expression getTenantId() {
        return new LongValue(TenantContextHolder.getRequiredTenantId());
    }

    @Override
    public boolean ignoreTable(String tableName) {
        return TenantContextHolder.isIgnore() ||
                CollUtil.contains(tenantIgnoreProperties.getIgnoreTables(), tableName);
    }
}
