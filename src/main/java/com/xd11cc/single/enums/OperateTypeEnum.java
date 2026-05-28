package com.xd11cc.single.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author xd11cc
 * @date 2026-05-28
 */
@Getter
@AllArgsConstructor
public enum OperateTypeEnum {

    ADD("1", "新增"),
    UPDATE("2", "修改"),
    DELETE("3", "删除"),
    EXPORT("4", "导出"),
    OTHER("5", "其他");

    private final String code;
    private final String desc;
}
