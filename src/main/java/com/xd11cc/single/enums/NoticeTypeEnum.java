package com.xd11cc.single.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author xd11cc
 * @date 2026-05-29
 */
@Getter
@AllArgsConstructor
public enum NoticeTypeEnum {

    NOTICE(1, "通知"),
    MESSAGE(2, "消息"),
    TODO(3, "待办"),
    ;

    private final Integer code;

    private final String info;
}
