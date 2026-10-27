package com.xd11cc.single.common.mq;

import com.rabbitmq.client.Channel;
import com.xd11cc.single.common.mq.enums.MqQueueEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 延迟重试回流消费者。
 * <p>
 * 监听全局重试队列 {@code mq_retry_queue}。消息先由延迟交换机暂存，
 * 到期后才进入本队列，这里再把消息投回<b>原始业务队列</b>，
 * 由原来的消费者重新处理。
 * </p>
 *
 * <p>回流链路：</p>
 * <pre>
 * 业务消费失败 → mq_retry_exchange（暂存 30s/2min/5min）
 *              → 到期进入 mq_retry_queue
 *              → 本类读取 x-original-queue 等头部
 *              → 投回原队列 → 原消费者重试
 * </pre>
 *
 * <p>
 * 本类<b>不承载业务逻辑</b>，也不做重试计数判断（那是
 * {@link MqConsumeTemplate} 的职责）。它只负责搬运，因此不需要走
 * {@code mqConsumeTemplate.consume(...)}，直接 ACK 即可。
 * </p>
 *
 * @author xd11cc
 */
@Slf4j
@Component
public class MqRetryConsumer {

    /**
     * 队列名。
     * <p>{@code @RabbitListener} 属性要求编译期常量，与枚举的一致性由测试保证。</p>
     */
    public static final String QUEUE_NAME = "mq_retry_queue";

    /**
     * Jackson 类型标识消息头
     */
    private static final String TYPE_ID_HEADER = "__TypeId__";

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 回流入口。
     * <p>
     * 用<b>方法级</b> {@code @RabbitListener} 而不是类级 + {@code @RabbitHandler}：
     * 后者会先做类型转换再按参数类型分派，消息体是未知业务类型时会失败
     * （{@code NoSuchMethodException ... for class [B}）。
     * 方法级监听器直接接收原始 {@link Message}，不经过业务类型转换。
     * </p>
     */
    @RabbitListener(queues = MqRetryConsumer.QUEUE_NAME)
    public void onRetryMessage(Message message, Channel channel) {
        MessageProperties props = message.getMessageProperties();
        Object originalQueue = props.getHeader(MqMessageSender.HEADER_ORIGINAL_QUEUE);
        long tag = props.getDeliveryTag();

        try {
            if (originalQueue == null) {
                log.error("[MQ-Retry] 缺少原始队列信息，无法回流，丢弃 messageId={}",
                        props.getMessageId());
                channel.basicAck(tag, false);
                return;
            }

            String queueName = originalQueue.toString();
            MqQueueEnum origin = findQueue(queueName);
            if (origin == null) {
                log.error("[MQ-Retry] 原始队列 {} 未在枚举中登记，无法回流 messageId={}",
                        queueName, props.getMessageId());
                channel.basicAck(tag, false);
                return;
            }

            // 必须用 send(Message) 而非 convertAndSend：
            // 后者会把 byte[] 再交给 JSON 转换器序列化一次，消息体被二次编码，
            // 回流后原始消费者反序列化失败（payload 从 203 字节涨到 370 字节）。
            MessageProperties out = new MessageProperties();
            copyHeader(props, out, MqMessageSender.HEADER_TENANT_ID);
            copyHeader(props, out, MqMessageSender.HEADER_MESSAGE_ID);
            copyHeader(props, out, MqMessageSender.HEADER_RETRY_COUNT);
            copyHeader(props, out, TYPE_ID_HEADER);
            out.setMessageId(props.getMessageId());
            out.setDeliveryMode(MessageProperties.DEFAULT_DELIVERY_MODE);
            out.setContentType(MessageProperties.CONTENT_TYPE_JSON);

            rabbitTemplate.send(origin.getExchange(), origin.getRoutingKey(),
                    new Message(message.getBody(), out));

            channel.basicAck(tag, false);
            log.info("[MQ-Retry] 已回流到 {} messageId={}, 第{}次重试",
                    queueName, props.getMessageId(),
                    props.getHeader(MqMessageSender.HEADER_RETRY_COUNT));
        } catch (Exception e) {
            log.error("[MQ-Retry] 回流失败 messageId={}", props.getMessageId(), e);
            try {
                // requeue=false：转入死信，避免回流失败的消息反复循环
                channel.basicNack(tag, false, false);
            } catch (Exception ex) {
                log.error("[MQ-Retry] NACK 失败 tag={}", tag, ex);
            }
        }
    }

    private void copyHeader(MessageProperties from, MessageProperties to, String key) {
        Object value = from.getHeader(key);
        if (value != null) {
            to.setHeader(key, value);
        }
    }

    private MqQueueEnum findQueue(String name) {
        for (MqQueueEnum item : MqQueueEnum.values()) {
            if (item.getQueue().equals(name)) {
                return item;
            }
        }
        return null;
    }
}
