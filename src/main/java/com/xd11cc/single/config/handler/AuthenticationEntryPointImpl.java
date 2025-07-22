package com.xd11cc.single.config.handler;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.xd11cc.single.entity.vo.base.ResponseVO;
import com.xd11cc.single.enums.SingleErrorEnum;
import com.xd11cc.single.exception.ServiceException;
import com.xd11cc.single.utils.ServletUtils;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@Component
public class AuthenticationEntryPointImpl implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) {
        log.error("请求地址：{}", request.getRequestURI());
        Throwable throwable = authException.getCause();
        if (throwable instanceof ServiceException) {
            throw (ServiceException) throwable;
        }
        ServletUtils.renderString(response, JSONObject.toJSONString(ResponseVO.fail(SingleErrorEnum.UNAUTHORIZED)));
    }
}
