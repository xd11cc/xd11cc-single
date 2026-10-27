package com.xd11cc.single.common.mq;

import lombok.extern.slf4j.Slf4j;

/**
 * 当前正在消费的投递上下文（线程级）。
 * <p>
 * 消费端框架会注入 {@code Message}、{@code Channel}、{@code deliveryTag} 三个参数，
 * 业务方法签名里不需要、也不该出现它们。因此在监听器适配器调用业务方法前，
 * 把这三个参数打包成 {@link MqDelivery} 挂到这里，业务方法通过
 * {@code mqConsumeTemplate.consume(...)} 间接取用。
 * </p>
 *
 * <p><b>生命周期</b>：</p>
 * <pre>
 * 适配器 onMessage  → bind(delivery)
 *                  → 调用业务方法 → consume(...) 内部取 current()
 *                  → finally unbind()   // 必须清理，否则线程池复用会串号
 * </pre>
 *
 * <p>
 * 这是 MQ 框架内部的隐式通道，<b>不要</b>在业务代码里直接调用 {@link #current()}，
 * 统一走 {@link MqConsumeTemplate}。
 * </p>
 *
 * @author xd11cc
 */
@Slf4j
public final class MqDeliveryContext {

    private static final ThreadLocal<MqDelivery> CURRENT = new ThreadLocal<>();

    private MqDeliveryContext() {
    }

    /**
     * 绑定当前线程的投递上下文。
     */
    public static void bind(MqDelivery delivery) {
        CURRENT.set(delivery);
    }

    /**
     * 获取当前线程的投递上下文，未绑定时返回 {@code null}。
     */
    public static MqDelivery current() {
        return CURRENT.get();
    }

    /**
     * 获取当前线程的投递上下文，未绑定则抛异常。
     */
    public static MqDelivery require() {
        MqDelivery delivery = CURRENT.get();
        if (delivery == null) {
            throw new IllegalStateException(
                    "当前线程没有 MQ 投递上下文：请确认业务方法由 MQ 监听器调用，"
                            + "且通过 mqConsumeTemplate.consume(...) 执行");
        }
        return delivery;
    }

    /**
     * 清理当前线程的投递上下文。
     */
    public static void unbind() {
        CURRENT.remove();
    }
}
