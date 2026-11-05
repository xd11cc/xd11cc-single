package com.xd11cc.single.service;

/**
 * 文件回收。
 * <p>
 * 处理两类残留：
 * 1. 「上传中」超时的记录——上传过程崩在写对象前后的痕迹；
 * 2. 「临时」超期的文件——上传后一直没有被业务引用。
 * </p>
 *
 * @author xd11cc
 */
public interface FileCleanupService {

    /**
     * 按配置执行清理
     */
    FileCleanupResult run();

    /**
     * @param dryRun true 只统计不删除，用于上线前估算影响面
     */
    FileCleanupResult run(boolean dryRun);
}
