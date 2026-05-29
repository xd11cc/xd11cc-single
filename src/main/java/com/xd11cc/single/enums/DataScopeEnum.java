package com.xd11cc.single.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author xd11cc
 * @date 2026-05-28
 */
@Getter
@AllArgsConstructor
public enum DataScopeEnum {

    ALL("1", "全部数据"),
    DEPT_AND_SUB("2", "本部门及下级"),
    DEPT("3", "仅本部门"),
    SELF("4", "仅本人"),
    CUSTOM("5", "自定义"),
    ;

    private final String code;

    private final String info;
}
