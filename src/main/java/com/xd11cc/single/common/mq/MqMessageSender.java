package com.xd11cc.single.common.mq;

import com.xd11cc.single.common.context.TenantContextHolder;
import com.xd11cc.single.common.exception.ServiceException;
import com.xd11cc.single.enums.SystemErrorEnum;
import com.xd11cc.single.common.mq.enums.MqQueueEnum;
import com.xd11cc.single.common.mq.message.MqMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * MQ 统一消息发送器。
 * <p>
 * 业务方只传业务对象，消息信封字段（messageId、tenantId）由本类统一填充，
 * 避免手工拼装导致字段缺失或租户写错。
 * </p>
 *
 * <p>
 * <b>所有发送方法默认「事务提交后」才真正发送</b>：
 * 在 {@code @Transactional} 方法内调用时，消息会注册到 {@code afterCommit}，
 * 事务回滚则消息不会发出，避免“库没写成功、消息却发出去了”的不一致；
 * 不在事务中调用则立即发送。
 * </p>
 *
 * @author xd11cc
 */
@Slf4j
@Component
public class MqMessageSender {

    /**
     * 租户 ID 消息头，消费端切面据此设置租户上下文
     */
    public static final String HEADER_TENANT_ID = "tenantId";

    /**
     * 消息 ID 消息头，便于日志串联
     */
    public static final String HEADER_MESSAGE_ID = "messageId";

    /**
     * 延迟时长消息头（毫秒），由 rabbitmq_delayed_message_exchange 插件识别
     */
    public static final String HEADER_DELAY = "x-delay";

    /**
     * 已重试次数消息头。
     * <p>延迟重试时随消息携带，消费端据此判断是否已达到重试上限。</p>
     */
    public static final String HEADER_RETRY_COUNT = "x-retry-count";

    /**
     * 原始队列消息头。
     * <p>重试队列是全局共用的，回流时需要据此确定投回哪个业务队列。</p>
     */
    public static final String HEADER_ORIGINAL_QUEUE = "x-original-queue";

    /**
     * 原始投递尝试次数消息头（用于失败落表时记录真实重试次数）
     */
    public static final String HEADER_ORIGINAL_EXCHANGE = "x-original-exchange";

    /**
     * 原始路由键消息头，回流时使用
     */
    public static final String HEADER_ORIGINAL_ROUTING_KEY = "x-original-routing-key";

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 发送租户消息，tenantId 自动取自当前上下文。
     *
     * @param queue   目标队列
     * @param message 业务消息（必须继承 {@link MqMessage}）
     */
    public <T extends MqMessage> void send(MqQueueEnum queue, T message) {
        Long tenantId = TenantContextHolder.getTenantId();
        if (tenantId == null) {
            throw new ServiceException(SystemErrorEnum.MQ_TENANT_CONTEXT_MISSING);
        }
        send(queue, message, tenantId);
    }

    /**
     * 发送租户消息，显式指定 tenantId。
     * <p>在事务中调用时延迟到事务提交后发送，事务回滚则不发。</p>
     *
     * @param queue    目标队列
     * @param message  业务消息
     * @param tenantId 目标租户ID
     */
    public <T extends MqMessage> void send(MqQueueEnum queue, T message, Long tenantId) {
        afterCommitIfActive(() -> doSend(queue, message, tenantId));
    }

    /**
     * 真正执行发送（不加事务判断）。
     * <p>由 {@link #send} 在事务提交后调用，或在无事务时直接调用。</p>
     */
    private <T extends MqMessage> void doSend(MqQueueEnum queue, T message, Long tenantId) {
        if (message == null) {
            throw new ServiceException(SystemErrorEnum.MQ_MESSAGE_EMPTY);
        }
        if (tenantId == null) {
            throw new ServiceException(SystemErrorEnum.MQ_TENANT_ID_REQUIRED);
        }

        // 统一填充信封字段
        message.setMessageId(message.getMessageId() != null ? message.getMessageId() : UUID.randomUUID().toString());
        message.setTenantId(tenantId);

        publish(queue, message, tenantId, null);

        log.info("[MQ] 发送消息 messageId={}, queue={}, tenantId={}",
                message.getMessageId(), queue.getQueue(), tenantId);
    }

    /**
     * 发送延迟消息，tenantId 自动取自当前上下文。
     *
     * @param queue      目标延迟队列（枚举项必须 delayed=true）
     * @param message    业务消息
     * @param delayMillis 延迟时长（毫秒）
     */
    public <T extends MqMessage> void sendDelay(MqQueueEnum queue, T message, long delayMillis) {
        Long tenantId = TenantContextHolder.getTenantId();
        if (tenantId == null) {
            throw new ServiceException(SystemErrorEnum.MQ_TENANT_CONTEXT_MISSING);
        }
        sendDelay(queue, message, delayMillis, tenantId);
    }

    /**
     * 发送延迟消息，指定时间单位，tenantId 自动取自当前上下文。
     *
     * @param queue   目标延迟队列（枚举项必须 delayed=true）
     * @param message 业务消息
     * @param delay   延迟时长
     * @param unit    时间单位
     */
    public <T extends MqMessage> void sendDelay(MqQueueEnum queue, T message, long delay, TimeUnit unit) {
        Long tenantId = TenantContextHolder.getTenantId();
        if (tenantId == null) {
            throw new ServiceException(SystemErrorEnum.MQ_TENANT_CONTEXT_MISSING);
        }
        sendDelay(queue, message, delay, unit, tenantId);
    }

