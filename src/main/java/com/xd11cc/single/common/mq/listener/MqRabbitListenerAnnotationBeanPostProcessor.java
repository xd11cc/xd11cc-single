package com.xd11cc.single.common.mq.listener;

import org.springframework.amqp.core.Declarable;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.annotation.RabbitListenerAnnotationBeanPostProcessor;
import org.springframework.amqp.rabbit.listener.MethodRabbitListenerEndpoint;

import java.util.Collection;

/**
 * 让 {@code @RabbitListener} / {@code @RabbitHandler} 都使用 {@link MqMessageListenerAdapter}。
 * <p>
 * 框架内部有三条建监听器路径（单方法、{@code @RabbitHandler} 多方法），
 * 其中 {@code processMultiMethodListeners} 是 {@code private}，无法覆写；
 * 但它们最终都会经过 {@link #processListener}，因此在这里统一切换适配器，
 * 覆盖所有情况而无需关心具体走哪条路径。
 * </p>
 *
 * @author xd11cc
 */
public class MqRabbitListenerAnnotationBeanPostProcessor
        extends RabbitListenerAnnotationBeanPostProcessor {

    @Override
    protected Collection<Declarable> processListener(MethodRabbitListenerEndpoint endpoint,
                                                     RabbitListener rabbitListener,
                                                     Object bean,
                                                     Object beanName,
                                                     String methodName) {
        endpoint.setAdapterProvider(new MqAdapterProvider(rabbitListener.queues()));
        return super.processListener(endpoint, rabbitListener, bean, beanName, methodName);
    }

    /**
     * 适配器提供者：非批量场景返回 {@link MqMessageListenerAdapter}。
     */
    private static class MqAdapterProvider implements MethodRabbitListenerEndpoint.AdapterProvider {

        private final String[] queues;

        MqAdapterProvider(String[] queues) {
            this.queues = queues;
        }

        @Override
        public org.springframework.amqp.rabbit.listener.adapter.MessagingMessageListenerAdapter getAdapter(
                boolean batch,
                Object bean,
                java.lang.reflect.Method method,
                boolean returnExceptions,
                org.springframework.amqp.rabbit.listener.api.RabbitListenerErrorHandler errorHandler,
                org.springframework.amqp.rabbit.batch.BatchingStrategy batchingStrategy) {

            if (batch) {
                return new org.springframework.amqp.rabbit.listener.adapter.BatchMessagingMessageListenerAdapter(
                        bean, method, returnExceptions, errorHandler, batchingStrategy);
            }
            String queueName = queues != null && queues.length > 0 ? queues[0] : null;
            return new MqMessageListenerAdapter(bean, method, returnExceptions, errorHandler, queueName);
        }
    }
}
