package com.xd11cc.single.common.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 文件清理策略。
 *
 * @author xd11cc
 */
@Data
@Component
@ConfigurationProperties("file.cleanup")
public class FileCleanupProperties {

    /**
     * 「上传中」记录的超时小时数；超过则视为失败残留，删除对象与记录
     */
    private int pendingExpireHours = 1;

    /**
     * 「临时」文件（未被业务引用）的保留天数；超过则回收
     */
    private int tempRetentionDays = 7;

    /**
     * 试运行：只统计不删除，用于上线前确认影响面
     */
    private boolean dryRun = false;

    /**
     * 单批处理条数，避免一次拉太多
     */
    private int batchSize = 200;
}
