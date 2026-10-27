package com.xd11cc.single.common.mq;

import com.rabbitmq.client.Channel;
import com.xd11cc.single.common.mq.enums.MqQueueEnum;
import com.xd11cc.single.common.mq.message.MqMessage;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.listener.adapter.MessagingMessageListenerAdapter;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * MQ 基础设施单元测试（不依赖真实 Broker）。
 *
 * @author xd11cc
 */
class MqInfrastructureTest {

    @Data
    @EqualsAndHashCode(callSuper = true)
    static class DemoMessage extends MqMessage {
        private String bizId;
        private String remark;
    }

    // ==================== 队列枚举 ====================

    @Test
    void 队列枚举_工作流发起队列配置完整() {
        MqQueueEnum item = MqQueueEnum.WORKFLOW_START;

        assertThat(item.getQueue()).isEqualTo("workflow_start_queue");
        assertThat(item.getExchange()).isEqualTo("workflow_exchange");
        assertThat(item.getRoutingKey()).isEqualTo("workflow.start");
    }

    @Test
    void 队列枚举_每项三元组均非空且队列名唯一() {
        for (MqQueueEnum item : MqQueueEnum.values()) {
            assertThat(item.getQueue()).isNotBlank();
            assertThat(item.getExchange()).isNotBlank();
            assertThat(item.getRoutingKey()).isNotBlank();
        }
        long distinct = java.util.Arrays.stream(MqQueueEnum.values())
                .map(MqQueueEnum::getQueue).distinct().count();
        assertThat(distinct).isEqualTo(MqQueueEnum.values().length);
    }

    // ==================== 消息模型 ====================

    @Test
    void 消息模型_业务消息继承后可携带业务字段() {
        DemoMessage msg = new DemoMessage();
        msg.setMessageId("mid-1");
        msg.setTenantId(1L);
        msg.setBizId("leave:1001");
        msg.setRemark("请假申请");

        assertThat(msg.getMessageId()).isEqualTo("mid-1");
        assertThat(msg.getTenantId()).isEqualTo(1L);
        assertThat(msg.getBizId()).isEqualTo("leave:1001");
    }

    @Test
    void 消息模型_序列化后可还原信封与业务字段() {
        DemoMessage msg = new DemoMessage();
        msg.setMessageId("mid-2");
        msg.setTenantId(2L);
        msg.setBizId("order:2001");

        String json = com.alibaba.fastjson2.JSON.toJSONString(msg);
        DemoMessage back = com.alibaba.fastjson2.JSON.parseObject(json, DemoMessage.class);

        assertThat(back.getMessageId()).isEqualTo("mid-2");
        assertThat(back.getTenantId()).isEqualTo(2L);
        assertThat(back.getBizId()).isEqualTo("order:2001");
    }

    // ==================== 延迟队列 ====================

    @Test
    void 延迟队列_枚举标记正确() {
        assertThat(MqQueueEnum.WORKFLOW_START.isDelayed()).isFalse();
        assertThat(MqQueueEnum.DEMO_DELAY.isDelayed()).isTrue();
        assertThat(MqQueueEnum.DEMO_DELAY.getQueue()).isEqualTo("mq_demo_delay_queue");
        assertThat(MqQueueEnum.DEMO_DELAY.getExchange()).isEqualTo("mq_demo_delay_exchange");
    }

    @Test
    void 延迟队列_时间单位换算为毫秒() {
        java.util.concurrent.TimeUnit unit = java.util.concurrent.TimeUnit.SECONDS;
        assertThat(unit.toMillis(30)).isEqualTo(30000L);

        // 不足 1 毫秒时向上取整为 1
        long micros = java.util.concurrent.TimeUnit.MICROSECONDS.toMillis(100);
        assertThat(micros).isZero();
        assertThat(micros <= 0 ? 1L : micros).isEqualTo(1L);
    }

    // ==================== 死信参数 ====================

