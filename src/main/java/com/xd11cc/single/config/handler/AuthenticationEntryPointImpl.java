package com.xd11cc.single.config.handler;

import com.xd11cc.single.enums.SingleErrorEnum;
import com.xd11cc.single.exception.ServiceExceptions;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @Author: xd11cc
 * @Date: 2025/6/16 23:25
 *
 * security认证异常处理
 **/
@Component
public class AuthenticationEntryPointImpl implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) {
        ServiceExceptions.throwWithErrorCode(SingleErrorEnum.UNAUTHORIZED);
    }
}