    /**
     * 发送延迟消息，指定时间单位与租户。
     * <p>
     * {@code x-delay} 头只接受毫秒，因此内部统一换算；不足 1 毫秒时向上取整为 1 毫秒
     * （1 毫秒以下的延迟无业务意义）。
     * </p>
     *
     * @param queue    目标延迟队列（枚举项必须 delayed=true）
     * @param message  业务消息
     * @param delay    延迟时长
     * @param unit     时间单位
     * @param tenantId 目标租户ID
     */
    public <T extends MqMessage> void sendDelay(MqQueueEnum queue, T message, long delay, TimeUnit unit, Long tenantId) {
        if (unit == null) {
            throw new ServiceException(SystemErrorEnum.MQ_DELAY_UNIT_REQUIRED);
        }
        if (delay <= 0) {
            throw new ServiceException(SystemErrorEnum.MQ_DELAY_VALUE_INVALID);
        }
        long millis = unit.toMillis(delay);
        // 不足 1 毫秒向上取整
        if (millis <= 0) {
            millis = 1L;
            log.debug("[MQ] 延迟时长不足 1 毫秒，向上取整为 1 毫秒: {} {}", delay, unit);
        }
        sendDelay(queue, message, millis, tenantId);
    }

    /**
     * 发送延迟消息，显式指定 tenantId。
     * <p>
     * 依赖 rabbitmq_delayed_message_exchange 插件，消息通过 x-delay 头指定延迟时长，
     * 到期后由 Broker 投递到目标队列，不受队头阻塞影响。
     * </p>
     *
     * @param queue       目标延迟队列（枚举项必须 delayed=true）
     * @param message     业务消息
     * @param delayMillis 延迟时长（毫秒），必须大于 0
     * @param tenantId    目标租户ID
     */
    public <T extends MqMessage> void sendDelay(MqQueueEnum queue, T message, long delayMillis, Long tenantId) {
        afterCommitIfActive(() -> doSendDelay(queue, message, delayMillis, tenantId));
    }

    /**
     * 真正执行延迟发送（不加事务判断）。
     */
    private <T extends MqMessage> void doSendDelay(MqQueueEnum queue, T message, long delayMillis, Long tenantId) {
        if (queue == null || !queue.isDelayed()) {
            throw new ServiceException(SystemErrorEnum.MQ_DELAY_QUEUE_REQUIRED);
        }
        if (delayMillis <= 0) {
            throw new ServiceException(SystemErrorEnum.MQ_DELAY_VALUE_INVALID);
        }
        if (message == null) {
            throw new ServiceException(SystemErrorEnum.MQ_MESSAGE_EMPTY);
        }
        if (tenantId == null) {
            throw new ServiceException(SystemErrorEnum.MQ_TENANT_ID_REQUIRED);
        }

        message.setMessageId(message.getMessageId() != null ? message.getMessageId() : UUID.randomUUID().toString());
        message.setTenantId(tenantId);

        publish(queue, message, tenantId, delayMillis);

        log.info("[MQ] 发送延迟消息 messageId={}, queue={}, tenantId={}, delay={}ms",
                message.getMessageId(), queue.getQueue(), tenantId, delayMillis);
    }

    /**
     * 有活动事务则注册到 {@code afterCommit} 执行，否则立即执行。
     * <p>
     * 统一收口「事务后发送」的判断，避免每个方法重复写。
     * </p>
     *
     * @param sender 实际发送动作
     */
    private void afterCommitIfActive(Runnable sender) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            log.debug("[MQ] 检测到活动事务，消息将在提交后发送");
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    sender.run();
                }
            });
            return;
        }
        sender.run();
    }

    /**
     * 统一发布消息，非延迟时 delayMillis 传 {@code null}。
     * <p>
     * 直接发送业务对象：Jackson 转换器会写入 {@code __TypeId__}，消费端可反序列化回具体子类。
     * 若先 {@code JSON.toJSONString} 成 String，{@code __TypeId__} 就变成 {@code java.lang.String}，
     * {@code @RabbitHandler} 按类型分派时会找不到对应方法。
     * </p>
     */
    private <T extends MqMessage> void publish(MqQueueEnum queue, T message, Long tenantId, Long delayMillis) {
        rabbitTemplate.convertAndSend(queue.getExchange(), queue.getRoutingKey(), message, msg -> {
            MessageProperties props = msg.getMessageProperties();
            props.setMessageId(message.getMessageId());
            props.setHeader(HEADER_TENANT_ID, tenantId);
            props.setHeader(HEADER_MESSAGE_ID, message.getMessageId());
            if (delayMillis != null) {
                // 插件约定的延迟头，单位毫秒
                props.setHeader(HEADER_DELAY, delayMillis);
            }
            // 持久化，保证 Broker 重启后消息不丢
            props.setDeliveryMode(MessageProperties.DEFAULT_DELIVERY_MODE);
            // 明确声明 JSON 内容类型，与消费者侧的 Jackson 转换器对应
            props.setContentType(MessageProperties.CONTENT_TYPE_JSON);
            return msg;
        });
    }
}
