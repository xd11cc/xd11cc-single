package com.xd11cc.single.config.properties;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author xd11cc
 * @date 2026-05-07 17:55:39
 * @description
 */
@Data
@NoArgsConstructor
@ConfigurationProperties("minio")
@Component
public class MinioProperties {

    private String endpoint;

    private String accessKey;

    private String secretKey;

    private String bucketName;

    private int connectTimeout;

    private int writeTimeout;

    private int readTimeout;
}
