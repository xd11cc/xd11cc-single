package com.xd11cc.single.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author xd11cc
 * @date 2026-05-28
 */
@Getter
@AllArgsConstructor
public enum LoginTypeEnum {

    PASSWORD("1", "密码登录"),
    SOCIAL("2", "社交登录"),
    LOGOUT("3", "退出登录");

    private final String code;
    private final String desc;
}
