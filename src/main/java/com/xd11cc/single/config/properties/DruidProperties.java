package com.xd11cc.single.config.properties;

import com.alibaba.druid.pool.DruidDataSource;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @Author: xd11cc
 * @Date: 2025/6/27 14:03
 **/
@Data
@NoArgsConstructor
@ConfigurationProperties("spring.datasource.druid")
@Component
public class DruidProperties {

    private int initialSize;

    private int minIdle;

    private int maxActive;

    private int maxWait;

    private int connectTimeout;

    private int socketTimeout;

    private int timeBetweenEvictionRunsMillis;

    private int minEvictableIdleTimeMillis;

    private int maxEvictableIdleTimeMillis;

    private String validationQuery;

    private boolean testWhileIdle;

    private boolean testOnBorrow;

    private boolean testOnReturn;

    public DruidDataSource dataSource(DruidDataSource druidDataSource) {
        // 配置初始化大小
        druidDataSource.setInitialSize(initialSize);
        druidDataSource.setMinIdle(minIdle);
        druidDataSource.setMaxActive(maxActive);
        // 配置获取连接池超过等待时间
        druidDataSource.setMaxWait(maxWait);
        // 配置驱动连接超时时间，检测数据库建立连接的超时时间，单位/ms
        druidDataSource.setConnectTimeout(connectTimeout);
        // 配置网络超时时间，等待数据库操作完成的网络超时时间，单位/ms
        druidDataSource.setSocketTimeout(socketTimeout);
        // 配置间隔多久检测一次，检测需要关闭空闲连接，单位/ms
        druidDataSource.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis);
        // 配置一个连接池中最小、最大生存时间，单位/ms
        druidDataSource.setMinEvictableIdleTimeMillis(minEvictableIdleTimeMillis);
        druidDataSource.setMaxEvictableIdleTimeMillis(maxEvictableIdleTimeMillis);
        // 检测连接是否是有效sql，如果validationQuery is null，testWhileIdle、testOnBorrow、testOnReturn不会生效
        druidDataSource.setValidationQuery(validationQuery);
        // 申请连接的时候检测，如果空闲时间大于timeBetweenEvictionRunsMillis，执行validationQuery检测连接是否有效（对性能影响不大）
        druidDataSource.setTestWhileIdle(testWhileIdle);
        // 申请连接时，执行validationQuery检测连接是否有效（对性能有影响）
        druidDataSource.setTestOnBorrow(testOnBorrow);
        // 归还连接时，执行validationQuery检测连接是否有效（对性能有影响）
        druidDataSource.setTestOnReturn(testOnReturn);
        return druidDataSource;
    }
}
