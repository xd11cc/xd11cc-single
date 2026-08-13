package com.xd11cc.single.config.handler;

import com.xd11cc.single.config.exception.ErrorCode;
import com.xd11cc.single.config.exception.ServiceException;
import com.xd11cc.single.entity.base.ResponseVO;
import com.xd11cc.single.enums.SystemErrorEnum;
import org.junit.jupiter.api.Test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mock;

class AuthenticationEntryPointImplTest {

    private final AuthenticationEntryPointImpl entryPoint = new AuthenticationEntryPointImpl();

    // ==================== 普通认证异常 ====================

    @Test
    void commence_普通认证异常_返回UNAUTHORIZED() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        StringWriter responseWriter = new StringWriter();
        given(response.getWriter()).willReturn(new PrintWriter(responseWriter));

        org.springframework.security.core.AuthenticationException authException =
                new org.springframework.security.core.AuthenticationException("未认证") {};

        entryPoint.commence(request, response, authException);

        String body = responseWriter.toString();
        assertThat(body).contains("401"); // UNAUTHORIZED code
        assertThat(body).contains("未授权");
    }

    // ==================== ServiceException 原因 ====================

    @Test
    void commence_ServiceException原因_返回对应错误码() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        StringWriter responseWriter = new StringWriter();
        given(response.getWriter()).willReturn(new PrintWriter(responseWriter));

        ServiceException serviceException = new ServiceException(SystemErrorEnum.CHOOSE_RIGHT_DOMAIN);
        org.springframework.security.core.AuthenticationException authException =
                new org.springframework.security.core.AuthenticationException("认证失败", serviceException) {};

        entryPoint.commence(request, response, authException);

        String body = responseWriter.toString();
        assertThat(body).contains("1008001"); // CHOOSE_RIGHT_DOMAIN code
        assertThat(body).contains("请使用正确的域名访问");
    }

    // ==================== 自定义 ErrorCode 原因 ====================

    @Test
    void commence_自定义ErrorCode_返回对应错误码() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        StringWriter responseWriter = new StringWriter();
        given(response.getWriter()).willReturn(new PrintWriter(responseWriter));

        ServiceException serviceException = new ServiceException(4001, "自定义业务异常");
        org.springframework.security.core.AuthenticationException authException =
                new org.springframework.security.core.AuthenticationException("认证失败", serviceException) {};

        entryPoint.commence(request, response, authException);

        String body = responseWriter.toString();
        assertThat(body).contains("4001");
        assertThat(body).contains("自定义业务异常");
    }

    // ==================== 日志记录 ====================

    @Test
    void commence_记录日志_包含请求地址() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        StringWriter responseWriter = new StringWriter();
        given(response.getWriter()).willReturn(new PrintWriter(responseWriter));
        given(request.getRequestURI()).willReturn("/api/users");

        org.springframework.security.core.AuthenticationException authException =
                new org.springframework.security.core.AuthenticationException("未认证") {};

        entryPoint.commence(request, response, authException);

        // 验证响应体正确
        String body = responseWriter.toString();
        assertThat(body).contains("401");
    }
}
