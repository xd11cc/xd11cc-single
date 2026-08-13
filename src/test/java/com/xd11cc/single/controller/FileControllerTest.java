package com.xd11cc.single.controller;

import com.xd11cc.single.entity.base.ResponseVO;
import com.xd11cc.single.service.FileService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class FileControllerTest {

    @Mock
    private FileService fileService;

    @InjectMocks
    private FileController fileController;

    // ==================== batchDelete ====================

    @Test
    void batchDelete_批量删除文件_返回成功() {
        ResponseVO result = fileController.batchDelete(Arrays.asList("file-id-1", "file-id-2"));

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData()).isNull();
    }
}
