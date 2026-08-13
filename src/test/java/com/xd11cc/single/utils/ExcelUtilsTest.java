package com.xd11cc.single.utils;

import com.alibaba.excel.EasyExcel;
import org.junit.jupiter.api.Test;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

class ExcelUtilsTest {

    // ==================== export 方法测试 ====================

    @Test
    void export_设置正确的响应头() throws Exception {
        RequestContextHolder.setRequestAttributes(mock(ServletRequestAttributes.class));
        HttpServletResponse response = mock(HttpServletResponse.class);
        ServletOutputStream outputStream = mock(ServletOutputStream.class);
        given(response.getOutputStream()).willReturn(outputStream);

        ExcelUtils.export(response, "test file", "sheet1", String.class, Collections.emptyList());

        verify(response).setContentType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        verify(response).setCharacterEncoding("utf-8");
        verify(response).setHeader(startsWith("Content-Disposition"), contains("test%20file.xlsx"));
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void export_中文文件名正确编码为UTF8() throws Exception {
        RequestContextHolder.setRequestAttributes(mock(ServletRequestAttributes.class));
        HttpServletResponse response = mock(HttpServletResponse.class);
        ServletOutputStream outputStream = mock(ServletOutputStream.class);
        given(response.getOutputStream()).willReturn(outputStream);

        ExcelUtils.export(response, "报表", "sheet1", String.class, Collections.emptyList());

        verify(response).setHeader(startsWith("Content-Disposition"), contains("%E6%8A%A5%E8%A1%A8.xlsx"));
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void export_IOException时抛出异常() throws Exception {
        RequestContextHolder.setRequestAttributes(mock(ServletRequestAttributes.class));
        HttpServletResponse response = mock(HttpServletResponse.class);
        given(response.getOutputStream()).willThrow(new IOException("磁盘已满"));

        assertThatThrownBy(() ->
                ExcelUtils.export(response, "test", "sheet1", String.class, Collections.emptyList()))
                .isInstanceOf(IOException.class)
                .hasMessage("磁盘已满");
        RequestContextHolder.resetRequestAttributes();
    }
}
