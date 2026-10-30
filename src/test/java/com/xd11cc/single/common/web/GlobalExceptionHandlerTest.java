package com.xd11cc.single.common.web;

import com.xd11cc.single.common.exception.ErrorCode;
import com.xd11cc.single.common.exception.ServiceException;
import com.xd11cc.single.enums.SystemErrorEnum;
import com.xd11cc.single.entity.base.ResponseVO;
import org.junit.jupiter.api.Test;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    // ==================== HttpRequestMethodNotSupportedException ====================

    @Test
    void httpRequestMethodNotSupported_返回方法不允许() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        given(request.getRequestURI()).willReturn("/api/users");

        HttpRequestMethodNotSupportedException ex = new HttpRequestMethodNotSupportedException("POST", new String[]{"GET", "PUT"});

        ResponseVO<?> result = handler.httpRequestMethodNotSupportedException(request, ex);

        assertThat(result.getCode()).isEqualTo(405);
        assertThat(result.getMsg()).isEqualTo(ex.getMessage());
    }

    // ==================== MethodArgumentNotValidException ====================

    @Test
    void methodArgumentNotValid_返回参数错误() throws NoSuchMethodException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        given(request.getRequestURI()).willReturn("/api/users");

        java.lang.reflect.Method dummyMethod = this.getClass().getMethod("toString");
        org.springframework.core.MethodParameter methodParameter = new org.springframework.core.MethodParameter(dummyMethod, -1);
        Object target = new Object();
        org.springframework.validation.BeanPropertyBindingResult bindingResult =
                new org.springframework.validation.BeanPropertyBindingResult(target, "target");
        FieldError fieldError = new FieldError("target", "username", "用户名不能为空");
        bindingResult.addError(fieldError);

        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(methodParameter, bindingResult);

        ResponseVO<?> result = handler.methodArgumentNotValidException(request, ex);

        assertThat(result.getCode()).isEqualTo(400);
        assertThat(result.getMsg()).contains("请求参数有误");
        assertThat(result.getMsg()).contains("用户名不能为空");
    }

    // ==================== BindException ====================

    @Test
    void bindException_返回参数错误() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        given(request.getRequestURI()).willReturn("/api/users");

        Object target = new Object();
        BindException ex = new BindException(target, "target");
        FieldError fieldError = new FieldError("target", "email", "邮箱格式不正确");
        ex.addError(fieldError);

        ResponseVO<?> result = handler.bindException(request, ex);

        assertThat(result.getCode()).isEqualTo(400);
        assertThat(result.getMsg()).contains("请求参数有误");
        assertThat(result.getMsg()).contains("邮箱格式不正确");
    }

    // ==================== ConstraintViolationException ====================

    @Test
    void constraintViolation_返回参数错误() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        given(request.getRequestURI()).willReturn("/api/users");

        @SuppressWarnings("unchecked")
        ConstraintViolation<Object> violation = mock(ConstraintViolation.class);
        given(violation.getMessage()).willReturn("手机号格式不正确");

        Set<ConstraintViolation<?>> violations = new HashSet<>(Collections.singleton(violation));
        ConstraintViolationException ex = new ConstraintViolationException("参数校验失败", violations);

        ResponseVO<?> result = handler.constraintViolationException(request, ex);

        assertThat(result.getCode()).isEqualTo(400);
        assertThat(result.getMsg()).contains("请求参数有误");
        assertThat(result.getMsg()).contains("手机号格式不正确");
    }

    // ==================== ServiceException with ErrorCode ====================

    @Test
    void serviceException_带ErrorCode_返回对应错误码() {
        ServiceException ex = new ServiceException(SystemErrorEnum.USER_NOT_FOUND);

        ResponseVO<?> result = handler.handleServiceException(ex);

        assertThat(result.getCode()).isEqualTo(SystemErrorEnum.USER_NOT_FOUND.getErrorCode());
        assertThat(result.getMsg()).isEqualTo(SystemErrorEnum.USER_NOT_FOUND.getErrorMsg());
    }

    @Test
    void serviceException_不带ErrorCode_返回原始消息() {
        ServiceException ex = new ServiceException(4001, "自定义业务异常");

        ResponseVO<?> result = handler.handleServiceException(ex);

        assertThat(result.getCode()).isEqualTo(4001);
        assertThat(result.getMsg()).isEqualTo("自定义业务异常");
    }

    // ==================== handleException (兜底) ====================

    @Test
    void handleException_普通异常_返回系统错误() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        given(request.getRequestURI()).willReturn("/api/users");

        Exception ex = new RuntimeException("连接超时");

        ResponseVO<?> result = handler.handleException(ex, request);

        assertThat(result.getCode()).isEqualTo(SystemErrorEnum.SYSTEM_ERROR.getErrorCode());
        assertThat(result.getMsg()).isEqualTo(SystemErrorEnum.SYSTEM_ERROR.getErrorMsg());
    }

    @Test
    void handleException_嵌套ServiceException_解包业务异常() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        given(request.getRequestURI()).willReturn("/api/users");

        ServiceException serviceException = new ServiceException(4004, "数据不存在");
        Exception ex = new RuntimeException("wrapper", serviceException);

        ResponseVO<?> result = handler.handleException(ex, request);

        assertThat(result.getCode()).isEqualTo(4004);
        assertThat(result.getMsg()).isEqualTo("数据不存在");
    }
    // ==================== AccessDeniedException（@PreAuthorize 拒绝）====================

    @Test
    void handleAccessDenied_返回没有操作权限() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        given(request.getRequestURI()).willReturn("/system/user/add");

        ResponseVO<?> result = handler.handleAccessDenied(
                new org.springframework.security.access.AccessDeniedException("denied"), request);

        assertThat(result.getCode()).isEqualTo(SystemErrorEnum.NO_PERMISSION.getErrorCode());
        assertThat(result.getMsg()).isEqualTo(SystemErrorEnum.NO_PERMISSION.getErrorMsg());
    }

    @Test
    void handleAccessDenied_不再被兜底处理器吞成系统错误() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        given(request.getRequestURI()).willReturn("/system/user/add");

        ResponseVO<?> result = handler.handleAccessDenied(
                new org.springframework.security.access.AccessDeniedException("denied"), request);

        assertThat(result.getCode()).isNotEqualTo(SystemErrorEnum.SYSTEM_ERROR.getErrorCode());
    }

    // ==================== AuthenticationException（认证失败）====================

    @Test
    void handleAuthentication_ServiceException原因_返回业务错误码() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        given(request.getRequestURI()).willReturn("/login/loginByPassword");

        ServiceException cause = new ServiceException(SystemErrorEnum.PASSWORD_ERROR);
        org.springframework.security.core.AuthenticationException ex =
                new org.springframework.security.authentication.BadCredentialsException("bad", cause);

        ResponseVO<?> result = handler.handleAuthentication(ex, request);

        assertThat(result.getCode()).isEqualTo(SystemErrorEnum.PASSWORD_ERROR.getErrorCode());
    }

    @Test
    void handleAuthentication_无原因_返回未授权() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        given(request.getRequestURI()).willReturn("/login/loginByPassword");

        org.springframework.security.core.AuthenticationException ex =
                new org.springframework.security.authentication.BadCredentialsException("bad");

        ResponseVO<?> result = handler.handleAuthentication(ex, request);

        assertThat(result.getCode()).isEqualTo(SystemErrorEnum.UNAUTHORIZED.getErrorCode());
    }
}
