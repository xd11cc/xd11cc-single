package com.xd11cc.single.common.mq;

import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MQ 消息序列化配置。
 * <p>
 * 注册 {@link Jackson2JsonMessageConverter}：发送端把业务对象写成 JSON
 * （并带上 {@code __TypeId__}），消费端据此反序列化回具体消息子类。
 * </p>
 *
 * <p>
 * 没有它时 Spring Boot 会退回 {@code SimpleMessageConverter}，
 * 消息体以 {@code byte[]} / {@code String} 形式传递，
 * {@code @RabbitHandler} 按参数类型分派时会找不到匹配方法。
 * </p>
 *
 * @author xd11cc
 */
@Configuration
public class MqSerializeConfig {

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
