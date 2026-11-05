package com.xd11cc.single.service.impl;

import com.xd11cc.single.common.context.TenantContextHolder;
import com.xd11cc.single.common.exception.ServiceException;
import com.xd11cc.single.common.properties.MinioProperties;
import com.xd11cc.single.constants.PermissionConstants;
import com.xd11cc.single.entity.base.FileResultVO;
import com.xd11cc.single.entity.domain.SystemFileDO;
import com.xd11cc.single.entity.domain.SystemUserDO;
import com.xd11cc.single.entity.dto.LoginUserDTO;
import com.xd11cc.single.enums.SystemErrorEnum;
import com.xd11cc.single.enums.SystemFileStatusEnum;
import com.xd11cc.single.mapper.SystemFileMapper;
import com.xd11cc.single.service.FileContent;
import com.xd11cc.single.utils.SecurityUtils;
import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.RemoveObjectArgs;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

/**
 * {@link FileServiceImpl} 的测试。
 * <p>
 * 重点钉住三件事：
 * 1. 上传成功后必须落一条元数据，fileId 是 UUID（不再拼接原文件名）；
 * 2. 下载/预览/删除都要校验归属——非本人且非管理员一律拒绝；
 * 3. 元数据落库失败时把已上传的对象补偿删除，避免孤儿文件。
 * </p>
 *
 * @author xd11cc
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class FileServiceImplTest {

    private static final Long OWNER_ID = 1L;
    private static final Long OTHER_ID = 2L;
    private static final Long TENANT_ID = 10L;

    @Mock
    private MinioClient minioClient;

    @Mock
    private MinioProperties minioProperties;

    @Mock
    private SystemFileMapper systemFileMapper;

    private FileServiceImpl fileService;

    private MockedStatic<SecurityUtils> securityUtils;

    private MockedStatic<TenantContextHolder> tenantContextHolder;

    @BeforeEach
    void setUp() throws Exception {
        fileService = new FileServiceImpl(minioClient, minioProperties, systemFileMapper);
        given(minioProperties.getBucketName()).willReturn("test-bucket");

        securityUtils = Mockito.mockStatic(SecurityUtils.class);
        securityUtils.when(SecurityUtils::getRequireUserId).thenReturn(OWNER_ID);
        securityUtils.when(SecurityUtils::getUserId).thenReturn(OWNER_ID);

        tenantContextHolder = Mockito.mockStatic(TenantContextHolder.class);
        tenantContextHolder.when(TenantContextHolder::getTenantId).thenReturn(TENANT_ID);

        // 模拟主键回填，便于断言后续的 updateById / deleteById
        given(systemFileMapper.insert(any(SystemFileDO.class))).willAnswer(invocation -> {
            invocation.getArgument(0, SystemFileDO.class).setId(1L);
            return 1;
        });
    }

    @AfterEach
    void tearDown() throws Exception {
        securityUtils.close();
        tenantContextHolder.close();
    }

    // ==================== 上传 ====================

    @Test
    @DisplayName("上传成功：先落「上传中」记录，对象写成功后再置为「临时」")
    void upload_success() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "请假单.pdf", "application/pdf", "hello".getBytes(StandardCharsets.UTF_8));

        FileResultVO result = fileService.upload(file);
        String fileId = result.getFileId();

        assertThat(fileId).matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
        ArgumentCaptor<SystemFileDO> captor = ArgumentCaptor.forClass(SystemFileDO.class);
        then(systemFileMapper).should().insert(captor.capture());
        SystemFileDO saved = captor.getValue();
        assertThat(saved.getFileId()).isEqualTo(fileId);
        assertThat(saved.getOriginalName()).isEqualTo("请假单.pdf");
        assertThat(saved.getContentType()).isEqualTo("application/pdf");
        assertThat(saved.getBucketName()).isEqualTo("test-bucket");
        // 落库时是「上传中」，对象写完后才更新为「临时」
        assertThat(saved.getStatus()).isEqualTo(SystemFileStatusEnum.UPLOADING.getCode());
        assertThat(saved.getTenantId()).isEqualTo(TENANT_ID);
        assertThat(saved.getObjectKey()).startsWith(TENANT_ID + "/").endsWith(".pdf");

        ArgumentCaptor<SystemFileDO> updateCaptor = ArgumentCaptor.forClass(SystemFileDO.class);
        then(systemFileMapper).should().updateById(updateCaptor.capture());
        assertThat(updateCaptor.getValue().getStatus()).isEqualTo(SystemFileStatusEnum.TEMP.getCode());
    }

    @Test
    @DisplayName("上传空文件：直接拒绝，不访问存储")
    void upload_emptyFile() throws Exception {
        MockMultipartFile empty = new MockMultipartFile("file", "a.txt", "text/plain", new byte[0]);

        assertThatThrownBy(() -> fileService.upload(empty))
                .isInstanceOf(ServiceException.class)
                .satisfies(e -> assertThat(((ServiceException) e).getErrorCode())
                        .isEqualTo(SystemErrorEnum.MINIO_UPLOAD_FILE));
        then(minioClient).should(never()).putObject(any());
    }

    @Test
    @DisplayName("记录落库失败：不写对象，直接失败（不会产生孤儿对象）")
    void upload_recordInsertFails_noObjectWritten() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "a.txt", "text/plain", "x".getBytes(StandardCharsets.UTF_8));
        given(systemFileMapper.insert(any(SystemFileDO.class)))
                .willThrow(new IllegalStateException("db down"));

        assertThatThrownBy(() -> fileService.upload(file))
                .isInstanceOf(ServiceException.class);

        then(minioClient).should(never()).putObject(any());
    }

    @Test
    @DisplayName("对象写入失败：删掉刚落的记录，不留悬空记录")
    void upload_putFails_removesRecord() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "a.txt", "text/plain", "x".getBytes(StandardCharsets.UTF_8));
        Mockito.doThrow(new IllegalStateException("minio down"))
                .when(minioClient).putObject(any());

        assertThatThrownBy(() -> fileService.upload(file))
                .isInstanceOf(ServiceException.class)
                .satisfies(e -> assertThat(((ServiceException) e).getErrorCode())
                        .isEqualTo(SystemErrorEnum.MINIO_UPLOAD_FILE));

        then(systemFileMapper).should().deleteById(any(Long.class));
    }

    // ==================== 归属校验 ====================

    @Test
    @DisplayName("文件不存在：抛 FILE_NOT_FOUND")
    void preview_notFound() throws Exception {
        given(systemFileMapper.selectOne(any())).willReturn(null);

        assertThatThrownBy(() -> fileService.getPreviewUrl("missing"))
                .isInstanceOf(ServiceException.class)
                .satisfies(e -> assertThat(((ServiceException) e).getErrorCode())
                        .isEqualTo(SystemErrorEnum.FILE_NOT_FOUND));
    }

    @Test
    @DisplayName("非上传人且非管理员：抛 FILE_ACCESS_DENIED（越权修复的核心）")
    void preview_notOwner_denied() throws Exception {
        given(systemFileMapper.selectOne(any())).willReturn(recordOf(OWNER_ID));
        securityUtils.when(SecurityUtils::getUserId).thenReturn(OTHER_ID);
        securityUtils.when(SecurityUtils::getLoginUser).thenReturn(loginUser(false));

        assertThatThrownBy(() -> fileService.getPreviewUrl("f-1"))
                .isInstanceOf(ServiceException.class)
                .satisfies(e -> assertThat(((ServiceException) e).getErrorCode())
                        .isEqualTo(SystemErrorEnum.FILE_ACCESS_DENIED));
        then(minioClient).should(never()).getPresignedObjectUrl(any());
    }

    @Test
    @DisplayName("上传人本人：允许获取预览地址")
    void preview_owner_allowed() throws Exception {
        given(systemFileMapper.selectOne(any())).willReturn(recordOf(OWNER_ID));
        given(minioClient.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class)))
                .willReturn("http://minio/presigned");

        assertThat(fileService.getPreviewUrl("f-1")).isEqualTo("http://minio/presigned");
    }

    @Test
    @DisplayName("管理员（*:*:*）：可访问他人文件")
    void preview_admin_allowed() throws Exception {
        given(systemFileMapper.selectOne(any())).willReturn(recordOf(OWNER_ID));
        securityUtils.when(SecurityUtils::getUserId).thenReturn(OTHER_ID);
        securityUtils.when(SecurityUtils::getLoginUser).thenReturn(loginUser(true));
        given(minioClient.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class)))
                .willReturn("http://minio/presigned");

        assertThat(fileService.getPreviewUrl("f-1")).isEqualTo("http://minio/presigned");
    }

    // ==================== 下载 ====================

    @Test
    @DisplayName("打开文件流：返回内容与元数据，调用方负责关闭")
    void open_owner_streamsContent() throws Exception {
        SystemFileDO record = recordOf(OWNER_ID);
        record.setOriginalName("请假 单.pdf");
        given(systemFileMapper.selectOne(any())).willReturn(record);

        GetObjectResponse objectResponse = Mockito.mock(GetObjectResponse.class);
        given(minioClient.getObject(any(GetObjectArgs.class))).willReturn(objectResponse);

        try (FileContent content = fileService.open("f-1")) {
            assertThat(content.getOriginalName()).isEqualTo("请假 单.pdf");
            assertThat(content.getContentType()).isEqualTo("application/pdf");
            assertThat(content.getStream()).isSameAs(objectResponse);
        }
        // 关闭后应释放底层流
        then(objectResponse).should().close();
    }

    // ==================== 删除 ====================

    @Test
    @DisplayName("删除本人文件：删对象并逻辑删除记录")
    void batchDelete_owner() throws Exception {
        given(systemFileMapper.selectOne(any())).willReturn(recordOf(OWNER_ID));

        List<String> failed = fileService.batchDelete(Collections.singletonList("f-1"));

        assertThat(failed).isEmpty();
        then(minioClient).should().removeObject(any(RemoveObjectArgs.class));
        then(systemFileMapper).should().deleteById(1L);
    }

    @Test
    @DisplayName("批量删除：不存在的 id 视为已删除，不报错（幂等）")
    void batchDelete_unknownId_isIdempotent() throws Exception {
        given(systemFileMapper.selectOne(any()))
                .willReturn(recordOf(OWNER_ID))
                .willReturn(null);

        List<String> failed = fileService.batchDelete(Arrays.asList("f-1", "f-2"));

        assertThat(failed).isEmpty();
        then(minioClient).should(org.mockito.Mockito.times(1)).removeObject(any(RemoveObjectArgs.class));
        then(systemFileMapper).should(org.mockito.Mockito.times(1)).deleteById(any(Long.class));
    }

    @Test
    @DisplayName("批量删除：含无权访问的文件时整体拒绝，不产生部分删除")
    void batchDelete_denied_failsFast() throws Exception {
        given(systemFileMapper.selectOne(any())).willReturn(recordOf(OWNER_ID));
        securityUtils.when(SecurityUtils::getUserId).thenReturn(OTHER_ID);
        securityUtils.when(SecurityUtils::getLoginUser).thenReturn(loginUser(false));

        assertThatThrownBy(() -> fileService.batchDelete(Arrays.asList("f-1", "f-2")))
                .isInstanceOf(ServiceException.class)
                .satisfies(e -> assertThat(((ServiceException) e).getErrorCode())
                        .isEqualTo(SystemErrorEnum.FILE_ACCESS_DENIED));
        then(minioClient).should(never()).removeObject(any());
    }

    @Test
    @DisplayName("批量删除：存储层失败时返回失败清单，交由调用方重试")
    void batchDelete_minioFailure() throws Exception {
        given(systemFileMapper.selectOne(any())).willReturn(recordOf(OWNER_ID));
        Mockito.doThrow(new IllegalStateException("minio down"))
                .when(minioClient).removeObject(any(RemoveObjectArgs.class));

        List<String> failed = fileService.batchDelete(Collections.singletonList("f-1"));

        assertThat(failed).containsExactly("f-1");
        then(systemFileMapper).should(never()).deleteById(any(Long.class));
    }

    // ==================== 夹具 ====================

    private SystemFileDO recordOf(Long ownerId) {
        SystemFileDO record = new SystemFileDO();
        record.setId(1L);
        record.setFileId("f-1");
        record.setObjectKey(TENANT_ID + "/202611/f-1.pdf");
        record.setBucketName("test-bucket");
        record.setOriginalName("a.pdf");
        record.setContentType("application/pdf");
        record.setSizeBytes(10L);
        record.setCreateUserId(ownerId);
        record.setTenantId(TENANT_ID);
        return record;
    }

    private LoginUserDTO loginUser(boolean admin) {
        SystemUserDO user = new SystemUserDO();
        user.setId(OTHER_ID);
        user.setTenantId(TENANT_ID);
        return new LoginUserDTO(
                admin ? new HashSet<>(Collections.singletonList(PermissionConstants.ALL_PERMISSION))
                        : new HashSet<>(),
                user);
    }
}
