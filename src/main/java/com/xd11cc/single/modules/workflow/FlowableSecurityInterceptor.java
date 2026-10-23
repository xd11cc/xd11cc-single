package com.xd11cc.single.modules.workflow;

import com.xd11cc.single.utils.SecurityUtils;
import org.flowable.engine.IdentityService;
import org.springframework.web.servlet.HandlerInterceptor;

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

    /**
     * 请求结束后清理 Flowable 身份上下文。
     * <p>
     * {@code IdentityService.setAuthenticatedUserId} 底层是 static ThreadLocal，
     * Tomcat 线程会被复用；若只在 preHandle 里设置而不清理，一旦请求在 preHandle 之后抛异常，
     * postHandle 不会执行，身份就会残留在该线程上并被下一个请求误用（发起人串号）。
     * afterCompletion 无论请求成功与否都会执行，是可靠的释放点。
     * </p>
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        identityService.setAuthenticatedUserId(null);
    }
}
