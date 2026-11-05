package com.xd11cc.single.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.xd11cc.single.common.context.TenantContextHolder;
import com.xd11cc.single.common.exception.ServiceException;
import com.xd11cc.single.common.properties.MinioProperties;
import com.xd11cc.single.constants.PermissionConstants;
import com.xd11cc.single.entity.base.FileResultVO;
import com.xd11cc.single.entity.domain.SystemFileDO;
import com.xd11cc.single.entity.dto.LoginUserDTO;
import com.xd11cc.single.enums.SystemErrorEnum;
import com.xd11cc.single.enums.SystemFileStatusEnum;
import com.xd11cc.single.mapper.SystemFileMapper;
import com.xd11cc.single.service.FileContent;
import com.xd11cc.single.service.FileService;
import com.xd11cc.single.utils.SecurityUtils;
import io.minio.GetObjectArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.http.Method;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author xd11cc
 * @date 2026-05-08 11:07:40
 * @description 文件服务。文件必须先落库再允许访问，下载/预览/删除都要校验归属。
 */
@Slf4j
@Service
public class FileServiceImpl implements FileService {

    /**
     * 预签名 URL 有效期（分钟）。不设过期会走 SDK 默认的 7 天，等于长期有效的链接
     */
    private static final int PREVIEW_URL_MINUTES = 120;

    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyyMM");

    private static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";

    private final MinioClient minioClient;

    private final MinioProperties minioProperties;

    private final SystemFileMapper systemFileMapper;

    public FileServiceImpl(MinioClient minioClient,
                           MinioProperties minioProperties,
                           SystemFileMapper systemFileMapper) {
        this.minioClient = minioClient;
        this.minioProperties = minioProperties;
        this.systemFileMapper = systemFileMapper;
    }

