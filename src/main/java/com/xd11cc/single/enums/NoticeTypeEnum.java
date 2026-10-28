package com.xd11cc.single.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 通知类型。
 * <p>
 * 对应 {@code system_notice.type}（char(2)），展示文案由字典
 * {@code system_notice_type} 提供，本枚举只承载<b>编译期取值契约</b>。
 * </p>
 *
 * @author xd11cc
 * @date 2026-05-29
 */
@Getter
@AllArgsConstructor
public enum NoticeTypeEnum {

    /**
     * 通知
     */
    NOTICE("1"),
    /**
     * 消息
     */
    MESSAGE("2"),
    /**
     * 待办
     */
    TODO("3"),
    ;

    /**
     * 类型码，与 system_notice.type 一致
     */
    private final String code;
}