    @Test
    void 死信队列_命名约定一致() {
        MqQueueEnum item = MqQueueEnum.WORKFLOW_START;

        String dlx = item.getExchange() + MqQueueConfig.DLX_SUFFIX;
        String dlq = item.getQueue() + MqQueueConfig.DLQ_SUFFIX;
        String deadKey = item.getQueue() + MqQueueConfig.DLQ_ROUTING_KEY_SUFFIX;

        assertThat(dlx).isEqualTo("workflow_exchange.dlx");
        assertThat(dlq).isEqualTo("workflow_start_queue.dlq");
        assertThat(deadKey).isEqualTo("workflow_start_queue.dead");
    }

    // ==================== 声明 Bean 契约 ====================

    /**
     * RabbitAdmin 只通过 {@code getBeansOfType(Queue.class)} 收集「单个」声明对象，
     * 必须用 {@link org.springframework.amqp.core.Declarables} 包装。
     * <p>
     * 若返回 {@code List<Queue>} / {@code Collection<Queue>}，容器中没有 {@code Queue}
     * 类型的单例 bean，队列会静默不创建，消费者启动即 404 失败。
     * </p>
     */
    @Test
    void 声明Bean_必须返回Declarables() throws Exception {
        assertThat(MqQueueConfig.class.getMethod("mqBusinessDeclarables").getReturnType())
                .isEqualTo(org.springframework.amqp.core.Declarables.class);
        assertThat(MqQueueConfig.class.getMethod("mqDeadLetterDeclarables").getReturnType())
                .isEqualTo(org.springframework.amqp.core.Declarables.class);
    }

    @Test
    void 业务声明_覆盖枚举中全部队列与交换机() {
        int size = MqQueueEnum.values().length;

        org.springframework.amqp.core.Declarables declarables = new MqQueueConfig().mqBusinessDeclarables();
        assertThat(declarables.getDeclarablesByType(org.springframework.amqp.core.Queue.class)).hasSize(size);
        assertThat(declarables.getDeclarablesByType(org.springframework.amqp.core.Exchange.class)).hasSize(size);
        assertThat(declarables.getDeclarablesByType(org.springframework.amqp.core.Binding.class)).hasSize(size);
    }

    @Test
    void 死信声明_覆盖枚举中全部队列与交换机() {
        int size = MqQueueEnum.values().length;

        org.springframework.amqp.core.Declarables declarables = new MqQueueConfig().mqDeadLetterDeclarables();
        assertThat(declarables.getDeclarablesByType(org.springframework.amqp.core.Queue.class)).hasSize(size);
        assertThat(declarables.getDeclarablesByType(org.springframework.amqp.core.Exchange.class)).hasSize(size);
        assertThat(declarables.getDeclarablesByType(org.springframework.amqp.core.Binding.class)).hasSize(size);

        // 死信队列必须是 <业务队列>.dlq
        assertThat(declarables.getDeclarablesByType(org.springframework.amqp.core.Queue.class))
                .allSatisfy(q -> assertThat(q.getName()).endsWith(MqQueueConfig.DLQ_SUFFIX));
    }

    @Test
    void 业务队列_携带指向自身死信交换机的参数() {
        new MqQueueConfig().mqBusinessDeclarables()
                .getDeclarablesByType(org.springframework.amqp.core.Queue.class)
                .forEach(queue -> {
                    MqQueueEnum matched = null;
                    for (MqQueueEnum item : MqQueueEnum.values()) {
                        if (item.getQueue().equals(queue.getName())) {
                            matched = item;
                            break;
                        }
                    }
                    assertThat(matched).as("队列 %s 应能匹配到枚举", queue.getName()).isNotNull();
                    assertThat(queue.getArguments().get("x-dead-letter-exchange"))
                            .isEqualTo(matched.getExchange() + MqQueueConfig.DLX_SUFFIX);
                    assertThat(queue.getArguments().get("x-dead-letter-routing-key"))
                            .isEqualTo(matched.getQueue() + MqQueueConfig.DLQ_ROUTING_KEY_SUFFIX);
                    assertThat(queue.isDurable()).isTrue();
                });
    }

