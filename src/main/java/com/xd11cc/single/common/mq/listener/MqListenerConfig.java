package com.xd11cc.single.common.mq.listener;

import org.springframework.amqp.rabbit.config.RabbitListenerConfigUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 把框架的 {@code @RabbitListener} 处理器替换为自定义实现。
 * <p>
 * <b>为什么不直接定义一个同名 {@code @Bean} 覆盖</b>：
 * Spring AMQP 的 {@code RabbitBootstrapConfiguration} 以「该名字不存在」为前提，
 * 同时注册 annotation processor 与 endpoint registry。若我们提前占了处理器这个名字，
 * 框架连 registry 都不再注册，启动直接失败：
 * </p>
 * <pre>
 * A component required a bean named
 * 'org.springframework.amqp.rabbit.config.internalRabbitListenerEndpointRegistry' could not be found.
 * </pre>
 *
 * <p>
 * 因此这里保留框架注册的 bean 定义，只替换它的<b>类</b>：
 * 两个 bean 仍成对存在，仅实现换成 {@link MqRabbitListenerAnnotationBeanPostProcessor}。
 * </p>
 *
 * @author xd11cc
 */
@Configuration
public class MqListenerConfig {

    /**
     * 声明为 {@code static}：{@code BeanFactoryPostProcessor} 会在配置类实例化之前执行，
     * 非静态方法会触发「配置类提前实例化」的告警与不确定行为。
     */
    @Bean
    public static BeanFactoryPostProcessor rabbitListenerProcessorReplacer() {
        return beanFactory -> {
            String beanName = RabbitListenerConfigUtils.RABBIT_LISTENER_ANNOTATION_PROCESSOR_BEAN_NAME;
            if (!beanFactory.containsBeanDefinition(beanName)) {
                return;
            }
            BeanDefinition definition = beanFactory.getBeanDefinition(beanName);
            definition.setBeanClassName(MqRabbitListenerAnnotationBeanPostProcessor.class.getName());
        };
    }
}
