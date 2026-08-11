package com.xd11cc.single.config.flowable;

import org.flowable.engine.IdentityService;
import org.flowable.engine.ProcessEngine;
import org.flowable.engine.ProcessEngineConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.sql.DataSource;

/**
 * Flowable 流程引擎配置。
 * <p>
 * 关键设计：
 * <ol>
 *     <li>自建 {@code processEngine} bean 覆盖 starter 自动配置，确保引擎使用 {@code flowableDataSource}</li>
 *     <li>Flowable 6.7.2 的多租户模型是"部署时显式传 tenantId"（由业务层调用 {@code deployment.tenantId(...)} 决定），没有 ThreadLocal 自动注入机制；租户隔离由业务层显式控制</li>
 *     <li>{@code FlowableSecurityInterceptor} 在此显式注册为 bean，不依赖组件扫描</li>
 * </ol>
 */
@Configuration
public class FlowableConfig implements WebMvcConfigurer {

    @Autowired
    @Qualifier("flowableDataSource")
    private DataSource flowableDataSource;

    @Autowired(required = false)
    private FlowableSecurityInterceptor flowableSecurityInterceptor;

    /**
     * 每次 HTTP 请求进入时，从 Spring Security 上下文取当前登录用户，写入 Flowable 的 IdentityService。
     * <p>
     * 这样后续调用 {@code runtimeService.startProcessInstanceByKey(...)} 时，Flowable 会自动把
     * 当前用户写进 ACT_HI_PROCINST 的 START_USER_ID_ 字段，实现"发起人自动成为流程发起人"。
     */
    @Bean
    public FlowableSecurityInterceptor flowableSecurityInterceptor(IdentityService identityService) {
        return new FlowableSecurityInterceptor(identityService);
    }

    /**
     * 覆盖 Flowable starter 默认的 processEngine bean，让它走 flowableDataSource 而不是主库。
     * <p>
     * 此 bean 与 starter 自动配置的 bean 同名（"processEngine"），Spring 会以此替换后者。
     */
    @Bean("processEngine")
    public ProcessEngine processEngine() {
        ProcessEngineConfiguration configuration = ProcessEngineConfiguration
                .createStandaloneProcessEngineConfiguration();
        configuration.setDataSource(flowableDataSource);
        // 首次启动自动建/升级 ACT_* 表；建完后请手动改为 "false"
        configuration.setDatabaseSchemaUpdate("true");
        // 保留完整历史（流程变量、任务详情等）
        configuration.setHistoryLevel(org.flowable.common.engine.impl.history.HistoryLevel.AUDIT);
        // 关闭异步执行器（单机部署不需要；开启后需要把 TransmittableThreadLocal 手动传递给 Flowable 的 Runnable）
        configuration.setAsyncExecutorActivate(false);
        return configuration.buildProcessEngine();
    }

    /**
     * 将 FlowableSecurityInterceptor 注册到 WebMvc 拦截器链。
     * order 与 HeaderInterceptor（-10）保持一致，确保租户和用户上下文最先设置。
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(flowableSecurityInterceptor(null))
                .addPathPatterns("/**")
                .order(-10);
    }
}
