package com.xd11cc.single.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 通知已读状态。
 * <p>
 * 对应 {@code system_notice_user.read_status}（char(2)），展示文案由字典
 * {@code system_read_status} 提供。
 * </p>
 * <p>
 * 此前该状态在 Service 中以 {@code 0}/{@code 1} 字面量散落 7 处，
 * 现收敛为本枚举，与其他状态枚举形态一致。
 * </p>
 *
 * @author xd11cc
 */
@Getter
@AllArgsConstructor
public enum ReadStatusEnum {

    /**
     * 未读
     */
    UNREAD("0"),
    /**
     * 已读
     */
    READ("1"),
    ;

    /**
     * 状态码，与 system_notice_user.read_status 一致
     */
    private final String code;
}
