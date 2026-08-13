package com.xd11cc.single.config.interceptor;

import com.xd11cc.single.config.context.TenantContextHolder;
import com.xd11cc.single.config.properties.TenantIgnoreProperties;
import net.sf.jsqlparser.expression.Expression;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TenantDatabaseInterceptorTest {

    private final TenantDatabaseInterceptor interceptor = new TenantDatabaseInterceptor();

    @AfterEach
    void tearDown() {
        TenantContextHolder.clear();
    }

    // ==================== getTenantId ====================

    @Test
    void getTenantId_已设置租户ID_返回对应值() {
        TenantContextHolder.setTenantId(10L);

        Expression tenantId = interceptor.getTenantId();

        assertThat(tenantId).isNotNull();
        assertThat(tenantId.toString()).isEqualTo("10");
    }

    @Test
    void getTenantId_未设置租户ID_抛异常() {
        TenantContextHolder.clear();

        try {
            interceptor.getTenantId();
            assertThat(false).as("应抛出异常").isTrue();
        } catch (Exception e) {
            assertThat(e).isInstanceOf(com.xd11cc.single.config.exception.ServiceException.class);
        }
    }

    // ==================== ignoreTable ====================

    @Test
    void ignoreTable_忽略租户模式开启_忽略所有表() throws Exception {
        injectTenantIgnoreProperties(new String[]{"any_table"});

        TenantContextHolder.setIgnore(true);

        boolean result = interceptor.ignoreTable("system_user");

        assertThat(result).isTrue();
    }

    @Test
    void ignoreTable_表在忽略列表_返回true() throws Exception {
        injectTenantIgnoreProperties("system_menu");

        boolean result = interceptor.ignoreTable("system_menu");

        assertThat(result).isTrue();
    }

    @Test
    void ignoreTable_表不在忽略列表_返回false() throws Exception {
        injectTenantIgnoreProperties("system_menu");

        boolean result = interceptor.ignoreTable("system_user");

        assertThat(result).isFalse();
    }

    @Test
    void ignoreTable_忽略列表为空_返回false() throws Exception {
        injectTenantIgnoreProperties();

        boolean result = interceptor.ignoreTable("system_user");

        assertThat(result).isFalse();
    }

    // ==================== 辅助方法 ====================

    @SuppressWarnings("unchecked")
    private void injectTenantIgnoreProperties(String... tableNames) throws Exception {
        TenantIgnoreProperties props = mock(TenantIgnoreProperties.class);
        java.lang.reflect.Field field = TenantDatabaseInterceptor.class.getDeclaredField("tenantIgnoreProperties");
        field.setAccessible(true);
        field.set(interceptor, props);
        when(props.getIgnoreTables()).thenReturn(java.util.Arrays.asList(tableNames));
    }
}
