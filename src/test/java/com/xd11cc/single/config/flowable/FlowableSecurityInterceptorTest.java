package com.xd11cc.single.config.flowable;

import com.xd11cc.single.utils.SecurityUtils;
import org.flowable.engine.IdentityService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
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
            verify(identityService, never()).setAuthenticatedUserId(org.mockito.ArgumentMatchers.anyString());
        }
    }

    // ==================== postHandle 无操作 ====================

    @Test
    void postHandle_直接返回_不修改modelAndView() {
        ModelAndView mav = new ModelAndView("test");

        interceptor.postHandle(mockRequest(), mockResponse(), new Object(), mav);

        assertThat(mav.getViewName()).isEqualTo("test");
        verify(identityService, never()).setAuthenticatedUserId(org.mockito.ArgumentMatchers.any());
    }

    // ==================== 辅助方法 ====================

    private HttpServletRequest mockRequest() {
        return org.mockito.Mockito.mock(HttpServletRequest.class);
    }

    private HttpServletResponse mockResponse() {
        return org.mockito.Mockito.mock(HttpServletResponse.class);
    }
}
