package com.xd11cc.single.entity.base;

import lombok.Builder;
import lombok.Data;

/**
 * @author xd11cc
 * @date 2026-05-08 11:08:41
 * @description
 */
@Builder
@Data
public class FileResultVO {

    /**
     * 文件id
     */
    private String fileId;

    /**
     * 对象键（相对桶的路径，如 1/2026-11/uuid.jpg）。
     * 需要长期保存引用时用这个字段，而不是 previewUrl —— 后者带签名且会过期。
     */
    private String objectKey;

    /**
     * 预览地址，含签名，有效期 120 分钟
     */
    private String previewUrl;

    /**
     * 原始文件名，仅用于展示与下载命名
     */
    private String originalName;

    /**
     * 文件大小（字节）
     */
    private Long sizeBytes;

    /**
     * MIME 类型
     */
    private String contentType;
}
