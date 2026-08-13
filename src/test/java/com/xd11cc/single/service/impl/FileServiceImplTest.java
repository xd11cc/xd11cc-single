package com.xd11cc.single.service.impl;

import com.xd11cc.single.config.exception.ServiceException;
import com.xd11cc.single.config.properties.MinioProperties;
import com.xd11cc.single.enums.SystemErrorEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class FileServiceImplTest {

    @Mock
    private MinioProperties minioProperties;

    @InjectMocks
    private FileServiceImpl fileService;

    private Method validateFileIdMethod;

    @BeforeEach
    void setUp() throws Exception {
        validateFileIdMethod = fileService.getClass()
                .getDeclaredMethod("validateFileId", String.class);
        validateFileIdMethod.setAccessible(true);
    }

    // ==================== validateFileId 路径穿越 ====================

    @Test
    void validateFileId_父目录穿越_抛ServiceException() {
        assertThatThrownBy(() -> invokeValidate("../../etc/passwd"))
                .isInstanceOf(ServiceException.class);
    }

    @Test
    void validateFileId_斜杠路径_抛ServiceException() {
        assertThatThrownBy(() -> invokeValidate("/etc/passwd"))
                .isInstanceOf(ServiceException.class);
    }

    @Test
    void validateFileId_反斜杠路径_抛ServiceException() {
        assertThatThrownBy(() -> invokeValidate("..\\windows\\system32"))
                .isInstanceOf(ServiceException.class);
    }

    @Test
    void validateFileId_null_抛ServiceException() {
        assertThatThrownBy(() -> invokeValidate(null))
                .isInstanceOf(ServiceException.class);
    }

    // ==================== validateFileId 合法id ====================

    @Test
    void validateFileId_纯文件名_通过() throws Exception {
        invokeValidate("20250812_report.pdf");
    }

    @Test
    void validateFileId_含下划线_通过() throws Exception {
        invokeValidate("2025_08_12_report.pdf");
    }

    @Test
    void validateFileId_含连字符_通过() throws Exception {
        invokeValidate("my-report-2025.pdf");
    }

    // ==================== 辅助方法 ====================

    private void invokeValidate(String fileId) {
        try {
            validateFileIdMethod.invoke(fileService, fileId);
        } catch (java.lang.reflect.InvocationTargetException e) {
            // ServiceException 抛出时被包装在 InvocationTargetException 中
            throw (RuntimeException) e.getCause();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
