package com.xd11cc.single.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.xd11cc.single.common.annotation.TenantIgnore;
import com.xd11cc.single.common.properties.FileCleanupProperties;
import com.xd11cc.single.entity.domain.SystemFileDO;
import com.xd11cc.single.enums.SystemFileStatusEnum;
import com.xd11cc.single.mapper.SystemFileMapper;
import com.xd11cc.single.service.FileCleanupResult;
import com.xd11cc.single.service.FileCleanupService;
import io.minio.MinioClient;
import io.minio.RemoveObjectArgs;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 文件回收实现。
 * <p>
 * 完全由数据库驱动：先按状态与时间捞出候选，再删对象、删记录。
 * 不依赖列举存储桶，避免全量扫描与误删正在上传的对象。
 * </p>
 * <p>
 * 删除对象是幂等的（S3 DELETE 对不存在的对象同样返回成功），
 * 所以「记录在、对象不在」的情况也能被自然清掉。
 * </p>
 *
 * @author xd11cc
 */
@Slf4j
@Service
public class FileCleanupServiceImpl implements FileCleanupService {

    private final SystemFileMapper systemFileMapper;

    private final MinioClient minioClient;

    private final FileCleanupProperties properties;

    public FileCleanupServiceImpl(SystemFileMapper systemFileMapper,
                                  MinioClient minioClient,
                                  FileCleanupProperties properties) {
        this.systemFileMapper = systemFileMapper;
        this.minioClient = minioClient;
        this.properties = properties;
    }

    @Override
    public FileCleanupResult run() {
        return run(properties.isDryRun());
    }

    /**
     * 跨租户扫描：清理是系统级操作，需要忽略租户过滤
     */
    @Override
    @TenantIgnore
    public FileCleanupResult run(boolean dryRun) {
        LocalDateTime pendingBefore = LocalDateTime.now().minusHours(properties.getPendingExpireHours());
        LocalDateTime tempBefore = LocalDateTime.now().minusDays(properties.getTempRetentionDays());

        FileCleanupResult result = FileCleanupResult.builder().dryRun(dryRun).build();
        sweep(SystemFileStatusEnum.UPLOADING.getCode(), pendingBefore, dryRun, result, true);
        sweep(SystemFileStatusEnum.TEMP.getCode(), tempBefore, dryRun, result, false);

        log.info("[file-cleanup] 完成 dryRun={} pending={} temp={} objects={} failed={}",
                dryRun, result.getPendingRemoved(), result.getTempRemoved(),
                result.getObjectsDeleted(), result.getFailed());
        return result;
    }

    private void sweep(String status, LocalDateTime before, boolean dryRun,
                       FileCleanupResult result, boolean pending) {
        int budget = properties.getBatchSize() * 10;
        int handled = 0;
        while (handled < budget) {
            List<SystemFileDO> batch = systemFileMapper.selectList(Wrappers.<SystemFileDO>lambdaQuery()
                    .eq(SystemFileDO::getStatus, status)
                    .lt(SystemFileDO::getCreateTime, before)
                    .last("LIMIT " + properties.getBatchSize()));
            if (batch.isEmpty()) {
                return;
            }
            int successThisRound = 0;
            for (SystemFileDO record : batch) {
                if (dryRun) {
                    count(result, pending);
                    continue;
                }
                if (removeOne(record)) {
                    count(result, pending);
                    result.setObjectsDeleted(result.getObjectsDeleted() + 1);
                    successThisRound++;
                } else {
                    result.setFailed(result.getFailed() + 1);
                }
            }
            if (dryRun) {
                return;
            }
            // 一轮下来没有任何进展，说明剩下的都删不掉，直接停，避免反复重试同一批
            if (successThisRound == 0) {
                return;
            }
            handled += successThisRound;
        }
        log.warn("[file-cleanup] 达到单次处理上限 status={} handled={}", status, handled);
    }

    /**
     * @return true 表示该记录已处理完（对象与记录都已删除或不存在）
     */
    private boolean removeOne(SystemFileDO record) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(record.getBucketName())
                    .object(record.getObjectKey())
                    .build());
            // 逻辑删除，记录不再参与查询
            systemFileMapper.deleteById(record.getId());
            return true;
        } catch (Exception e) {
            log.warn("[file-cleanup] 清理失败 id={} key={}", record.getId(), record.getObjectKey(), e);
            return false;
        }
    }

    private void count(FileCleanupResult result, boolean pending) {
        if (pending) {
            result.setPendingRemoved(result.getPendingRemoved() + 1);
        } else {
            result.setTempRemoved(result.getTempRemoved() + 1);
        }
    }
}
