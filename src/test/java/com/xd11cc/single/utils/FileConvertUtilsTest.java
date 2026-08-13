package com.xd11cc.single.utils;

import com.xd11cc.single.config.exception.ServiceException;
import com.xd11cc.single.enums.SystemErrorEnum;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FileConvertUtilsTest {

    // ==================== PdfToWord 异常流程 ====================

    @Test
    void pdfToWord_转换过程抛异常_抛出ServiceException并包装PDF_TO_WORD_ERROR() {
        // PdfDocument 为第三方库 final 类，无法通过 mockito-inline 模拟其构造函数
        // 此处仅验证异常包装逻辑：运行时异常被包装为带 PDF_TO_WORD_ERROR 错误码的 ServiceException
        // 成功路径依赖 Spire.PDF 原生行为，由集成测试覆盖
        assertThatThrownBy(() -> {
            // 直接调用会触发真正的 PDF 转换，因无真实文件会抛异常
            FileConvertUtils.PdfToWord("/nonexistent/file.pdf", "/output.docx");
        })
        .isInstanceOf(ServiceException.class)
        .satisfies(ex -> {
            ServiceException se = (ServiceException) ex;
            assertThat(Integer.valueOf(se.getErrorCode().getErrorCode()))
                    .isEqualTo(SystemErrorEnum.PDF_TO_WORD_ERROR.getErrorCode());
            assertThat(se.getErrorCode().getErrorMsg())
                    .isEqualTo(SystemErrorEnum.PDF_TO_WORD_ERROR.getErrorMsg());
        });
    }
}