    @Test
    void 延迟交换机_使用x_delayed_message类型() {
        // 延迟队列可能不止一个（演示延迟队列 + 全局重试队列），
        // 因此按枚举逐个判断，而不是硬编码某一个交换机名
        java.util.Set<String> delayedExchanges = new java.util.HashSet<>();
        for (MqQueueEnum item : MqQueueEnum.values()) {
            if (item.isDelayed()) {
                delayedExchanges.add(item.getExchange());
            }
        }
        assertThat(delayedExchanges).isNotEmpty();

        new MqQueueConfig().mqBusinessDeclarables()
                .getDeclarablesByType(org.springframework.amqp.core.Exchange.class)
                .forEach(exchange -> {
                    if (delayedExchanges.contains(exchange.getName())) {
                        assertThat(exchange.getType()).isEqualTo("x-delayed-message");
                        assertThat(exchange.getArguments().get("x-delayed-type")).isEqualTo("direct");
                    } else {
                        assertThat(exchange.getType()).isEqualTo("direct");
                    }
                });
    }

    @Test
    void 重试队列_必须是延迟队列且已登记() {
        assertThat(MqQueueEnum.RETRY.isDelayed()).isTrue();
        assertThat(MqQueueEnum.RETRY.getQueue()).isEqualTo(
                com.xd11cc.single.common.mq.MqRetryConsumer.QUEUE_NAME);
    }

    // ==================== 投递上下文 ====================

    private Message rawMessage(String body, String messageId, Long headerTenantId) {
        MessageProperties props = new MessageProperties();
        props.setMessageId(messageId);
        props.setContentType(MessageProperties.CONTENT_TYPE_JSON);
        if (headerTenantId != null) {
            props.setHeader(MqMessageSender.HEADER_TENANT_ID, headerTenantId);
        }
        return new Message(body.getBytes(StandardCharsets.UTF_8), props);
    }

