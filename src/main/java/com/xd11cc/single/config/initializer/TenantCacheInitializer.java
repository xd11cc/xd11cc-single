package com.xd11cc.single.config.initializer;

import com.xd11cc.single.service.ISystemTenantService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * @author xd11cc
 * @date 2026-05-28
 */
@Slf4j
@Component
public class TenantCacheInitializer implements CommandLineRunner {

    @Autowired
    private ISystemTenantService systemTenantService;

    @Override
    public void run(String... args) {
        log.info("开始初始化租户缓存...");
        systemTenantService.refreshCache();
    }
}
