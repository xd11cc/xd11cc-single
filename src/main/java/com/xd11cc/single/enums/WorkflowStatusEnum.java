package com.xd11cc.single.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 工作流实例状态。
 * <p>
 * 对应 {@code workflow_instance.status}（char(2)），展示文案由字典
 * {@code workflow_status} 提供，本枚举只承载<b>编译期取值契约</b>，
 * 不再重复维护中文文案，避免与字典分叉。
 * </p>
 *
 * @author xd11cc
 * @date 2026-05-29
 */
@AllArgsConstructor
@Getter
public enum WorkflowStatusEnum {

    /**
     * 进行中
     */
    RUNNING("0"),
    /**
     * 已完成
     */
    COMPLETED("1"),
    /**
     * 已取消
     */
    CANCELLED("2"),
    /**
     * 已驳回
     */
    REJECTED("3"),
    ;

    /**
     * 状态码，与 workflow_instance.status 一致
     */
    private final String code;
}
