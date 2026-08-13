package com.xd11cc.single.config.initializer;

import com.xd11cc.single.service.ISystemTenantService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class TenantCacheInitializerTest {

    @Mock
    private ISystemTenantService systemTenantService;

    @InjectMocks
    private TenantCacheInitializer tenantCacheInitializer;

    // ==================== run ====================

    @Test
    void run_启动时刷新租户缓存() {
        tenantCacheInitializer.run();

        then(systemTenantService).should(times(1)).refreshCache();
    }

    @Test
    void run_多次调用_每次刷新缓存() {
        tenantCacheInitializer.run();
        tenantCacheInitializer.run();

        then(systemTenantService).should(times(2)).refreshCache();
    }
}
