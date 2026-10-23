package com.xd11cc.single.modules.workflow;

import com.xd11cc.single.utils.SecurityUtils;
import org.flowable.engine.IdentityService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class FlowableSecurityInterceptorTest {

    @Mock
    private IdentityService identityService;

    @InjectMocks
    private FlowableSecurityInterceptor interceptor;

    // ==================== preHandle 已登录 ====================

    @Test
    void preHandle_已登录_设置Flowable认证用户() {
        try (org.mockito.MockedStatic<SecurityUtils> mocked = org.mockito.Mockito.mockStatic(SecurityUtils.class)) {
            mocked.when(SecurityUtils::getUsername).thenReturn("admin");

            boolean result = interceptor.preHandle(mockRequest(), mockResponse(), new Object());

            assertThat(result).isTrue();
            verify(identityService).setAuthenticatedUserId("admin");
        }
    }

    // ==================== preHandle 未登录 ====================

    @Test
    void preHandle_未登录_清空Flowable认证用户() {
        try (org.mockito.MockedStatic<SecurityUtils> mocked = org.mockito.Mockito.mockStatic(SecurityUtils.class)) {
            mocked.when(SecurityUtils::getUsername).thenReturn(null);

            boolean result = interceptor.preHandle(mockRequest(), mockResponse(), new Object());

            assertThat(result).isTrue();
            verify(identityService).setAuthenticatedUserId(null);
            verify(identityService, never()).setAuthenticatedUserId(anyString());
        }
    }

    // ==================== afterCompletion 必须清理 ====================

    /**
     * afterCompletion 无论请求成功还是异常都要清理 ThreadLocal，
     * 否则身份会残留在复用的 Tomcat 线程上并被下一个请求误用。
     */
    @Test
    void afterCompletion_正常结束_清空认证用户() {
        interceptor.afterCompletion(mockRequest(), mockResponse(), new Object(), null);

        verify(identityService).setAuthenticatedUserId(null);
    }

    @Test
    void afterCompletion_异常结束_仍然清空认证用户() {
        interceptor.afterCompletion(mockRequest(), mockResponse(), new Object(),
                new IllegalStateException("boom"));

        verify(identityService).setAuthenticatedUserId(null);
    }

    // ==================== 辅助方法 ====================

    private HttpServletRequest mockRequest() {
        return org.mockito.Mockito.mock(HttpServletRequest.class);
    }

    private HttpServletResponse mockResponse() {
        return org.mockito.Mockito.mock(HttpServletResponse.class);
    }
}
