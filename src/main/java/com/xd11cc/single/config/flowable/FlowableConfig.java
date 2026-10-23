package com.xd11cc.single.config.flowable;

import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.IdentityService;
import org.flowable.engine.ProcessEngine;
import org.flowable.engine.ProcessEngineConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
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
@Slf4j
@Configuration
public class FlowableConfig implements WebMvcConfigurer {

    /**
     * 是否自动建表/升级 ACT_* 表结构。
     * <p>
     * 默认 false：生产环境不应在每次启动时自动变更表结构。
     * 首次初始化时可临时通过 {@code flowable.database-schema-update=true} 打开。
     * </p>
     */
    @Value("${flowable.database-schema-update:false}")
    private String databaseSchemaUpdate;

    /**
     * 历史级别：none / activity / audit / full
     */
    @Value("${flowable.history-level:audit}")
    private String historyLevel;

    /**
     * 是否启用异步执行器（定时任务、异步服务任务等）
     */
    @Value("${flowable.async-executor-activate:false}")
    private boolean asyncExecutorActivate;

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
        // 读取 application.yml 的 flowable.* 配置，避免与配置文件形成“双轨”误导
        configuration.setDatabaseSchemaUpdate(databaseSchemaUpdate);
        configuration.setHistoryLevel(resolveHistoryLevel(historyLevel));
        configuration.setAsyncExecutorActivate(asyncExecutorActivate);
        return configuration.buildProcessEngine();
    }

    /**
     * 将 yml 中的历史级别字符串解析为 Flowable 枚举，非法值回退到 AUDIT。
     */
    private org.flowable.common.engine.impl.history.HistoryLevel resolveHistoryLevel(String level) {
        if (level == null) {
            return org.flowable.common.engine.impl.history.HistoryLevel.AUDIT;
        }
        switch (level.trim().toLowerCase()) {
            case "none":
                return org.flowable.common.engine.impl.history.HistoryLevel.NONE;
            case "activity":
                return org.flowable.common.engine.impl.history.HistoryLevel.ACTIVITY;
            case "full":
                return org.flowable.common.engine.impl.history.HistoryLevel.FULL;
            case "audit":
                return org.flowable.common.engine.impl.history.HistoryLevel.AUDIT;
            default:
                log.warn("[Flowable] 无法识别的 history-level: {}，回退为 audit", level);
                return org.flowable.common.engine.impl.history.HistoryLevel.AUDIT;
        }
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
