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
 * MQ 消费者标准结构演示。
 * <p>
 * 这是后续所有消费者的模板，包含以下要点：
 * </p>
 *
 * <ol>
 *     <li><b>类上 {@code @RabbitListener}</b>：声明监听哪个队列</li>
 *     <li><b>方法上 {@code @RabbitHandler}</b>：
 *         <ul>
 *             <li>切面 {@code MqTenantAspect} 只切这个注解，进入方法前已设置好租户上下文</li>
 *             <li>参数只需业务对象（{@code DemoMessage}）。
 *                 {@code Message}/{@code Channel}/{@code deliveryTag} 属于框架帧信息，
 *                 由监听适配器收进 {@code MqDelivery} 线程上下文，业务方法不必感知</li>
 *         </ul>
 *     </li>
 *     <li><b>业务逻辑包在 {@code mqConsumeTemplate.consume(...)} 内</b>：
 *         统一处理「租户校验 → 重试 3 次 → 失败落表 → NACK 进死信」</li>
 *     <li><b>业务方法内不要再 catch 异常</b>：让异常抛给模板，由模板决定重试</li>
 * </ol>
 *
 * <p><b>可删除说明</b>：本类仅用于验证 MQ 基础设施，业务接入后可直接删除。</p>
 *
 * @author xd11cc
 */
@Slf4j
@Component
@RabbitListener(queues = DemoConsumer.QUEUE_NAME)
public class DemoConsumer {

    /**
     * 队列名。
     * <p>
     * {@code @RabbitListener} 的属性要求编译期常量，无法写成 {@code MqQueueEnum.DEMO.getQueue()}，
     * 因此这里保留字面量，并由 {@code MqInfrastructureTest} 校验它与枚举一致，防止漂移。
     * </p>
     */
    static final String QUEUE_NAME = "mq_demo_queue";

    @Autowired
    private MqConsumeTemplate mqConsumeTemplate;

    /**
     * 演示消费入口。
     * <p>
     * 参数只有业务对象：{@code Message}/{@code Channel}/{@code deliveryTag}
     * 由监听适配器收进 {@code MqDelivery} 线程上下文，模板内部取用。
     * </p>
     */
    @RabbitHandler
    public void onDemoMessage(DemoMessage message) {
        // 切面进入方法前已设置好租户上下文，可直接使用
        log.info("[MQ-Demo] 收到消息 messageId={}, tenantId={}, bizId={}, content={}",
                message.getMessageId(), TenantContextHolder.getTenantId(),
                message.getBizId(), message.getContent());

        mqConsumeTemplate.consume(() -> handleBusiness(message));
    }

    /**
     * 真正的业务处理。
     * <p>这里抛出的任何异常都会被 {@code MqConsumeTemplate} 捕获并触发重试。</p>
     */
    private void handleBusiness(DemoMessage message) {
        // 演示：模拟耗时
        if (message.getCostMillis() != null && message.getCostMillis() > 0) {
            try {
                Thread.sleep(message.getCostMillis());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("处理被中断", e);
            }
        }

        // 演示：主动失败，用于观察重试与失败落表
        if (Boolean.TRUE.equals(message.getSimulateFail())) {
            throw new IllegalStateException("【演示】业务处理失败，bizId=" + message.getBizId());
        }

        // 演示：业务成功
        log.info("[MQ-Demo] 业务处理成功 bizId={}, tenantId={}",
                message.getBizId(), message.getTenantId());
    }
}
