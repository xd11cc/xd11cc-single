package com.xd11cc.single.common.mq.comsumer;

import com.xd11cc.single.common.context.TenantContextHolder;
import com.xd11cc.single.common.mq.MqConsumeTemplate;
import com.xd11cc.single.common.mq.enums.MqQueueEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 延迟消息消费者演示。
 * <p>
 * 与 {@link DemoConsumer} 结构完全一致，区别只在监听 {@code mq_demo_delay_queue}：
 * 该队列由 {@code x-delayed-message} 交换机投递，消息到期后才进入队列，
 * 因此消费逻辑本身与普通消息无差别。
 * </p>
 *
 * <p><b>延迟是怎么实现的</b>：发送方通过 {@code x-delay} 头指定毫秒数，
 * 插件在交换机侧暂存消息，到期后再路由到队列。这在插件侧完成，
 * 消费者拿到消息时「延迟」已经结束，不需要做任何特殊处理。</p>
 *
 * <p><b>为什么不能靠消费者 sleep 模拟延迟</b>：那会阻塞消费线程，
 * 且应用重启即丢失；延迟交换机把等待放在 broker，消费者无感。</p>
 *
 * <p><b>可删除说明</b>：本类仅用于验证延迟链路，业务接入后可直接删除。</p>
 *
 * @author xd11cc
 */
@Slf4j
@Component
@RabbitListener(queues = DemoDelayConsumer.QUEUE_NAME)
public class DemoDelayConsumer {

    /**
     * 队列名。
     * <p>
     * {@code @RabbitListener} 属性要求编译期常量，无法写成
     * {@code MqQueueEnum.DEMO_DELAY.getQueue()}，因此保留字面量，
     * 并由 {@code MqInfrastructureTest} 校验它与枚举一致，防止漂移。
     * </p>
     */
    public static final String QUEUE_NAME = "mq_demo_delay_queue";

    @Autowired
    private MqConsumeTemplate mqConsumeTemplate;

    /**
     * 延迟消息消费入口。
     * <p>
     * 参数只有业务对象：{@code Message}/{@code Channel}/{@code deliveryTag}
     * 由监听适配器收进 {@code MqDelivery} 线程上下文，模板内部取用。
     * </p>
     */
    @RabbitHandler
    public void onDelayMessage(DemoMessage message) {
        // 切面进入方法前已设置好租户上下文，可直接使用
        log.info("[MQ-Delay] 延迟消息到期，messageId={}, tenantId={}, bizId={}, content={}",
                message.getMessageId(), TenantContextHolder.getTenantId(),
                message.getBizId(), message.getContent());

        mqConsumeTemplate.consume(() -> handleBusiness(message));
    }

    /**
     * 业务处理。
     * <p>这里抛出的任何异常都会被 {@code MqConsumeTemplate} 捕获并触发重试。</p>
     */
    private void handleBusiness(DemoMessage message) {
        // 演示：主动失败，用于观察延迟队列的重试与死信链路
        if (Boolean.TRUE.equals(message.getSimulateFail())) {
            throw new IllegalStateException("【演示】延迟消息处理失败，bizId=" + message.getBizId());
        }
        log.info("[MQ-Delay] 业务处理成功 bizId={}", message.getBizId());
    }
}
