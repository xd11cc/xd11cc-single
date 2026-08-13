package com.xd11cc.single.config.filter;

import com.alibaba.fastjson2.JSONObject;
import com.xd11cc.single.config.RedisCache;
import com.xd11cc.single.config.properties.TenantIgnoreProperties;
import com.xd11cc.single.constants.CacheConstants;
import com.xd11cc.single.constants.SecurityConstants;
import com.xd11cc.single.entity.base.ResponseVO;
import com.xd11cc.single.entity.dto.TenantDTO;
import com.xd11cc.single.enums.SystemErrorEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

class TenantFilterTest {

    private RedisCache redisCache;
    private TenantIgnoreProperties tenantIgnoreProperties;
    private TenantFilter tenantFilter;

    @BeforeEach
    void setUp() {
        redisCache = mock(RedisCache.class);
        tenantIgnoreProperties = new TenantIgnoreProperties();
        tenantIgnoreProperties.setIgnoreUrls(Arrays.asList("/public/health", "/actuator/health"));
        tenantFilter = new TenantFilter();
        // 用反射注入
        try {
            java.lang.reflect.Field redisField = TenantFilter.class.getDeclaredField("redisCache");
            redisField.setAccessible(true);
            redisField.set(tenantFilter, redisCache);
            java.lang.reflect.Field propsField = TenantFilter.class.getDeclaredField("tenantIgnoreProperties");
            propsField.setAccessible(true);
            propsField.set(tenantFilter, tenantIgnoreProperties);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // ==================== 白名单跳过 ====================

    @Test
    void doFilterInternal_白名单URL_直接放行() throws ServletException, IOException {
        org.springframework.mock.web.MockHttpServletRequest request = new org.springframework.mock.web.MockHttpServletRequest();
        request.setRequestURI("/public/health");
        request.setServerName("any.domain.com");
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChainMock filterChain = new FilterChainMock();

        tenantFilter.doFilterInternal(request, response, filterChain);

        assertThat(filterChain.passed).isTrue();
        assertThat(filterChain.passedRequest).isEqualTo(request);
        assertThat(filterChain.passedResponse).isEqualTo(response);
    }

    // ==================== 正常租户 ====================

    @Test
    void doFilterInternal_正常租户_放行并设置租户ID() throws ServletException, IOException {
        org.springframework.mock.web.MockHttpServletRequest request = new org.springframework.mock.web.MockHttpServletRequest();
        request.setRequestURI("/api/users");
        request.setServerName("tenant1.example.com");
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChainMock filterChain = new FilterChainMock();

        TenantDTO tenantDTO = new TenantDTO();
        tenantDTO.setId(10L);
        tenantDTO.setStatus("0"); // 正常
        tenantDTO.setExpireTime(new Date(System.currentTimeMillis() + 86400000L)); // 明天过期

        given(redisCache.getCacheMapValue(CacheConstants.TENANT_DOMAIN_KEY, "tenant1.example.com")).willReturn(tenantDTO);

        tenantFilter.doFilterInternal(request, response, filterChain);

        assertThat(filterChain.passed).isTrue();
        assertThat(filterChain.tenantId).isEqualTo(10L);
    }

    // ==================== 租户不存在 ====================

    @Test
    void doFilterInternal_租户不存在_返回错误响应() throws ServletException, IOException {
        StringWriter responseWriter = new StringWriter();
        org.springframework.mock.web.MockHttpServletRequest request = new org.springframework.mock.web.MockHttpServletRequest();
        request.setRequestURI("/api/users");
        request.setServerName("unknown.example.com");
        HttpServletResponse response = mock(HttpServletResponse.class);
        given(response.getWriter()).willReturn(new PrintWriter(responseWriter));

        given(redisCache.getCacheMapValue(CacheConstants.TENANT_DOMAIN_KEY, "unknown.example.com")).willReturn(null);

        FilterChainMock filterChain = new FilterChainMock();
        tenantFilter.doFilterInternal(request, response, filterChain);

        assertThat(filterChain.passed).isFalse(); // filterChain 没有被调用
        assertThat(responseWriter.toString()).contains("请使用正确的域名访问");
    }

    // ==================== 租户已禁用 ====================

    @Test
    void doFilterInternal_租户已禁用_返回错误响应() throws ServletException, IOException {
        StringWriter responseWriter = new StringWriter();
        org.springframework.mock.web.MockHttpServletRequest request = new org.springframework.mock.web.MockHttpServletRequest();
        request.setRequestURI("/api/users");
        request.setServerName("disabled.example.com");
        HttpServletResponse response = mock(HttpServletResponse.class);
        given(response.getWriter()).willReturn(new PrintWriter(responseWriter));

        TenantDTO tenantDTO = new TenantDTO();
        tenantDTO.setId(10L);
        tenantDTO.setStatus("1"); // FORBIDDEN
        tenantDTO.setExpireTime(new Date(System.currentTimeMillis() + 86400000L));

        given(redisCache.getCacheMapValue(CacheConstants.TENANT_DOMAIN_KEY, "disabled.example.com")).willReturn(tenantDTO);

        FilterChainMock filterChain = new FilterChainMock();
        tenantFilter.doFilterInternal(request, response, filterChain);

        assertThat(filterChain.passed).isFalse();
        assertThat(responseWriter.toString()).contains("系统异常，请联系管理员处理");
    }

    // ==================== 租户已过期 ====================

    @Test
    void doFilterInternal_租户已过期_返回错误响应() throws ServletException, IOException {
        StringWriter responseWriter = new StringWriter();
        org.springframework.mock.web.MockHttpServletRequest request = new org.springframework.mock.web.MockHttpServletRequest();
        request.setRequestURI("/api/users");
        request.setServerName("expired.example.com");
        HttpServletResponse response = mock(HttpServletResponse.class);
        given(response.getWriter()).willReturn(new PrintWriter(responseWriter));

        TenantDTO tenantDTO = new TenantDTO();
        tenantDTO.setId(10L);
        tenantDTO.setStatus("0");
        tenantDTO.setExpireTime(new Date(System.currentTimeMillis() - 86400000L)); // 昨天过期

        given(redisCache.getCacheMapValue(CacheConstants.TENANT_DOMAIN_KEY, "expired.example.com")).willReturn(tenantDTO);

        FilterChainMock filterChain = new FilterChainMock();
        tenantFilter.doFilterInternal(request, response, filterChain);

        assertThat(filterChain.passed).isFalse();
        assertThat(responseWriter.toString()).contains("系统异常，请联系管理员处理");
    }

    // ==================== FilterChain Mock ====================

    private static class FilterChainMock implements javax.servlet.FilterChain {
        boolean passed = false;
        HttpServletRequest passedRequest;
        HttpServletResponse passedResponse;
        Long tenantId;

        @Override
        public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {
            passed = true;
            passedRequest = (HttpServletRequest) request;
            passedResponse = (HttpServletResponse) response;
            tenantId = (Long) passedRequest.getAttribute(SecurityConstants.TENANT_ID);
        }
    }
}
