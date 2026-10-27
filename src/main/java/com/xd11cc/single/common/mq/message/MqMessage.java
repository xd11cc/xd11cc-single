package com.xd11cc.single.common.mq.message;

import lombok.Data;

import java.io.Serializable;

/**
 * MQ 消息基类。
 * <p>
 * 业务消息直接继承本类，业务字段写在子类中；不设通用 payload 字段，
 * 消费端可将消息直接反序列化为具体子类，保证类型安全。
 * </p>
 *
 * <p>约定：</p>
 * <ul>
 *     <li>{@code messageId} 由发送端生成，用于幂等与失败追踪</li>
 *     <li>{@code tenantId} 标识目标租户，消费端据此设置租户上下文</li>
 * </ul>
 *
 * <pre>
 * public class WorkflowStartMessage extends MqMessage {
 *     private String processDefinitionKey;
 *     private String businessKey;
 * }
 * </pre>
 *
 * @author xd11cc
 */
@Data
public class MqMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 消息唯一标识，发送端生成（UUID）
     */
    private String messageId;

    /**
     * 目标租户 ID
     */
    private Long tenantId;

    /**
     * 已重试次数（由消费框架维护，业务代码无需关心）。
     * <p>
     * 延迟重试时写入消息体，随消息回到业务队列，用于判断是否已达重试上限。
     * </p>
     */
    private Integer retryCount;
}
