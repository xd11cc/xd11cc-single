package com.xd11cc.single.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 通知状态。
 * <p>
 * 对应 {@code system_notice.status}（char(2)），展示文案由字典
 * {@code system_notice_status} 提供，本枚举只承载<b>编译期取值契约</b>。
 * </p>
 *
 * @author xd11cc
 * @date 2026-05-29
 */
@Getter
@AllArgsConstructor
public enum NoticeStatusEnum {

    /**
     * 草稿
     */
    DRAFT("0"),
    /**
     * 已发布
     */
    PUBLISHED("1"),
    /**
     * 已撤回
     */
    REVOKED("2"),
    ;

    /**
     * 状态码，与 system_notice.status 一致
     */
    private final String code;
}
