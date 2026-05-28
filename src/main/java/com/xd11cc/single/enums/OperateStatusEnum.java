package com.xd11cc.single.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author xd11cc
 * @date 2026-05-28
 */
@Getter
@AllArgsConstructor
public enum OperateStatusEnum {

    SUCCESS("0", "成功"),
    FAIL("1", "失败");

    private final String code;
    private final String desc;
}
