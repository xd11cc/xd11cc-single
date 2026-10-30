package com.xd11cc.single.common.web;

import com.alibaba.fastjson2.JSONObject;
import com.xd11cc.single.entity.base.ResponseVO;
import com.xd11cc.single.enums.SystemErrorEnum;
import com.xd11cc.single.utils.ServletUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 无权限访问处理器。
 * <p>
 * 与 {@link AuthenticationEntryPointImpl} 配对：那里管「没登录」（401），
 * 这里管「登录了但没权限」（403）。
 * </p>
 * <p>
 * 控制器方法上的 {@code @PreAuthorize} 拒绝会被全局异常处理器接住，走不到这里；
 * 本类负责的是 <b>过滤器链层面</b>的拒绝，保证响应体与全项目一致（ResponseVO 信封），
 * 而不是 Spring 默认的空 body。
 * </p>
 *
 * @author xd11cc
 */
@Slf4j
@Component
public class AccessDeniedHandlerImpl implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) {
        log.warn("无权限访问（过滤器链）：{}", request.getRequestURI());
        ServletUtils.renderString(response, JSONObject.toJSONString(
                ResponseVO.fail(SystemErrorEnum.NO_PERMISSION)));
    }
}
