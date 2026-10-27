package com.xd11cc.single.common.mq.enums;

import lombok.Getter;

/**
 * MQ 队列定义。
 * <p>
 * 一个枚举项同时持有队列名、交换机名与路由键，三者绑定在一起，
 * 避免分散配置导致「消息发出但路由不到队列」的静默失败。
 * </p>
 *
 * <p>
 * 约定：
 * <ul>
 *     <li>队列名硬编码在代码中，不随环境变化</li>
 *     <li>一个队列只承载一种业务类型，消费端靠队列名区分语义</li>
 *     <li>{@code delayed=true} 的队列使用 {@code x-delayed-message} 交换机，
 *         支持任意时长延迟投递（需启用 rabbitmq_delayed_message_exchange 插件）</li>
 * </ul>
 * </p>
 *
 * @author xd11cc
 */
@Getter
public enum MqQueueEnum {

    /**
     * 工作流发起队列
     */
    WORKFLOW_START("workflow_start_queue", "workflow_exchange", "workflow.start", false),

    /**
     * 演示延迟队列。
     * <p>
     * 使用 {@code x-delayed-message} 交换机，投递时长由发送方通过 {@code x-delay}
     * 指定（单位毫秒），到期后才路由到队列。业务接入后可删除。
     * </p>
     */
    DEMO_DELAY("mq_demo_delay_queue", "mq_demo_delay_exchange", "mq.demo.delay", true),

    /**
     * 演示队列。
     * <p>仅用于验证 MQ 基础设施（发送、重试、失败落表、死信），业务接入后可删除。</p>
     */
    DEMO("mq_demo_queue", "mq_demo_exchange", "mq.demo", false),

    /**
     * 全局重试队列（延迟队列）。
     * <p>
     * 所有业务队列消费失败后都投递到这里，由延迟交换机暂存指定时长，
     * 到期后回流到原业务队列重新消费。这样重试的「等待」发生在 broker 侧，
     * 不占用消费线程。
     * </p>
     * <p>
     * 回流目标由消息头 {@code x-original-queue} 指定，因此新增业务队列
     * 无需为本队列增加配置。
     * </p>
     */
    RETRY("mq_retry_queue", "mq_retry_exchange", "mq.retry", true),
    ;

    /**
     * 队列名
     */
    private final String queue;

    /**
     * 交换机名
     */
    private final String exchange;

    /**
     * 路由键
     */
    private final String routingKey;

    /**
     * 是否为延迟队列（使用 x-delayed-message 交换机）
     */
    private final boolean delayed;

    MqQueueEnum(String queue, String exchange, String routingKey, boolean delayed) {
        this.queue = queue;
        this.exchange = exchange;
        this.routingKey = routingKey;
        this.delayed = delayed;
    }
}
