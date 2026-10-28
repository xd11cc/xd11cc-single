package com.xd11cc.single.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * MQ 失败消息状态。
 * <p>
 * 对应 {@code mq_fail_message.status}（char(2)），展示文案由字典
 * {@code mq_fail_status} 提供，本枚举只承载<b>编译期取值契约</b>。
 * </p>
 * <p>
 * 原先散落在 {@code MqFailMessageServiceImpl} 里的 int 常量已收敛到这里，
 * 与 {@link WorkflowStatusEnum}、{@link NoticeStatusEnum} 保持同一形态。
 * </p>
 *
 * @author xd11cc
 */
@Getter
@AllArgsConstructor
public enum MqFailStatus {

    /**
     * 待处理
     */
    PENDING("0"),
    /**
     * 已重放
     */
    REPLAYED("1"),
    /**
     * 已忽略
     */
    IGNORED("2"),
    ;

    /**
     * 状态码，与 mq_fail_message.status 一致
     */
    private final String code;
}
