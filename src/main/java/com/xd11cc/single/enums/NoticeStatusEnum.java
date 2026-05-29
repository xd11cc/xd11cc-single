package com.xd11cc.single.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author xd11cc
 * @date 2026-05-29
 */
@Getter
@AllArgsConstructor
public enum NoticeStatusEnum {

    DRAFT(0, "草稿"),
    PUBLISHED(1, "已发布"),
    REVOKED(2, "已撤回"),
    ;

    private final Integer code;

    private final String info;
}
