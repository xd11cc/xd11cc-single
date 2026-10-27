package com.xd11cc.single.common.mq.listener;

import com.rabbitmq.client.Channel;
import com.xd11cc.single.common.mq.MqDelivery;
import com.xd11cc.single.common.mq.MqDeliveryContext;
import com.xd11cc.single.common.mq.enums.MqQueueEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.listener.adapter.MessagingMessageListenerAdapter;
import org.springframework.amqp.rabbit.listener.api.RabbitListenerErrorHandler;
import org.springframework.amqp.support.AmqpHeaders;

/**
 * MQ 监听适配器。
 * <p>
 * 在调用业务方法前把「队列 + 原始消息 + Channel + deliveryTag」组装成
 * {@link MqDelivery} 并绑定到当前线程，业务方法因此不必再声明这三个参数；
 * 业务方法执行结束后（无论成功或异常）立即清理，避免线程池复用导致串号。
 * </p>
 *
 * <p>业务方法签名可以简化成：</p>
 * <pre>{@code
 * @RabbitHandler
 * public void onDemoMessage(DemoMessage message) {
 *     mqConsumeTemplate.consume(() -> handleBusiness(message));
 * }
 * }</pre>
 *
 * @author xd11cc
 */
@Slf4j
public class MqMessageListenerAdapter extends MessagingMessageListenerAdapter {

    /**
     * 监听队列名（用于还原 {@link MqQueueEnum}）
     */
    private final String queueName;

    public MqMessageListenerAdapter(Object bean, java.lang.reflect.Method method,
                                    boolean returnExceptions, RabbitListenerErrorHandler errorHandler,
                                    String queueName) {
        super(bean, method, returnExceptions, errorHandler);
        this.queueName = queueName;
    }

    @Override
    public void onMessage(Message amqpMessage, Channel channel) throws Exception {
        MqDelivery delivery = MqDelivery.of(
                resolveQueue(amqpMessage), amqpMessage, channel, resolveDeliveryTag(amqpMessage));

        MqDeliveryContext.bind(delivery);
        try {
            super.onMessage(amqpMessage, channel);
        } finally {
            MqDeliveryContext.unbind();
        }
    }

    /**
     * 还原队列枚举：优先注解声明的队列名，其次由 {@link #toMessagingMessage}
     * 回写的 {@code consumerQueue}；都取不到时返回 {@code null}
     * （失败落表仍可继续，只是缺少队列维度）。
     */
    private MqQueueEnum resolveQueue(Message message) {
        String name = queueName;
        if (name == null) {
            Object consumerQueue = message.getMessageProperties().getConsumerQueue();
            name = consumerQueue == null ? null : consumerQueue.toString();
        }
        if (name == null) {
            return null;
        }
        for (MqQueueEnum item : MqQueueEnum.values()) {
            if (item.getQueue().equals(name)) {
                return item;
            }
        }
        log.warn("[MQ] 队列 {} 未在 MqQueueEnum 中登记，投递上下文将缺少队列信息", name);
        return null;
    }

    /**
     * 把 messaging 消息里的 {@code amqp_deliveryTag} 回写到 AMQP 属性。
     * <p>
     * 框架只在转换后的 Spring Message 头里带 tag，AMQP {@code MessageProperties}
     * 上的 {@code deliveryTag} 默认是 0。这里在转换后立即补上，
     * 后续 {@link #resolveDeliveryTag} 才能取到正确的 tag 用于 ACK/NACK。
     * </p>
     */
    @Override
    protected org.springframework.messaging.Message<?> toMessagingMessage(Message amqpMessage) {
        org.springframework.messaging.Message<?> message = super.toMessagingMessage(amqpMessage);
        Object tag = message.getHeaders().get(AmqpHeaders.DELIVERY_TAG);
        if (tag instanceof Number) {
            amqpMessage.getMessageProperties().setDeliveryTag(((Number) tag).longValue());
        }
        Object consumerQueue = message.getHeaders().get(AmqpHeaders.CONSUMER_QUEUE);
        if (consumerQueue != null && amqpMessage.getMessageProperties().getConsumerQueue() == null) {
            amqpMessage.getMessageProperties().setConsumerQueue(consumerQueue.toString());
        }
        return message;
    }

    /**
     * 解析 deliveryTag（由 {@link #toMessagingMessage} 提前回写到 AMQP 属性）。
     */
    private long resolveDeliveryTag(Message message) {
        long tag = message.getMessageProperties().getDeliveryTag();
        return tag > 0 ? tag : -1L;
    }
}
