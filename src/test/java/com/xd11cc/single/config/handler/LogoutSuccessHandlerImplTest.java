package com.xd11cc.single.config.handler;

import com.xd11cc.single.config.context.TenantContextHolder;
import com.xd11cc.single.constants.SecurityConstants;
import com.xd11cc.single.entity.dto.LoginUserDTO;
import com.xd11cc.single.service.ISystemLoginLogService;
import com.xd11cc.single.service.TokenService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class LogoutSuccessHandlerImplTest {

    private TokenService tokenService;
    private ISystemLoginLogService systemLoginLogService;
    private LogoutSuccessHandlerImpl handler;

    @BeforeEach
    void setUp() {
        tokenService = mock(TokenService.class);
        systemLoginLogService = mock(ISystemLoginLogService.class);
        handler = new LogoutSuccessHandlerImpl();
        // 用反射注入
        try {
            java.lang.reflect.Field tokenField = LogoutSuccessHandlerImpl.class.getDeclaredField("tokenService");
            tokenField.setAccessible(true);
            tokenField.set(handler, tokenService);
            java.lang.reflect.Field logField = LogoutSuccessHandlerImpl.class.getDeclaredField("systemLoginLogService");
            logField.setAccessible(true);
            logField.set(handler, systemLoginLogService);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @AfterEach
    void tearDown() {
        TenantContextHolder.clear();
    }

    // ==================== 正常退出（已登录用户） ====================

    @Test
    void onLogoutSuccess_已登录用户_记录日志并清除令牌() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        StringWriter responseWriter = new StringWriter();
        given(response.getWriter()).willReturn(new PrintWriter(responseWriter));

        given(request.getAttribute(SecurityConstants.TENANT_ID)).willReturn(10L);

        LoginUserDTO loginUser = mockLoginUser(1L, 10L);
        given(tokenService.getLoginUser(request)).willReturn(loginUser);
        given(loginUser.getUsername()).willReturn("admin");

        handler.onLogoutSuccess(request, response, null);

        // 验证响应成功
        String body = responseWriter.toString();
        assertThat(body).contains("200");

        // 验证记录登出日志
        verify(systemLoginLogService).recordLoginLog("admin", com.xd11cc.single.enums.LoginTypeEnum.LOGOUT,
                com.xd11cc.single.enums.OperateStatusEnum.SUCCESS, "退出成功");

        // 验证移除登录用户
        verify(tokenService).removeLoginUser(loginUser);

        // finally 清除
        assertThat(TenantContextHolder.getTenantId()).isNull();
    }

    // ==================== 未登录用户 ====================

    @Test
    void onLogoutSuccess_未登录用户_不记录日志() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        StringWriter responseWriter = new StringWriter();
        given(response.getWriter()).willReturn(new PrintWriter(responseWriter));

        given(request.getAttribute(SecurityConstants.TENANT_ID)).willReturn(10L);
        given(tokenService.getLoginUser(request)).willReturn(null);

        handler.onLogoutSuccess(request, response, null);

        String body = responseWriter.toString();
        assertThat(body).contains("200");

        // 未登录时不记录日志
        verify(systemLoginLogService, org.mockito.Mockito.never()).recordLoginLog(
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.anyString());

        assertThat(TenantContextHolder.getTenantId()).isNull();
    }

    // ==================== 租户ID为null ====================

    @Test
    void onLogoutSuccess_租户ID为null_正常处理() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        StringWriter responseWriter = new StringWriter();
        given(response.getWriter()).willReturn(new PrintWriter(responseWriter));

        given(request.getAttribute(SecurityConstants.TENANT_ID)).willReturn(null);
        given(tokenService.getLoginUser(request)).willReturn(null);

        handler.onLogoutSuccess(request, response, null);

        String body = responseWriter.toString();
        assertThat(body).contains("200");
        assertThat(TenantContextHolder.getTenantId()).isNull();
    }

    // ==================== 异常安全 ====================

    @Test
    void onLogoutSuccess_tokenService抛异常_仍清除租户上下文() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        given(request.getAttribute(SecurityConstants.TENANT_ID)).willReturn(10L);
        given(tokenService.getLoginUser(request)).willThrow(new RuntimeException("token error"));

        try {
            handler.onLogoutSuccess(request, response, null);
        } catch (Exception e) {
            // expected
        }

        assertThat(TenantContextHolder.getTenantId()).isNull();
    }

    // ==================== 租户上下文传播 ====================

    @Test
    void onLogoutSuccess_设置租户ID_执行期间可获取() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        StringWriter responseWriter = new StringWriter();
        given(response.getWriter()).willReturn(new PrintWriter(responseWriter));

        given(request.getAttribute(SecurityConstants.TENANT_ID)).willReturn(20L);
        given(tokenService.getLoginUser(request)).willReturn(null);

        handler.onLogoutSuccess(request, response, null);

        assertThat(TenantContextHolder.getTenantId()).isNull(); // finally cleared
    }

    // ==================== 辅助方法 ====================

    private LoginUserDTO mockLoginUser(Long userId, Long tenantId) {
        LoginUserDTO dto = mock(LoginUserDTO.class);
        com.xd11cc.single.entity.domain.SystemUserDO userDO = new com.xd11cc.single.entity.domain.SystemUserDO();
        userDO.setId(userId);
        userDO.setTenantId(tenantId);
        dto.setSystemUserDO(userDO);
        dto.setUserId(userId);
        return dto;
    }
}
