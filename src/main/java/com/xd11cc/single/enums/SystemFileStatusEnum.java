package com.xd11cc.single.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 文件状态。
 * <p>
 * 对应 {@code system_file.status}（char(2)），展示文案由字典 {@code system_file_status} 提供。
 * </p>
 * <p>
 * 三态是有意设计的：上传时先以 {@link #UPLOADING} 落库，对象写成功后再置为 {@link #TEMP}。
 * 这样进程若在两步之间中断，留下的是「可见的待清理记录」而不是「桶里不可见的孤儿对象」。
 * </p>
 *
 * @author xd11cc
 */
@Getter
@AllArgsConstructor
public enum SystemFileStatusEnum {

    /**
     * 上传中：记录已落库，对象尚未确认写入
     */
    UPLOADING("2"),

    /**
     * 临时：上传完成，但还没有被业务数据引用，超过保留期可回收
     */
    TEMP("0"),

    /**
     * 已引用：已被业务数据引用，不参与回收
     */
    REFERENCED("1"),
    ;

    /**
     * 状态码，与 system_file.status 一致
     */
    private final String code;
}
