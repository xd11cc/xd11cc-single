package com.xd11cc.single.common.mq;

import com.xd11cc.single.common.context.TenantContextHolder;
import com.xd11cc.single.common.exception.ServiceException;
import com.xd11cc.single.common.mq.enums.MqQueueEnum;
import com.xd11cc.single.enums.MqFailStatus;
import com.xd11cc.single.entity.domain.MqFailMessageDO;
import com.xd11cc.single.mapper.MqFailMessageMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * MQ 消费骨架。
 * <p>
 * 统一封装「租户校验 → 本地重试 → 失败落表 → ACK/NACK」的流程，
 * 业务消费者只需实现业务逻辑。
 * </p>
 *
 * <p>处理流程：</p>
 * <pre>
 * 租户校验失败 → 落表 → NACK → 死信队列（不重试）
 * 处理成功     → ACK
 * 处理失败     → 投递到延迟重试队列（30s / 2min / 5min），立即 ACK 原消息
 *              → 到期后回流原队列重新消费
 *              → 达到上限 → 落表 → NACK → 死信队列
 * </pre>
 *
 * <p>
 * <b>为什么用延迟队列而不是线程 sleep</b>：sleep 会把消费线程占住整个重试周期
 * （三次退避合计约 8 分钟），期间无法处理其他消息。延迟重试把「等待」交给 broker，
 * 消费线程立即释放；应用重启也不影响已排期重试。
 * </p>
 *
 * <p>
 * 关键约束：落表与 NACK 之间不能有会抛异常的代码，
 * 用 try-finally 保证 NACK 一定执行，否则消息会卡在 unacked 状态。
 * </p>
 *
 * @author xd11cc
 */
@Slf4j
@Component
public class MqConsumeTemplate {

    /**
     * 重试退避间隔（毫秒）：30s / 2min / 5min。
     * <p>等待发生在延迟交换机侧，不阻塞消费线程，因此可以拉长。</p>
     */
    private static final long[] RETRY_INTERVALS = {30_000L, 120_000L, 300_000L};

    /**
     * 系统操作人 ID。
     * <p>
     * 消费线程没有登录用户，{@code DefaultDBFieldHandler} 会跳过 createUserId 填充，
     * 而该列 NOT NULL，落表会抛 {@code Column 'create_user_id' cannot be null}。
     * 这里显式记为 0（系统），保证失败消息一定能留痕。
     * </p>
     */
    private static final Long SYSTEM_USER_ID = 0L;

    @Autowired
    private MqFailMessageMapper failMessageMapper;

    @Autowired
    private MqTenantValidator tenantValidator;

    @Autowired
    private MqIdempotentGuard idempotentGuard;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * Jackson 类型标识消息头
     */
    private static final String TYPE_ID_HEADER = "__TypeId__";

    /**
     * 执行消费。
     * <p>
     * 内部依次完成：租户校验 → 设置租户上下文 → 重试执行业务 → ACK 或（落表 + NACK）。
     * 业务代码不要在这里做 try-catch，让异常抛出来触发重试。
     * </p>
     *
     * <p>
     * 投递上下文由监听适配器在调用业务方法前准备好，
     * 业务方法无需再声明 {@code Message}/{@code Channel}/{@code deliveryTag}。
     * </p>
     *
     * @param handler 业务处理逻辑
     */
    public void consume(Runnable handler) {
        MqDelivery delivery = MqDeliveryContext.require();
        Long tenantId;
        try {
            tenantId = tenantValidator.requireValidTenant(delivery.getTenantId());
        } catch (ServiceException e) {
            // 租户非法属于前置校验失败，重试无意义：落表 + NACK 进死信
            log.error("[MQ] 消费前置校验失败 {}", delivery, e);
            failAndNack(delivery, 0, e);
            return;
        }

        // 幂等：成功消费过的消息直接 ACK，避免重投/重放导致业务重复执行
        if (idempotentGuard.isConsumed(delivery.getMessageId(), tenantId)) {
            log.info("[MQ] 消息已消费，跳过 {}", delivery);
            delivery.ack();
            return;
        }

        Long previous = TenantContextHolder.getTenantId();
        TenantContextHolder.setTenantId(tenantId);
        try {
            doConsume(delivery, tenantId, handler);
        } finally {
            // 恢复而非直接 clear，兼容嵌套场景
            if (previous == null) {
                TenantContextHolder.clear();
            } else {
                TenantContextHolder.setTenantId(previous);
            }
        }
    }

    /**
     * 单次消费：成功即 ACK 并记幂等；失败则决定「延迟重试」还是「落表 + 进死信」。
     * <p>
     * 注意这里<b>只执行一次</b>业务逻辑，不再有线程内 sleep 循环。
     * 重试由 broker 侧的延迟队列驱动，每回流一次 retryCount 加一。
     * </p>
     */
    private void doConsume(MqDelivery delivery, Long tenantId, Runnable handler) {
        int retried = resolveRetryCount(delivery);

        try {
            handler.run();
            // 先落幂等标记再 ACK：标记写入失败不影响 ACK，下次重投仍会重新执行
            idempotentGuard.markConsumed(delivery.getMessageId(), tenantId);
            delivery.ack();
            log.info("[MQ] 消费成功 {}, 重试次数={}", delivery, retried);
            return;
        } catch (Throwable e) {
            // 永久性错误不重试，直接失败进死信
            if (isFatal(e)) {
                log.warn("[MQ] 消费失败（不可重试）{}, 第{}次: {}", delivery, retried + 1, e.getMessage());
                failAndNack(delivery, retried + 1, e);
                return;
            }

            // 还有重试额度：投延迟队列，立即 ACK 原消息释放消费线程
            if (retried < RETRY_INTERVALS.length) {
                long delay = RETRY_INTERVALS[retried];
                log.warn("[MQ] 消费失败 {}, 第{}次: {}，{} 毫秒后重试",
                        delivery, retried + 1, e.getMessage(), delay);
                if (scheduleRetry(delivery, tenantId, retried + 1, delay)) {
                    delivery.ack();
                    return;
                }
                // 排期失败：退化为直接落表 + 进死信，避免消息丢失
                log.error("[MQ] 重试排期失败，转为落表 {}", delivery);
            } else {
                log.warn("[MQ] 消费失败 {}, 已耗尽重试次数: {}", delivery, e.getMessage());
            }

            failAndNack(delivery, retried + 1, e);
        }
    }

