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
     * 预览地址
     */
    private String previewUrl;
}
