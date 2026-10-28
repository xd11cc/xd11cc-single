package com.xd11cc.single.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 通知范围。
 * <p>
 * 对应 {@code system_notice.scope}（char(2)），展示文案由字典
 * {@code system_notice_scope} 提供，本枚举只承载<b>编译期取值契约</b>。
 * </p>
 *
 * @author xd11cc
 * @date 2026-05-29
 */
@Getter
@AllArgsConstructor
public enum NoticeScopeEnum {

    /**
     * 全部
     */
    ALL("1"),
    /**
     * 指定部门
     */
    DEPT("2"),
    /**
     * 指定用户
     */
    USER("3"),
    ;

    /**
     * 范围码，与 system_notice.scope 一致
     */
    private final String code;
}
