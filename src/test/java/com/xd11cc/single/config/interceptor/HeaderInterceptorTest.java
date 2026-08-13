package com.xd11cc.single.config.interceptor;

import com.xd11cc.single.config.context.TenantContextHolder;
import com.xd11cc.single.constants.SecurityConstants;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HeaderInterceptorTest {

    private final HeaderInterceptor interceptor = new HeaderInterceptor();

    @AfterEach
    void tearDown() {
        TenantContextHolder.clear();
    }

    // ==================== preHandle ====================

    @Test
    void preHandle_请求有租户ID_设置到上下文() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        org.springframework.web.method.HandlerMethod handler = mock(org.springframework.web.method.HandlerMethod.class);

        when(request.getAttribute(SecurityConstants.TENANT_ID)).thenReturn(10L);

        boolean result = interceptor.preHandle(request, response, handler);

        assertThat(result).isTrue();
        assertThat(TenantContextHolder.getTenantId()).isEqualTo(10L);
    }

    @Test
    void preHandle_请求无租户ID_不设置上下文() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        org.springframework.web.method.HandlerMethod handler = mock(org.springframework.web.method.HandlerMethod.class);

        when(request.getAttribute(SecurityConstants.TENANT_ID)).thenReturn(null);

        boolean result = interceptor.preHandle(request, response, handler);

        assertThat(result).isTrue();
        assertThat(TenantContextHolder.getTenantId()).isNull();
    }

    // ==================== afterCompletion ====================

    @Test
    void afterCompletion_清除租户上下文() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        Object handler = mock(Object.class);
        Exception ex = null;

        TenantContextHolder.setTenantId(10L);

        interceptor.afterCompletion(request, response, handler, ex);

        assertThat(TenantContextHolder.getTenantId()).isNull();
    }

    // ==================== Handler 类型 ====================

    @Test
    void preHandle_非HandlerMethod_直接放行() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        // handler 是 String（不是 HandlerMethod）
        Object handler = "not-a-handler";

        boolean result = interceptor.preHandle(request, response, handler);

        assertThat(result).isTrue();
        assertThat(TenantContextHolder.getTenantId()).isNull();
    }
}
