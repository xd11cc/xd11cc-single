package com.xd11cc.single.config;

import com.alibaba.ttl.threadpool.TtlExecutors;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.concurrent.DelegatingSecurityContextExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author xd11cc
 * @date 2026-04-07 14:30:50
 * @description 线程池配置，对于异步线程需要认证上下文的通过{@link DelegatingSecurityContextExecutor}进行包装
 */
@Configuration
public class ThreadPoolConfig {

    @Bean(name = "nettyTaskExecutor")
    public Executor nettyTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.setKeepAliveSeconds(300);
        executor.setThreadNamePrefix("netty-server-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(15);
        executor.initialize();
        return TtlExecutors.getTtlExecutor(executor);
    }

    @Bean(name = "operateLogExecutor")
    public Executor operateLogExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(500);
        executor.setKeepAliveSeconds(120);
        executor.setThreadNamePrefix("operate-log-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        return new DelegatingSecurityContextExecutor(TtlExecutors.getTtlExecutor(executor));
    }
}
