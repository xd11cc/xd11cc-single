package com.xd11cc.single.common.mq.comsumer;

import com.xd11cc.single.common.mq.message.MqMessage;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * MQ 演示消息。
 * <p>
 * 演示业务消息如何继承 {@link MqMessage}：
 * messageId、tenantId 由基类提供并在发送时自动填充，子类只关注业务字段。
 * </p>
 *
 * @author xd11cc
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DemoMessage extends MqMessage {

    private static final long serialVersionUID = 1L;

    /**
     * 业务标识，便于在日志与失败记录中定位
     */
    private String bizId;

    /**
     * 演示内容
     */
    private String content;

    /**
     * 模拟处理耗时（毫秒），用于演示消费重试
     */
    private Long costMillis;

    /**
     * 是否为失败消息：true 时消费者主动抛异常，用于观察「重试 → 落表 → 进死信」链路
     */
    private Boolean simulateFail;

    /**
     * 失败消息的失败次数，用于验证重试逻辑
     */
    private Integer failRetryTimes;
}
