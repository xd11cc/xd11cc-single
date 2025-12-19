package com.xd11cc.single.mq.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;


/**
 * @author xd11cc
 * @date 2025-11-28 09:41:59
 */
@Configuration
public class QueueConfig {

    private static final String DEMO_QUEUE = "demo_queue";

    @Bean
    public Queue demoQueue() {
        return QueueBuilder.durable(DEMO_QUEUE).build();
    }
}
