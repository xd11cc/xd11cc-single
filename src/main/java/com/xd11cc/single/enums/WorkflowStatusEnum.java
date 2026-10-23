package com.xd11cc.single.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author xd11cc
 * @date 2026-05-29
 */
@AllArgsConstructor
@Getter
public enum WorkflowStatusEnum {

    RUNNING(0, "进行中"),
    COMPLETED(1, "已完成"),
    CANCELLED(2, "已取消"),
    REJECTED(3, "已驳回"),
    ;

    private final int code;
    private final String desc;
}
