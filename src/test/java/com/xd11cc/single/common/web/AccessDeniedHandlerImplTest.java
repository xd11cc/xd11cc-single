package com.xd11cc.single.common.web;

import com.xd11cc.single.enums.SystemErrorEnum;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

/**
 * {@link AccessDeniedHandlerImpl} 的测试。
 * <p>
 * 它负责过滤器链层面的 403 渲染，保证响应体是全项目的 ResponseVO 信封，
 * 而不是 Spring 默认的空 body。
 * </p>
 *
 * @author xd11cc
 */
class AccessDeniedHandlerImplTest {

    private final AccessDeniedHandlerImpl handler = new AccessDeniedHandlerImpl();

    @Test
    void handle_返回没有操作权限信封() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        given(request.getRequestURI()).willReturn("/system/user/add");
        StringWriter writer = new StringWriter();
        given(response.getWriter()).willReturn(new PrintWriter(writer));

        handler.handle(request, response, new AccessDeniedException("denied"));

        String body = writer.toString();
        assertThat(body).contains(String.valueOf(SystemErrorEnum.NO_PERMISSION.getErrorCode()));
        assertThat(body).contains(SystemErrorEnum.NO_PERMISSION.getErrorMsg());
    }
}
