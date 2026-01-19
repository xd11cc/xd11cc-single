package com.xd11cc.single.config.handler;

import com.xd11cc.single.entity.vo.base.ResponseVO;
import com.xd11cc.single.enums.SingleErrorEnum;
import com.xd11cc.single.exception.ErrorCode;
import com.xd11cc.single.exception.RateLimitException;
import com.xd11cc.single.exception.ServiceException;
import lombok.extern.slf4j.Slf4j;
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
        return ResponseVO.fail(SingleErrorEnum.METHOD_NOT_ALLOWED.getErrorCode(), e.getMessage());
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
        return ResponseVO.fail(SingleErrorEnum.BAD_REQUEST.getErrorCode(), String.format("请求参数有误：%s", message));
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
        return ResponseVO.fail(SingleErrorEnum.BAD_REQUEST.getErrorCode(), String.format("请求参数有误：%s", message));
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
        return ResponseVO.fail(SingleErrorEnum.BAD_REQUEST.getErrorCode(), String.format("请求参数有误：%s", next.getMessage()));
    }

    @ExceptionHandler(RateLimitException.class)
    public ResponseVO<?> handleRateLimitException(RateLimitException e){
        log.error(e.getMessage(), e);
        return ResponseVO.fail(e.getMessage());
    }

    /**
     * 处理业务逻辑上的异常
     * @param e
     * @return
     */
    @ExceptionHandler(ServiceException.class)
    public ResponseVO<?> handleServiceException(ServiceException e){
        log.error(e.getMessage(), e);
        ErrorCode errorCode = e.getErrorCode();
        return null != errorCode ? ResponseVO.fail(errorCode) : ResponseVO.fail(e.getMessage());
    }

    /**
     * 兜底工作，拦截未知的异常
     * @param e
     * @return
     */
    @ExceptionHandler(Exception.class)
    public ResponseVO<?> handleException(Exception e, HttpServletRequest request){
        String requestURI = request.getRequestURI();
        log.error("请求地址：{}, 发生系统异常:{}", requestURI, e.getMessage(), e);
        Throwable throwable = e.getCause();
        if (throwable instanceof ServiceException){
            ServiceException serviceException = (ServiceException) throwable;
            return ResponseVO.fail(serviceException.getErrorCode().getErrorCode(), serviceException.getMessage());
        }
        return ResponseVO.fail(SingleErrorEnum.SYSTEM_ERROR);
    }
}
