package com.xd11cc.single.config.flowable;

import com.xd11cc.single.utils.SecurityUtils;
import org.flowable.engine.IdentityService;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 每次 HTTP 请求进入时，从 Spring Security 上下文取当前登录用户，写入 Flowable 的 IdentityService。
 * <p>
 * 这样后续调用 {@code runtimeService.startProcessInstanceByKey(...)} 时，Flowable 会自动把
 * 当前用户写进 ACT_HI_PROCINST 的 START_USER_ID_ 字段，实现"发起人自动成为流程发起人"。
 */
public class FlowableSecurityInterceptor implements HandlerInterceptor {

    private final IdentityService identityService;

    public FlowableSecurityInterceptor(IdentityService identityService) {
        this.identityService = identityService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String userId = SecurityUtils.getUsername();
        if (userId != null) {
            // 每次请求都重设，防止跨请求污染
            identityService.setAuthenticatedUserId(userId);
        } else {
            // 匿名/未登录请求，清掉之前可能残留的 authenticated user
            identityService.setAuthenticatedUserId(null);
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
                           ModelAndView modelAndView) {
        // 不需要额外处理，请求结束线程自动回收，不残留
    }
}
