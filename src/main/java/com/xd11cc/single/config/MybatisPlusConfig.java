package com.xd11cc.single.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import com.xd11cc.single.config.handler.DefaultDBFieldHandler;
import com.xd11cc.single.config.interceptor.TenantDatabaseInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author xd11cc
 * @date 2026-01-23 17:03:55
 * @description
 */
@Configuration
public class MybatisPlusConfig {

    @Autowired
    private TenantDatabaseInterceptor tenantDatabaseInterceptor;

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new TenantLineInnerInterceptor(tenantDatabaseInterceptor));
        return interceptor;
    }

    @Bean
    public MetaObjectHandler metaObjectHandler() {
        // 自动填充参数类
        return new DefaultDBFieldHandler();
    }
}
