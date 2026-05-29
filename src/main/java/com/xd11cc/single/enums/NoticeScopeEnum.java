package com.xd11cc.single.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author xd11cc
 * @date 2026-05-29
 */
@Getter
@AllArgsConstructor
public enum NoticeScopeEnum {

    ALL(1, "全部"),
    DEPT(2, "指定部门"),
    USER(3, "指定用户"),
    ;

    private final Integer code;

    private final String info;
}
