package com.xd11cc.single.service;

import lombok.Builder;
import lombok.Data;

/**
 * 文件清理结果，供定时任务与手动触发时观察影响面。
 *
 * @author xd11cc
 */
@Data
@Builder
public class FileCleanupResult {

    /**
     * 是否试运行（只统计不删除）
     */
    private boolean dryRun;

    /**
     * 回收的「上传中」记录数
     */
    private int pendingRemoved;

    /**
     * 回收的「临时」文件数
     */
    private int tempRemoved;

    /**
     * 成功删除的对象数
     */
    private int objectsDeleted;

    /**
     * 处理失败数（失败项保留，下次继续）
     */
    private int failed;
}
