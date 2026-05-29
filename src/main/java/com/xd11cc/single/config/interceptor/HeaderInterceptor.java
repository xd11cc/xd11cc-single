package com.xd11cc.single.config.interceptor;

import com.xd11cc.single.config.context.TenantContextHolder;
import com.xd11cc.single.constants.SecurityConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.AsyncHandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author xd11cc
 * @date 2026-01-23 17:33:53
 * @description 自定义请求头拦截器，将Header数据封装到线程变量中
 */
@Slf4j
@Component
public class HeaderInterceptor implements AsyncHandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        // 处理租户id
        Long tenantId = (Long) request.getAttribute(SecurityConstants.TENANT_ID);
        if (tenantId != null) {
            TenantContextHolder.setTenantId(tenantId);
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        TenantContextHolder.clear();
    }
}