    /**
     * 读取当前消息已重试过的次数。
     * <p>优先取消息头（延迟回流时写入），其次取消息体字段。</p>
     */
    private int resolveRetryCount(MqDelivery delivery) {
        Object header = delivery.getMessage().getMessageProperties()
                .getHeader(MqMessageSender.HEADER_RETRY_COUNT);
        if (header instanceof Number) {
            return ((Number) header).intValue();
        }
        if (header != null) {
            try {
                return Integer.parseInt(header.toString());
            } catch (NumberFormatException ignored) {
                // 头部格式非法时按首次处理
            }
        }
        return 0;
    }

    /**
     * 把失败消息投递到延迟重试队列。
     * <p>
     * 需要带上四个信息供回流使用：原始队列、原始交换机与路由键、已重试次数。
     * 回流逻辑由 {@link MqRetryConsumer} 负责。
     * </p>
     *
     * @return {@code true} 表示已成功排期
     */
    private boolean scheduleRetry(MqDelivery delivery, Long tenantId, int retryCount, long delayMillis) {
        MqQueueEnum origin = delivery.getQueue();
        if (origin == null) {
            return false;
        }
        try {
            // 必须用 send(Message) 而非 convertAndSend：
            // 后者会把 byte[] 再经 JSON 转换器序列化一次，消息体被二次编码
            // （203 字节变 274 字节），回流后原始消费者反序列化失败。
            MessageProperties props = new MessageProperties();
            props.setHeader(MqMessageSender.HEADER_TENANT_ID, tenantId);
            props.setHeader(MqMessageSender.HEADER_MESSAGE_ID, delivery.getMessageId());
            props.setHeader(MqMessageSender.HEADER_DELAY, delayMillis);
            props.setHeader(MqMessageSender.HEADER_RETRY_COUNT, retryCount);
            props.setHeader(MqMessageSender.HEADER_ORIGINAL_QUEUE, origin.getQueue());
            props.setHeader(MqMessageSender.HEADER_ORIGINAL_EXCHANGE, origin.getExchange());
            props.setHeader(MqMessageSender.HEADER_ORIGINAL_ROUTING_KEY, origin.getRoutingKey());
            props.setMessageId(delivery.getMessageId());
            // 保留原始类型，回流后消费端才能按类型分派
            String typeId = delivery.getMessageType();
            if (typeId != null) {
                props.setHeader(TYPE_ID_HEADER, typeId);
            }
            props.setDeliveryMode(MessageProperties.DEFAULT_DELIVERY_MODE);
            props.setContentType(MessageProperties.CONTENT_TYPE_JSON);

            rabbitTemplate.send(MqQueueEnum.RETRY.getExchange(), MqQueueEnum.RETRY.getRoutingKey(),
                    new Message(delivery.getBody(), props));
            return true;
        } catch (Exception e) {
            log.error("[MQ] 投递延迟重试队列失败 {}", delivery, e);
            return false;
        }
    }

    /**
     * 先落表，再 NACK 进死信队列。
     * <p>落表失败也必须 NACK，否则消息会永久卡在 unacked 状态。</p>
     */
    private void failAndNack(MqDelivery delivery, int retryCount, Throwable error) {
        try {
            saveFailMessage(delivery, retryCount, error);
        } catch (Exception e) {
            log.error("[MQ] 失败消息落表异常 {}", delivery, e);
        } finally {
            delivery.nack();
        }
    }

    /**
     * 判断是否为不可重试的永久性错误。
     * <p>业务校验失败（如流程定义不存在、参数非法）重试无意义，直接进死信。</p>
     */
    private boolean isFatal(Throwable e) {
        if (e instanceof ServiceException) {
            ServiceException se = (ServiceException) e;
            return se.getErrorCode() != null;
        }
        return false;
    }

    private void saveFailMessage(MqDelivery delivery, int retryCount, Throwable error) {
        MqFailMessageDO record = new MqFailMessageDO();
        record.setCreateUserId(SYSTEM_USER_ID);
        record.setUpdateUserId(SYSTEM_USER_ID);
        record.setMessageId(delivery.getMessageId());
        record.setQueueName(delivery.getQueue().getQueue());
        record.setData(new String(delivery.getBody(), StandardCharsets.UTF_8));
        record.setMessageType(delivery.getMessageType());
        record.setErrorMsg(buildErrorMsg(error));
        record.setRetryCount(retryCount);
        // 状态默认「待处理」，与 mq_fail_status 字典的 "0" 对齐
        record.setStatus(MqFailStatus.PENDING.getCode());
        record.setTenantId(delivery.getTenantId());
        failMessageMapper.insert(record);
        log.error("[MQ] 消费最终失败，已落表 messageId={}, queue={}",
                record.getMessageId(), record.getQueueName());
    }

    private String buildErrorMsg(Throwable error) {
        if (error == null) {
            return "未知错误";
        }
        String msg = error.getClass().getSimpleName() + ": " + error.getMessage();
        return msg.length() > 2000 ? msg.substring(0, 2000) : msg;
    }
}
