//package com.xd11cc.single.config;
//
//import org.quartz.utils.ConnectionProvider;
//import org.quartz.utils.DBConnectionManager;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.scheduling.quartz.SchedulerFactoryBean;
//import org.springframework.transaction.PlatformTransactionManager;
//
//import javax.sql.DataSource;
//import java.sql.Connection;
//import java.sql.SQLException;
//import java.util.Properties;
//
///**
// * @author xd11cc
// * @date 2025-07-22 15:27:39
// *
// * Quartz配置类，当需要集群或分布式时启用（单体架构通过内存+自定义数据实现）todo
// */
//@Configuration
//public class QuartzConfig {
//
//    @Autowired
//    @Qualifier("masterDataSource")
//    private DataSource dataSource;
//
//    @Autowired
//    private PlatformTransactionManager transactionManager;
//
//    @Bean
//    public SchedulerFactoryBean schedulerFactoryBean() {
//        // 将spring管理的数据源注册到Quartz的DBConnectionManager
//        DBConnectionManager.getInstance().addConnectionProvider("masterDataSource", new SpringManagedConnectionProvider(dataSource));
//        SchedulerFactoryBean schedulerFactoryBean = new SchedulerFactoryBean();
//        Properties properties = new Properties();
//
//        properties.put("org.quartz.scheduler.instanceName", "xd11cc-scheduler");
//        // 自动生成实例id
//        properties.put("org.quartz.scheduler.instanceId", "AUTO");
//        properties.put("org.quartz.threadPool.threadCount", "10");
//        // 线程优先级（1-10，默认5）
//        properties.put("org.quartz.threadPool.threadPriority", "5");
//
//        // 任务存储配置
//        properties.put("org.quartz.jobStore.class", "org.quartz.impl.jdbcjobstore.JobStoreTX");
//        properties.put("org.quartz.jobStore.driverDelegateClass", "org.quartz.impl.jdbcjobstore.StdJDBCDelegate");
//        properties.put("org.quartz.jobStore.tablePrefix", "QRTZ_");
//        // 单体应用关闭集群
//        properties.put("org.quartz.jobStore.isClustered", "false");
//        // 失火阈值（毫秒），超过一分钟判定为失火，配合失火策略
//        properties.put("org.quartz.jobStore.misfireThreshold", "60000");
//        properties.put("org.quartz.jobStore.dataSource", "masterDataSource");
//
//        schedulerFactoryBean.setQuartzProperties(properties);
//        // 使用Spring管理的数据源
//        schedulerFactoryBean.setDataSource(dataSource);
//        // 绑定事务管理器
//        schedulerFactoryBean.setTransactionManager(transactionManager);
//        // 项目启动时自动启动调度器
//        schedulerFactoryBean.setAutoStartup(true);
//        // 覆盖已存在的任务
//        schedulerFactoryBean.setOverwriteExistingJobs(true);
//        // 延迟10秒启动，避免项目未完全启动导致问题
//        schedulerFactoryBean.setStartupDelay(10);
//        return schedulerFactoryBean;
//    }
//
//    /**
//     * 自定义连接提供者，将Spring的datasource适配为quartz可识别的格式
//     */
//    private static class SpringManagedConnectionProvider implements ConnectionProvider {
//        private final DataSource dataSource;
//        public SpringManagedConnectionProvider(DataSource dataSource) {
//            this.dataSource = dataSource;
//        }
//
//        @Override
//        public Connection getConnection() throws SQLException {
//            return dataSource.getConnection();
//        }
//
//        @Override
//        public void shutdown() throws SQLException {
//
//        }
//
//        @Override
//        public void initialize() throws SQLException {
//
//        }
//    }
//}
