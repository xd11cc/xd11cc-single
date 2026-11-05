package com.xd11cc.single.controller;

import com.xd11cc.single.entity.base.ResponseVO;
import com.xd11cc.single.service.FileService;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class FileControllerTest {

    @Mock
    private FileService fileService;

    @InjectMocks
    private FileController fileController;

    // ==================== batchDelete ====================

    @Test
    void batchDelete_全部成功_返回空失败列表() {
        given(fileService.batchDelete(anyList())).willReturn(Collections.emptyList());

        ResponseVO<List<String>> result = fileController.batchDelete(Arrays.asList("file-id-1", "file-id-2"));

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData()).isEmpty();
    }

    @Test
    void batchDelete_部分失败_把失败清单返回给调用方() {
        given(fileService.batchDelete(anyList())).willReturn(Collections.singletonList("file-id-2"));

        ResponseVO<List<String>> result = fileController.batchDelete(Arrays.asList("file-id-1", "file-id-2"));

        assertThat(result.getData()).containsExactly("file-id-2");
    }
}
