package com.xd11cc.single.utils;

import org.junit.jupiter.api.Test;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

class ServletUtilsTest {

    // ==================== renderString ====================

    @Test
    void renderString_设置JSON响应头并写入内容() throws Exception {
        RequestContextHolder.setRequestAttributes(mock(ServletRequestAttributes.class));
        HttpServletResponse response = mock(HttpServletResponse.class);
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        given(response.getWriter()).willReturn(pw);

        ServletUtils.renderString(response, "{\"code\":200}");

        assertThat(sw.toString()).isEqualTo("{\"code\":200}\n");
        verify(response).setContentType("application/json; charset=utf-8");
        verify(response).setCharacterEncoding("utf-8");
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void renderString_IOException时静默吞异常() throws Exception {
        RequestContextHolder.setRequestAttributes(mock(ServletRequestAttributes.class));
        HttpServletResponse response = mock(HttpServletResponse.class);
        given(response.getWriter()).willThrow(new IOException("broken pipe"));

        // 方法内部 catch 住异常，不向外抛
        ServletUtils.renderString(response, "test");
        verify(response).setContentType("application/json; charset=utf-8");
        RequestContextHolder.resetRequestAttributes();
    }

    // ==================== getHeaders ====================

    @Test
    void getHeaders_null枚举_返回空map() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        given(request.getHeaderNames()).willReturn(null);

        assertThat(ServletUtils.getHeaders(request)).isEmpty();
    }

    @Test
    void getHeaders_正常请求头_返回大小写无关map() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        @SuppressWarnings("unchecked")
        Enumeration<String> headerNames = Collections.enumeration(
                java.util.Arrays.asList("Content-Type", "Authorization"));
        given(request.getHeaderNames()).willReturn(headerNames);
        given(request.getHeader("Content-Type")).willReturn("application/json");
        given(request.getHeader("Authorization")).willReturn("Bearer token");

        Map<String, String> headers = ServletUtils.getHeaders(request);

        assertThat(headers).hasSize(2);
        assertThat(headers.get("content-type")).isEqualTo("application/json");
        assertThat(headers.get("Content-Type")).isEqualTo("application/json");
        assertThat(headers.get("authorization")).isEqualTo("Bearer token");
    }

    // ==================== getHeader ====================

    @Test
    void getHeader_空值_返回空字符串() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        given(request.getHeader("X-Token")).willReturn(null);

        assertThat(ServletUtils.getHeader(request, "X-Token")).isEmpty();
    }

    @Test
    void getHeader_需要url解码_正确解码() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        // URL 编码后的中文字符 "张三"
        given(request.getHeader("X-Name")).willReturn("%E5%BC%A0%E4%B8%89");

        assertThat(ServletUtils.getHeader(request, "X-Name")).isEqualTo("张三");
    }

    @Test
    void getHeader_不支持编码_抛出IllegalArgumentException() {
        // urlDecode 内部只 catch UnsupportedEncodingException，不 catch IllegalArgumentException
        // 非法 hex 序列（如 %ZZ）会向外抛 IllegalArgumentException
        HttpServletRequest request = mock(HttpServletRequest.class);
        given(request.getHeader("X-Token")).willReturn("value%ZZ");

        assertThatThrownBy(() -> ServletUtils.getHeader(request, "X-Token"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // ==================== getRequest ====================

    @Test
    void getRequest_无请求上下文_抛空指针() {
        RequestContextHolder.resetRequestAttributes();

        assertThatThrownBy(ServletUtils::getRequest)
                .isInstanceOf(NullPointerException.class);
    }

    // ==================== 辅助方法 ====================

    @SuppressWarnings("unchecked")
    private Enumeration<String> enumerationOf(String... items) {
        return Collections.enumeration(java.util.Arrays.asList(items));
    }
}
