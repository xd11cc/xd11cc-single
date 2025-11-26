package com.xd11cc.single.config;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author xd11cc
 * @date 2025-11-26 16:33:54
 */
@Data
@NoArgsConstructor
@Configuration
@ConfigurationProperties("xxl.job")
public class XxlJobProperties {

    private Executor executor;

    private Admin admin;

    private String accessToken;

    @Data
    public static class Executor {
        private String appName;

        private String logPath;

        private int logRetentionDays;
    }

    @Data
    public static class Admin {
        private String addresses;
    }
}