    @Override
    public FileResultVO upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ServiceException(SystemErrorEnum.MINIO_UPLOAD_FILE);
        }
        try (InputStream in = file.getInputStream()) {
            return upload(in, file.getSize(), file.getOriginalFilename(), file.getContentType());
        } catch (IOException e) {
            throw new ServiceException(SystemErrorEnum.MINIO_UPLOAD_FILE, e);
        }
    }

    /**
     * 上传任意流。不关闭传入的流，由调用方管理生命周期。
     * <p>
     * 顺序是「先落记录 → 再写对象 → 最后置为临时」，而不是反过来：
     * 若中途崩溃，留下的是库里可见的「上传中」记录，清理任务能扫到并回收；
     * 反过来先写对象的话，崩溃会留下桶里完全不可见的孤儿对象。
     * </p>
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public FileResultVO upload(InputStream in, long size, String filename, String contentType) {
        if (in == null || size <= 0) {
            throw new ServiceException(SystemErrorEnum.MINIO_UPLOAD_FILE);
        }
        Long userId = SecurityUtils.getRequireUserId();
        Long tenantId = TenantContextHolder.getTenantId();
        String bucketName = minioProperties.getBucketName();
        String fileId = UUID.randomUUID().toString();
        String objectKey = buildObjectKey(tenantId, fileId, filename);
        String type = contentType == null || contentType.trim().isEmpty()
                ? DEFAULT_CONTENT_TYPE : contentType;

        // 第一步：先落「上传中」记录
        SystemFileDO record = new SystemFileDO();
        record.setFileId(fileId);
        record.setObjectKey(objectKey);
        record.setBucketName(bucketName);
        record.setOriginalName(filename);
        record.setContentType(type);
        record.setSizeBytes(size);
        record.setStatus(SystemFileStatusEnum.UPLOADING.getCode());
        record.setCreateUserId(userId);
        record.setUpdateUserId(userId);
        record.setTenantId(tenantId);
        try {
            systemFileMapper.insert(record);
        } catch (Exception e) {
            // 记录都没落成，对象更不会写，直接失败即可——不会产生孤儿对象
            throw new ServiceException(SystemErrorEnum.MINIO_UPLOAD_FILE, e);
        }

        // 第二步：写对象；失败则删掉刚落的记录（对象本来就没写成功）
        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectKey)
                    .stream(in, size, -1)
                    .contentType(type)
                    .build());
        } catch (Exception e) {
            removeRecordQuietly(record.getId());
            throw new ServiceException(SystemErrorEnum.MINIO_UPLOAD_FILE, e);
        }

        // 第三步：对象确认写入，置为「临时」；此处失败也不要删对象，
        // 记录仍是「上传中」，交给清理任务连同对象一起回收
        SystemFileDO update = new SystemFileDO();
        update.setId(record.getId());
        update.setStatus(SystemFileStatusEnum.TEMP.getCode());
        update.setUpdateUserId(userId);
        systemFileMapper.updateById(update);

        return buildResult(record);
    }

    @Override
    public FileContent open(String fileId) {
        SystemFileDO record = requireAccessible(fileId);
        try {
            InputStream in = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(record.getBucketName())
                    .object(record.getObjectKey())
                    .build());
            return new FileContent(in, record.getOriginalName(), record.getContentType());
        } catch (Exception e) {
            throw new ServiceException(SystemErrorEnum.MINIO_DOWNLOAD_FILE, e);
        }
    }

    @Override
    public String getPreviewUrl(String fileId) {
        SystemFileDO record = requireAccessible(fileId);
        try {
            return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .bucket(record.getBucketName())
                    .object(record.getObjectKey())
                    .method(Method.GET)
                    .expiry(PREVIEW_URL_MINUTES, TimeUnit.MINUTES)
                    .build());
        } catch (Exception e) {
            throw new ServiceException(SystemErrorEnum.MINIO_GET_FILE, e);
        }
    }

    /**
     * 先统一校验归属，避免「删了一半才发现某个文件无权访问」造成部分删除；
     * 再逐个删对象，失败项收集后返回，由调用方决定是否重试。
     */
    @Override
    public List<String> batchDelete(List<String> fileIds) {
        if (CollectionUtils.isEmpty(fileIds)) {
            return new ArrayList<>();
        }
        List<SystemFileDO> records = new ArrayList<>(fileIds.size());
        for (String fileId : fileIds) {
            SystemFileDO record = findAccessible(fileId);
            // 不存在（含已删除）视为已完成，保证删除接口幂等
            if (record != null) {
                records.add(record);
            }
        }
        List<String> failed = new ArrayList<>();
        for (SystemFileDO record : records) {
            try {
                minioClient.removeObject(RemoveObjectArgs.builder()
                        .bucket(record.getBucketName())
                        .object(record.getObjectKey())
                        .build());
                // 逻辑删除，fileId 不再可访问
                systemFileMapper.deleteById(record.getId());
            } catch (Exception e) {
                log.warn("[file] 删除失败 fileId={}", record.getFileId(), e);
                failed.add(record.getFileId());
            }
        }
        return failed;
    }

    /**
     * 读取文件记录并校验归属。
     * <p>
     * 租户隔离由多租户拦截器保证：跨租户查询查不到记录，返回「文件不存在」而不是「无权访问」，
     * 避免向调用方泄露其它租户是否存在该文件。
     * </p>
     */
    private SystemFileDO requireAccessible(String fileId) {
        SystemFileDO record = findAccessible(fileId);
        if (record == null) {
            throw new ServiceException(SystemErrorEnum.FILE_NOT_FOUND);
        }
        return record;
    }

    /**
     * 查记录并校验归属：查不到返回 null（已删除或不属于当前租户），查到但无权访问则抛异常。
     * <p>
     * 删除场景把 null 当作「已经删掉了」处理，保证接口幂等；
     * 下载/预览场景则把 null 报成「文件不存在」。
     * </p>
     */
    private SystemFileDO findAccessible(String fileId) {
        if (fileId == null || fileId.trim().isEmpty()) {
            return null;
        }
        SystemFileDO record = systemFileMapper.selectOne(Wrappers.<SystemFileDO>lambdaQuery()
                .eq(SystemFileDO::getFileId, fileId.trim()));
        if (record != null && !canAccess(record)) {
            throw new ServiceException(SystemErrorEnum.FILE_ACCESS_DENIED);
        }
        return record;
    }

    /**
     * 上传人本人，或拥有全部权限的管理员
     */
    private boolean canAccess(SystemFileDO record) {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            return false;
        }
        if (userId.equals(record.getCreateUserId())) {
            return true;
        }
        LoginUserDTO loginUser = SecurityUtils.getLoginUser();
        return loginUser != null && loginUser.getPermissions() != null
                && loginUser.getPermissions().contains(PermissionConstants.ALL_PERMISSION);
    }

    private FileResultVO buildResult(SystemFileDO record) {
        String previewUrl = null;
        try {
            previewUrl = minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .bucket(record.getBucketName())
                    .object(record.getObjectKey())
                    .method(Method.GET)
                    .expiry(PREVIEW_URL_MINUTES, TimeUnit.MINUTES)
                    .build());
        } catch (Exception e) {
            // 预览地址拿不到不影响文件本身可用：记录已在库里，客户端可稍后重新获取
            log.warn("[file] 生成预览地址失败 fileId={}", record.getFileId(), e);
        }
        return FileResultVO.builder()
                .fileId(record.getFileId())
                .objectKey(record.getObjectKey())
                .previewUrl(previewUrl)
                .originalName(record.getOriginalName())
                .sizeBytes(record.getSizeBytes())
                .contentType(record.getContentType())
                .build();
    }

    /**
     * object key 带租户与月份前缀：既避免跨租户同名覆盖，也让存储层按租户归档
     */
    private String buildObjectKey(Long tenantId, String fileId, String originalName) {
        String extension = extensionOf(originalName);
        String prefix = (tenantId == null ? "0" : tenantId.toString())
                + "/" + LocalDate.now().format(MONTH_FORMATTER) + "/" + fileId;
        return extension.isEmpty() ? prefix : prefix + "." + extension;
    }

    private String extensionOf(String fileName) {
        if (fileName == null) {
            return "";
        }
        int index = fileName.lastIndexOf('.');
        if (index < 0 || index == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(index + 1).replaceAll("[^A-Za-z0-9]", "");
    }

    /**
     * 删除失败时只回滚记录：删不掉也没关系，记录会停在「上传中」，由清理任务兜底
     */
    private void removeRecordQuietly(Long id) {
        try {
            systemFileMapper.deleteById(id);
        } catch (Exception e) {
            log.warn("[file] 回滚记录失败 id={}，将依赖清理任务回收", id, e);
        }
    }
}
