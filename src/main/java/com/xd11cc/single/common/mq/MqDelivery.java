package com.xd11cc.single.common.mq;

import com.rabbitmq.client.Channel;
import com.xd11cc.single.common.context.TenantContextHolder;
import com.xd11cc.single.common.mq.enums.MqQueueEnum;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;

import java.io.IOException;

/**
 * 一次消息投递的上下文。
 * <p>
 * 把 {@link MqQueueEnum}、原始 {@link Message}、{@link Channel}、deliveryTag
 * 这四个「永远一起出现」的参数收拢成一个对象，
 * 业务消费者不必再逐个透传给 {@link MqConsumeTemplate}。
 * </p>
 *
 * <p>业务侧只需构造一次：</p>
 * <pre>{@code
 * MqDelivery delivery = MqDelivery.of(MqQueueEnum.DEMO, rawMessage, channel, deliveryTag);
 * mqConsumeTemplate.consume(delivery, () -> handleBusiness(message));
 * }</pre>
 *
 * <p>
 * ACK / NACK / 失败落表全部由 {@link MqConsumeTemplate} 通过本对象完成，
 * 业务代码不应直接调用 {@link #ack()} / {@link #nack()}。
 * </p>
 *
 * @author xd11cc
 */
@Slf4j
@Getter
public class MqDelivery {

    /**
     * 来源队列
     */
    private final MqQueueEnum queue;

    /**
     * 原始消息（失败落表、取消息头时需要）
     */
    private final Message message;

    /**
     * 投递通道，用于 ACK / NACK
     */
    private final Channel channel;

    /**
     * 投递标签，用于 ACK / NACK
     */
    private final long deliveryTag;

    private MqDelivery(MqQueueEnum queue, Message message, Channel channel, long deliveryTag) {
        this.queue = queue;
        this.message = message;
        this.channel = channel;
        this.deliveryTag = deliveryTag;
    }

    /**
     * 构造投递上下文。
     *
     * @param queue       来源队列
     * @param message     原始消息
     * @param channel     投递通道
     * @param deliveryTag 投递标签
     */
    public static MqDelivery of(MqQueueEnum queue, Message message, Channel channel, long deliveryTag) {
        return new MqDelivery(queue, message, channel, deliveryTag);
    }

    /**
     * 消息 ID（优先取 AMQP 属性里的 messageId，退化为自填的 header）。
     */
    public String getMessageId() {
        String messageId = message.getMessageProperties().getMessageId();
        if (messageId != null) {
            return messageId;
        }
        Object header = message.getMessageProperties().getHeader(MqMessageSender.HEADER_MESSAGE_ID);
        return header == null ? null : header.toString();
    }

    /**
     * 消息体全限定类名（Jackson {@code __TypeId__}）。
     * <p>失败落表时保存，重放时用于还原类型，否则消费端无法按类型分派。</p>
     */
    public String getMessageType() {
        Object typeId = message.getMessageProperties().getHeader("__TypeId__");
        return typeId == null ? null : typeId.toString();
    }

    /**
     * 消息体原始字节。
     */
    public byte[] getBody() {
        return message.getBody();
    }

    /**
     * 消息所属租户：优先消息头，其次当前上下文。
     */
    public Long getTenantId() {
        Object header = message.getMessageProperties().getHeader(MqMessageSender.HEADER_TENANT_ID);
        if (header instanceof Number) {
            return ((Number) header).longValue();
        }
        if (header != null) {
            try {
                return Long.valueOf(header.toString());
            } catch (NumberFormatException ignored) {
                // 头部格式非法时退化为上下文
            }
        }
        return TenantContextHolder.getTenantId();
    }

    /**
     * 应答成功。
     */
    public void ack() {
        try {
            channel.basicAck(deliveryTag, false);
        } catch (IOException e) {
            log.error("[MQ] ACK 失败 queue={}, tag={}", queue.getQueue(), deliveryTag, e);
        }
    }

    /**
     * 拒绝并投递到死信队列。
     * <p>{@code requeue=false}：重新入队会形成毒消息循环。</p>
     */
    public void nack() {
        try {
            channel.basicNack(deliveryTag, false, false);
        } catch (IOException e) {
            log.error("[MQ] NACK 失败 queue={}, tag={}", queue.getQueue(), deliveryTag, e);
        }
    }

    @Override
    public String toString() {
        return "MqDelivery{queue=" + queue.getQueue() + ", messageId=" + getMessageId()
                + ", deliveryTag=" + deliveryTag + "}";
    }
}
