package com.xd11cc.single.service.impl;

import com.xd11cc.single.common.properties.FileCleanupProperties;
import com.xd11cc.single.entity.domain.SystemFileDO;
import com.xd11cc.single.enums.SystemFileStatusEnum;
import com.xd11cc.single.mapper.SystemFileMapper;
import com.xd11cc.single.service.FileCleanupResult;
import io.minio.MinioClient;
import io.minio.RemoveObjectArgs;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

/**
 * {@link FileCleanupServiceImpl} 的测试。
 * <p>
 * 清理完全由数据库驱动：按状态 + 时间捞候选，再删对象、删记录。
 * 覆盖：试运行不删除、正常回收、失败保留、失败时不空转。
 * </p>
 * <p>
 * 注意查询顺序是固定的：先扫「上传中」，再扫「临时」；每个状态按批查询直到取空
 * （试运行时每状态只查一次）。所以这里按调用顺序编排桩。
 * </p>
 *
 * @author xd11cc
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class FileCleanupServiceImplTest {

    @Mock
    private SystemFileMapper systemFileMapper;

    @Mock
    private MinioClient minioClient;

    private FileCleanupServiceImpl cleanupService;

    @BeforeEach
    void setUp() {
        FileCleanupProperties properties = new FileCleanupProperties();
        properties.setBatchSize(200);
        cleanupService = new FileCleanupServiceImpl(systemFileMapper, minioClient, properties);
    }

    @Test
    @DisplayName("试运行：只统计，不调用删除")
    void run_dryRun_countsOnly() throws Exception {
        // 每个状态只查一次：上传中 1 条、临时 1 条
        given(systemFileMapper.selectList(any()))
                .willReturn(Collections.singletonList(record(1L, SystemFileStatusEnum.UPLOADING.getCode())))
                .willReturn(Collections.singletonList(record(2L, SystemFileStatusEnum.TEMP.getCode())));

        FileCleanupResult result = cleanupService.run(true);

        assertThat(result.isDryRun()).isTrue();
        assertThat(result.getPendingRemoved()).isEqualTo(1);
        assertThat(result.getTempRemoved()).isEqualTo(1);
        assertThat(result.getObjectsDeleted()).isZero();
        then(minioClient).should(never()).removeObject(any(RemoveObjectArgs.class));
        then(systemFileMapper).should(never()).deleteById(any(Long.class));
    }

    @Test
    @DisplayName("正常回收：删对象并逻辑删除记录")
    void run_removesObjectAndRecord() throws Exception {
        // 上传中：空；临时：1 条 → 下一轮空
        given(systemFileMapper.selectList(any()))
                .willReturn(Collections.emptyList())
                .willReturn(Collections.singletonList(record(1L, SystemFileStatusEnum.TEMP.getCode())))
                .willReturn(Collections.emptyList());

        FileCleanupResult result = cleanupService.run(false);

        assertThat(result.getPendingRemoved()).isZero();
        assertThat(result.getTempRemoved()).isEqualTo(1);
        assertThat(result.getObjectsDeleted()).isEqualTo(1);
        then(minioClient).should().removeObject(any(RemoveObjectArgs.class));
        then(systemFileMapper).should().deleteById(1L);
    }

    @Test
    @DisplayName("删除失败：计入 failed 且不删记录，下轮再试")
    void run_deleteFails_keepsRecord() throws Exception {
        given(systemFileMapper.selectList(any()))
                .willReturn(Collections.emptyList())
                .willReturn(Collections.singletonList(record(1L, SystemFileStatusEnum.TEMP.getCode())));
        Mockito.doThrow(new IllegalStateException("minio down"))
                .when(minioClient).removeObject(any(RemoveObjectArgs.class));

        FileCleanupResult result = cleanupService.run(false);

        assertThat(result.getFailed()).isGreaterThanOrEqualTo(1);
        assertThat(result.getObjectsDeleted()).isZero();
        then(systemFileMapper).should(never()).deleteById(any(Long.class));
    }

    @Test
    @DisplayName("无进展时停止：同一批失败不会无限重试")
    void run_noProgress_stops() throws Exception {
        // 两个状态各返回一条、且都删不掉：每个状态只尝试一次就停
        given(systemFileMapper.selectList(any()))
                .willReturn(Collections.singletonList(record(1L, SystemFileStatusEnum.UPLOADING.getCode())))
                .willReturn(Collections.singletonList(record(2L, SystemFileStatusEnum.TEMP.getCode())));
        Mockito.doThrow(new IllegalStateException("minio down"))
                .when(minioClient).removeObject(any(RemoveObjectArgs.class));

        cleanupService.run(false);

        then(minioClient).should(times(2)).removeObject(any(RemoveObjectArgs.class));
    }

    private SystemFileDO record(Long id, String status) {
        SystemFileDO record = new SystemFileDO();
        record.setId(id);
        record.setFileId("f-" + id);
        record.setObjectKey("1/202611/f-" + id + ".txt");
        record.setBucketName("test-bucket");
        record.setStatus(status);
        return record;
    }
}
