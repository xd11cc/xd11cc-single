package com.xd11cc.single.infrastructure.schedule.xxl;

import com.xxl.job.core.handler.annotation.XxlJob;
import com.xd11cc.single.service.FileCleanupResult;
import com.xd11cc.single.service.FileCleanupService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 文件回收任务。
 * <p>
 * 两个触发点：
 * - 定时：每小时清理「上传中」超时与「临时」超期文件；
 * - 手动：任务参数传 {@code dryRun} 时只统计不删除，用于上线前确认影响面。
 * </p>
 *
 * @author xd11cc
 */
@Slf4j
@Component
public class FileCleanupJob {

    private final FileCleanupService fileCleanupService;

    public FileCleanupJob(FileCleanupService fileCleanupService) {
        this.fileCleanupService = fileCleanupService;
    }

    @XxlJob("FileCleanupJob")
    public void cleanup(String param) {
        boolean dryRun = "dryRun".equalsIgnoreCase(param == null ? "" : param.trim());
        FileCleanupResult result = fileCleanupService.run(dryRun);
        log.info("[file-cleanup] 任务结束 dryRun={} pending={} temp={} objects={} failed={}",
                result.isDryRun(), result.getPendingRemoved(), result.getTempRemoved(),
                result.getObjectsDeleted(), result.getFailed());
    }
}
