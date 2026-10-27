package com.xd11cc.single.common.mq;

import com.xd11cc.single.common.mq.enums.MqQueueEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MQ 队列、交换机、绑定关系自动注册。
 * <p>
 * 遍历 {@link MqQueueEnum}，一次性完成全部声明，新增队列只需在枚举中加一行。
 * </p>
 *
 * <p>每个业务队列都会配套一个死信队列（DLQ）：</p>
 * <ul>
 *     <li>业务队列：{@code workflow_start_queue}</li>
 *     <li>死信交换机：{@code workflow_exchange.dlx}</li>
 *     <li>死信队列：{@code workflow_start_queue.dlq}</li>
 * </ul>
 *
 * @author xd11cc
 */
@Slf4j
@Configuration
public class MqQueueConfig {

    /**
     * 死信交换机后缀
     */
    public static final String DLX_SUFFIX = ".dlx";

    /**
     * 死信队列后缀
     */
    public static final String DLQ_SUFFIX = ".dlq";

    /**
     * 死信路由键后缀
     */
    public static final String DLQ_ROUTING_KEY_SUFFIX = ".dead";

    /**
     * 业务侧全部声明：队列 + 交换机 + 绑定。
     * <p>
     * <b>必须用 {@link Declarables} 包装</b>：{@code RabbitAdmin.initialize()} 只通过
     * {@code getBeansOfType(Queue.class)} 等按类型收集「单个」声明对象，
     * 返回 {@code List<Queue>} / {@code Collection<Queue>} 的 bean 不会被识别
     * （容器中也不存在 {@code Queue} 类型的单例 bean），队列会静默不创建，
     * 消费者启动即报 {@code NOT_FOUND - no queue}。
     * </p>
     */
    @Bean
    public Declarables mqBusinessDeclarables() {
        List<Declarable> items = new ArrayList<>();
        for (MqQueueEnum item : MqQueueEnum.values()) {
            Queue queue = buildBusinessQueue(item);
            Exchange exchange = buildBusinessExchange(item);

            items.add(queue);
            items.add(exchange);
            items.add(BindingBuilder.bind(queue).to(exchange)
                    .with(item.getRoutingKey()).noargs());

            log.info("[MQ] 声明业务队列: {} -> exchange={}, routingKey={}",
                    item.getQueue(), item.getExchange(), item.getRoutingKey());
        }
        return new Declarables(items);
    }

    /**
     * 死信侧全部声明：死信队列 + 死信交换机 + 死信绑定。
     */
    @Bean
    public Declarables mqDeadLetterDeclarables() {
        List<Declarable> items = new ArrayList<>();
        for (MqQueueEnum item : MqQueueEnum.values()) {
            Queue queue = QueueBuilder.durable(item.getQueue() + DLQ_SUFFIX).build();
            Exchange exchange = ExchangeBuilder
                    .directExchange(item.getExchange() + DLX_SUFFIX).durable(true).build();

            items.add(queue);
            items.add(exchange);
            items.add(BindingBuilder.bind(queue).to(exchange)
                    .with(item.getQueue() + DLQ_ROUTING_KEY_SUFFIX).noargs());

            log.info("[MQ] 声明死信队列: {} -> exchange={}", queue.getName(), exchange.getName());
        }
        return new Declarables(items);
    }

    /**
     * 业务队列：带死信参数的持久化队列
     */
    private static Queue buildBusinessQueue(MqQueueEnum item) {
        return QueueBuilder.durable(item.getQueue())
                .withArgument("x-dead-letter-exchange", item.getExchange() + DLX_SUFFIX)
                .withArgument("x-dead-letter-routing-key", item.getQueue() + DLQ_ROUTING_KEY_SUFFIX)
                .build();
    }

    /**
     * 业务交换机：延迟队列用 x-delayed-message，其余用 direct
     */
    private static Exchange buildBusinessExchange(MqQueueEnum item) {
        if (item.isDelayed()) {
            Map<String, Object> args = new HashMap<>(2);
            args.put("x-delayed-type", "direct");
            return new CustomExchange(item.getExchange(), "x-delayed-message", true, false, args);
        }
        return ExchangeBuilder.directExchange(item.getExchange()).durable(true).build();
    }
}
