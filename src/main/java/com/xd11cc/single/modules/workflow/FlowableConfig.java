package com.xd11cc.single.modules.workflow;

import lombok.extern.slf4j.Slf4j;
import org.flowable.common.engine.impl.history.HistoryLevel;
import org.flowable.engine.IdentityService;
import org.flowable.engine.ProcessEngine;
import org.flowable.engine.ProcessEngineConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * Flowable 流程引擎配置。
 * <p>
 * 关键设计：
 * <ol>
 *     <li>自建 {@code processEngine} bean 覆盖 starter 自动配置，确保引擎使用 {@code flowableDataSource}</li>
 *     <li>Flowable 6.7.2 的多租户模型是"部署时显式传 tenantId"（由业务层调用 {@code deployment.tenantId(...)} 决定），
 *         没有 ThreadLocal 自动注入机制；租户隔离由业务层显式控制</li>
 *     <li>Web 层配置（拦截器注册）已拆分至 {@link FlowableWebMvcConfig}，本类只负责引擎构建</li>
 * </ol>
 * </p>
 */
@Slf4j
@Configuration
public class FlowableConfig {

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

    /**
     * 拦截器 bean 的定义（本身由 FlowableWebMvcConfig 负责注册到 MVC 链）。
     * <p>
     * 保留在此是因为它依赖引擎的 {@link IdentityService}，属于引擎装配的一部分。
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
        log.info("[Flowable] 引擎启动参数: schema-update={}, history-level={}, async-executor={}",
                databaseSchemaUpdate, historyLevel, asyncExecutorActivate);
        return configuration.buildProcessEngine();
    }

    /**
     * 将 yml 中的历史级别字符串解析为 Flowable 枚举，非法值回退到 AUDIT。
     */
    private HistoryLevel resolveHistoryLevel(String level) {
        if (level == null) {
            return HistoryLevel.AUDIT;
        }
        switch (level.trim().toLowerCase()) {
            case "none":
                return HistoryLevel.NONE;
            case "activity":
                return HistoryLevel.ACTIVITY;
            case "full":
                return HistoryLevel.FULL;
            case "audit":
                return HistoryLevel.AUDIT;
            default:
                log.warn("[Flowable] 无法识别的 history-level: {}，回退为 audit", level);
                return HistoryLevel.AUDIT;
        }
    }
}
