package com.xd11cc.single.config.filter;

import com.xd11cc.single.config.context.TenantContextHolder;
import com.xd11cc.single.constants.SecurityConstants;
import com.xd11cc.single.entity.dto.LoginUserDTO;
import com.xd11cc.single.service.TokenService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class JwtAuthenticationTokenFilterTest {

    private TokenService tokenService;
    private JwtAuthenticationTokenFilter filter;

    @BeforeEach
    void setUp() {
        tokenService = mock(TokenService.class);
        filter = new JwtAuthenticationTokenFilter();
        // 用反射注入 tokenService
        try {
            java.lang.reflect.Field field = JwtAuthenticationTokenFilter.class.getDeclaredField("tokenService");
            field.setAccessible(true);
            field.set(filter, tokenService);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        TenantContextHolder.clear();
        SecurityContextHolder.clearContext();
    }

    // ==================== 租户上下文传播 ====================

    @Test
    void doFilterInternal_设置租户ID_执行后清除() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(SecurityConstants.TENANT_ID, 10L);
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        given(tokenService.getLoginUser(request)).willReturn(null);

        filter.doFilterInternal(request, response, filterChain);

        // filterChain 已执行
        assertThat(filterChain.getRequest()).isEqualTo(request);
        assertThat(TenantContextHolder.getTenantId()).isNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    // ==================== 无Token情况 ====================

    @Test
    void doFilterInternal_无登录用户_继续过滤器链() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(SecurityConstants.TENANT_ID, 10L);
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        given(tokenService.getLoginUser(request)).willReturn(null);

        filter.doFilterInternal(request, response, filterChain);

        assertThat(filterChain.getRequest()).isEqualTo(request);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    // ==================== 有Token，已认证 ====================

    @Test
    void doFilterInternal_已有认证_不重复设置() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(SecurityConstants.TENANT_ID, 5L);
        MockHttpServletResponse response = new MockHttpServletResponse();

        LoginUserDTO loginUserDTO = mockLoginUser(1L, 10L);
        given(tokenService.getLoginUser(request)).willReturn(loginUserDTO);

        // 预设 SecurityContext 已有认证
        SecurityContextHolder.getContext().setAuthentication(
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken("existing", null, Collections.emptyList()));

        // 在 filterChain 内部验证：认证保持不变
        MockFilterChain filterChain = new MockFilterChain() {
            public void doFilter(ServletRequest req, ServletResponse res) throws IOException, ServletException {
                assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
                assertThat(TenantContextHolder.getTenantId()).isEqualTo(5L);
            }
        };

        filter.doFilterInternal(request, response, filterChain);

        // finally 块已清除
        assertThat(TenantContextHolder.getTenantId()).isNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    // ==================== 有Token，未认证 ====================

    @Test
    void doFilterInternal_有Token未认证_设置SecurityContext() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(SecurityConstants.TENANT_ID, 5L);
        MockHttpServletResponse response = new MockHttpServletResponse();

        LoginUserDTO loginUserDTO = mockLoginUser(1L, 10L);
        given(tokenService.getLoginUser(request)).willReturn(loginUserDTO);

        // 在 filterChain 内部验证：认证已被设置
        MockFilterChain filterChain = new MockFilterChain() {
            public void doFilter(ServletRequest req, ServletResponse res) throws IOException, ServletException {
                assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
                org.springframework.security.authentication.UsernamePasswordAuthenticationToken auth =
                        (org.springframework.security.authentication.UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
                assertThat(auth.getPrincipal()).isEqualTo(loginUserDTO);
                assertThat(TenantContextHolder.getTenantId()).isEqualTo(5L);
            }
        };

        filter.doFilterInternal(request, response, filterChain);

        // finally 块已清除
        assertThat(TenantContextHolder.getTenantId()).isNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    // ==================== 租户ID为null ====================

    @Test
    void doFilterInternal_租户ID为null_正常处理() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(SecurityConstants.TENANT_ID, null);
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        given(tokenService.getLoginUser(request)).willReturn(null);

        filter.doFilterInternal(request, response, filterChain);

        assertThat(filterChain.getRequest()).isEqualTo(request);
        assertThat(TenantContextHolder.getTenantId()).isNull();
    }

    // ==================== 异常情况 ====================

    @Test
    void doFilterInternal_tokenService抛异常_仍清除上下文() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(SecurityConstants.TENANT_ID, 10L);
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        given(tokenService.getLoginUser(request)).willThrow(new RuntimeException("token error"));

        assertThrows(RuntimeException.class, () -> {
            try {
                filter.doFilterInternal(request, response, filterChain);
            } catch (ServletException | IOException e) {
                throw new RuntimeException(e);
            }
        });

        assertThat(TenantContextHolder.getTenantId()).isNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    // ==================== 辅助方法 ====================

    private LoginUserDTO mockLoginUser(Long userId, Long tenantId) {
        LoginUserDTO dto = mock(LoginUserDTO.class);
        com.xd11cc.single.entity.domain.SystemUserDO userDO = new com.xd11cc.single.entity.domain.SystemUserDO();
        userDO.setId(userId);
        userDO.setTenantId(tenantId);
        dto.setSystemUserDO(userDO);
        dto.setUserId(userId);
        given(dto.getAuthorities()).willReturn(Collections.emptyList());
        return dto;
    }
}
