package com.xd11cc.single.config;

import com.xd11cc.single.config.exception.ServiceException;
import com.xd11cc.single.config.properties.MinioProperties;
import com.xd11cc.single.enums.SystemErrorEnum;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.errors.*;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

/**
 * @author xd11cc
 * @date 2026-05-07 17:56:57
 * @description
 */
@Slf4j
@Configuration
public class MinioConfig {

    @Autowired
    private MinioProperties minioProperties;

    @Bean
    public MinioClient minioClient() {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(minioProperties.getConnectTimeout(), TimeUnit.MILLISECONDS)
                .writeTimeout(minioProperties.getWriteTimeout(), TimeUnit.MILLISECONDS)
                .readTimeout(minioProperties.getReadTimeout(), TimeUnit.MILLISECONDS)
                .build();
        return MinioClient.builder()
                .endpoint(minioProperties.getEndpoint())
                .credentials(minioProperties.getAccessKey(), minioProperties.getSecretKey())
                .httpClient(okHttpClient)
                .build();
    }

    @PostConstruct
    public void init() {
        String bucketName = minioProperties.getBucketName();
        try {
            boolean b = minioClient().bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!b){
                minioClient().makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                log.info("Minio Bucket Created, name: {}", bucketName);
            }else {
                log.info("Minio Bucket Exists, name: {}", bucketName);
            }
        } catch (Exception e) {
            log.error("Minio Bucket Error, name: {}", bucketName, e);
            throw new ServiceException(SystemErrorEnum.INIT_MINIO_ERROR);
        }
    }
}