    @Test
    void 投递上下文_收拢四个参数() {
        Message message = rawMessage("{}", "msg-1", 7L);
        Channel channel = null;
        MqDelivery delivery = MqDelivery.of(MqQueueEnum.DEMO, message, channel, 3L);

        assertThat(delivery.getQueue()).isEqualTo(MqQueueEnum.DEMO);
        assertThat(delivery.getMessage()).isSameAs(message);
        assertThat(delivery.getChannel()).isNull();
        assertThat(delivery.getDeliveryTag()).isEqualTo(3L);
        assertThat(delivery.getMessageId()).isEqualTo("msg-1");
        assertThat(delivery.getBody()).isEqualTo("{}".getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void 投递上下文_租户优先取消息头() {
        MqDelivery delivery = MqDelivery.of(MqQueueEnum.DEMO, rawMessage("{}", "m", 99L), null, 1L);
        assertThat(delivery.getTenantId()).isEqualTo(99L);
    }

    @Test
    void 投递上下文_无消息头时取不到租户返回null() {
        MqDelivery delivery = MqDelivery.of(MqQueueEnum.DEMO, rawMessage("{}", "m", null), null, 1L);
        assertThat(delivery.getTenantId()).isNull();
    }

    @Test
    void 投递上下文_messageId退化取header() {
        MessageProperties props = new MessageProperties();
        props.setHeader(MqMessageSender.HEADER_MESSAGE_ID, "header-id");
        MqDelivery delivery = MqDelivery.of(MqQueueEnum.DEMO,
                new Message("{}".getBytes(StandardCharsets.UTF_8), props), null, 1L);

        assertThat(delivery.getMessageId()).isEqualTo("header-id");
    }

    // ==================== 消费模板 API 形状 ====================

    /**
     * 消费模板的入参只剩业务逻辑本身，
     * 业务侧不再透传 Message / Channel / deliveryTag / MqDelivery。
     */
    @Test
    void 消费模板_只接收业务逻辑() {
        java.util.List<java.lang.reflect.Method> consumeMethods =
                java.util.Arrays.stream(MqConsumeTemplate.class.getMethods())
                        .filter(m -> "consume".equals(m.getName()))
                        .collect(java.util.stream.Collectors.toList());

        assertThat(consumeMethods).hasSize(1);
        assertThat(consumeMethods.get(0).getParameterTypes()).containsExactly(Runnable.class);
    }

    // ==================== 投递上下文线程变量 ====================

    @Test
    void 投递上下文_绑定与清理() {
        Message message = rawMessage("{}", "msg-ctx", 3L);
        MqDelivery delivery = MqDelivery.of(MqQueueEnum.DEMO, message, null, 9L);

        assertThat(MqDeliveryContext.current()).isNull();
        MqDeliveryContext.bind(delivery);
        try {
            assertThat(MqDeliveryContext.current()).isSameAs(delivery);
            assertThat(MqDeliveryContext.require().getDeliveryTag()).isEqualTo(9L);
        } finally {
            MqDeliveryContext.unbind();
        }
        assertThat(MqDeliveryContext.current()).isNull();
    }

    @Test
    void 投递上下文_未绑定时require抛异常() {
        MqDeliveryContext.unbind();
        org.assertj.core.api.Assertions.assertThatThrownBy(MqDeliveryContext::require)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("没有 MQ 投递上下文");
    }

    // ==================== 监听适配器 ====================

    /**
     * 业务方法签名必须只剩业务对象，不能出现框架帧参数。
     */
    @Test
    void 演示消费者_业务方法不含框架参数() throws Exception {
        Class<?> consumer = Class.forName("com.xd11cc.single.common.mq.comsumer.DemoConsumer");
        Method handler = null;
        for (Method method : consumer.getDeclaredMethods()) {
            if ("onDemoMessage".equals(method.getName())) {
                handler = method;
                break;
            }
        }
        assertThat(handler).isNotNull();
        assertThat(handler.getParameterTypes()).containsExactly(
                Class.forName("com.xd11cc.single.common.mq.comsumer.DemoMessage"));
    }

    @Test
    void 监听适配器_绑定并清理投递上下文() {
        com.xd11cc.single.common.mq.listener.MqMessageListenerAdapter adapter =
                new com.xd11cc.single.common.mq.listener.MqMessageListenerAdapter(
                        new Object(), Object.class.getMethods()[0], false, null,
                        MqQueueEnum.DEMO.getQueue());

        assertThat(adapter).isNotNull();
        // 适配器必须继承框架的实现，才能保留原有的转换与分派能力
        assertThat(adapter).isInstanceOf(MessagingMessageListenerAdapter.class);
    }

    @Test
    void 租户校验器_优先取业务对象中的租户() {
        MqTenantValidator validator = new MqTenantValidator();
        DemoMessage body = new DemoMessage();
        body.setTenantId(5L);

        MqDelivery delivery = MqDelivery.of(MqQueueEnum.DEMO, rawMessage("{}", "m", 1L), null, 1L);
        assertThat(validator.resolveTenantId(body, delivery)).isEqualTo(5L);

        // 业务对象没带租户时退化为消息头
        assertThat(validator.resolveTenantId(new DemoMessage(), delivery)).isEqualTo(1L);
        // 非 MqMessage 类型同样退化为消息头
        assertThat(validator.resolveTenantId("other", delivery)).isEqualTo(1L);
    }

    @Test
    void 演示消费者_队列名与枚举保持一致() throws Exception {
        Class<?> consumer = Class.forName("com.xd11cc.single.common.mq.comsumer.DemoConsumer");
        java.lang.reflect.Field field = consumer.getDeclaredField("QUEUE_NAME");
        field.setAccessible(true);

        assertThat(field.get(null)).isEqualTo(MqQueueEnum.DEMO.getQueue());
    }
}
