package com.xd11cc.single.common.web;

import com.xd11cc.single.entity.base.ResponseVO;
import com.xd11cc.single.enums.SystemErrorEnum;
import com.xd11cc.single.common.exception.ErrorCode;
import com.xd11cc.single.common.exception.ServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.Objects;

/**
 * @Author: xd11cc
 * @Date: 2025/7/2 10:13
 *
 * 全局异常处理
 **/
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Spring MVC 请求方法不支持
     * 例：通常是GET方法使用POST调用
     * @param request
     * @param e
     * @return
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseVO<?> httpRequestMethodNotSupportedException(HttpServletRequest request, HttpRequestMethodNotSupportedException e) {
        String requestURI = request.getRequestURI();
        log.error("请求地址:{}，不支持：'{}'请求", requestURI, e.getMethod());
        return ResponseVO.fail(SystemErrorEnum.METHOD_NOT_ALLOWED.getErrorCode(), e.getMessage());
    }

    /**
     * Spring MVC 参数校验不正确
     * @param request
     * @param e
     * @return
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseVO<?> methodArgumentNotValidException(HttpServletRequest request, MethodArgumentNotValidException e) {
        log.error("请求地址：{}，请求参数有误:{}", request.getRequestURI(), e.getMessage());
        String message = Objects.requireNonNull(e.getBindingResult().getFieldError()).getDefaultMessage();
        return ResponseVO.fail(SystemErrorEnum.BAD_REQUEST.getErrorCode(), String.format("请求参数有误：%s", message));
    }

    /**
     * Spring MVC 参数绑定不正确，本质上也是通过Validator校验
     * @param request
     * @param e
     * @return
     */
    @ExceptionHandler(BindException.class)
    public ResponseVO<?> bindException(HttpServletRequest request, BindException e) {
        log.error("请求地址：{}，请求参数有误:{}", request.getRequestURI(), e.getMessage());
        String message = Objects.requireNonNull(e.getBindingResult().getFieldError()).getDefaultMessage();
        return ResponseVO.fail(SystemErrorEnum.BAD_REQUEST.getErrorCode(), String.format("请求参数有误：%s", message));
    }

    /**
     * 处理Validator不通过产生的异常
     * @param request
     * @param e
     * @return
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseVO<?> constraintViolationException(HttpServletRequest request, ConstraintViolationException e) {
        log.error("请求地址:{}，请求参数不通过:{}", request.getRequestURI(), e.getMessage());
        ConstraintViolation<?> next = e.getConstraintViolations().iterator().next();
        return ResponseVO.fail(SystemErrorEnum.BAD_REQUEST.getErrorCode(), String.format("请求参数有误：%s", next.getMessage()));
    }

    @ExceptionHandler(ServiceException.class)
    public ResponseVO<?> handleServiceException(ServiceException e){
        log.error(e.getMessage(), e);
        ErrorCode errorCode = e.getErrorCode();
        if (errorCode == null) {
            return ResponseVO.fail(e.getMessage());
        }
        // e.getMessage() 形如 "1002044:实际格式化后的消息"，去掉前缀后返回，
        // 这样带 {} 占位符的错误码（如“不一致：modelKey={}, bpmnProcessId={}”）能正确展示实参，
        // 而不是把模板原样透给前端。
        String message = e.getMessage();
        String prefix = errorCode.getErrorCode() + ":";
        if (message != null && message.startsWith(prefix)) {
            message = message.substring(prefix.length());
        }
        return ResponseVO.fail(errorCode.getErrorCode(), message);
    }

    /**
     * 兜底工作，拦截未知的异常
     * @param e
     * @return
     */
    /**
     * 无权限访问（@PreAuthorize 拒绝）
     * <p>
     * 必须单独处理：否则会被下面的兜底 {@code Exception} 处理器接住，
     * 变成 500「系统异常」，用户看到的和真实原因完全对不上。
     * </p>
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseVO<?> handleAccessDenied(AccessDeniedException e, HttpServletRequest request) {
        // 正常业务拒绝，不是系统故障，用 warn 避免污染错误告警
        log.warn("无权限访问：{}", request.getRequestURI());
        return ResponseVO.fail(SystemErrorEnum.NO_PERMISSION);
    }

    /**
     * 认证失败（令牌无效、密码错误等）
     * <p>
     * 认证链路抛出的异常自带原因（可能包在 cause 里），优先取业务错误码，
     * 取不到才退回「未授权」。
     * </p>
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseVO<?> handleAuthentication(AuthenticationException e, HttpServletRequest request) {
        log.warn("认证失败：{}，原因：{}", request.getRequestURI(), e.getMessage());
        Throwable cause = e.getCause();
        if (cause instanceof ServiceException) {
            return ResponseVO.fail(((ServiceException) cause).getErrorCode());
        }
        return ResponseVO.fail(SystemErrorEnum.UNAUTHORIZED);
    }

    @ExceptionHandler(Exception.class)
    public ResponseVO<?> handleException(Exception e, HttpServletRequest request){
        String requestURI = request.getRequestURI();
        log.error("请求地址：{}, 发生系统异常:{}", requestURI, e.getMessage(), e);
        Throwable throwable = e.getCause();
        if (throwable instanceof ServiceException){
            ServiceException serviceException = (ServiceException) throwable;
            return ResponseVO.fail(serviceException.getErrorCode());
        }
        return ResponseVO.fail(SystemErrorEnum.SYSTEM_ERROR);
    }
}
