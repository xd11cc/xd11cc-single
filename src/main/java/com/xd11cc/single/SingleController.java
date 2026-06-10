package com.xd11cc.single;

import com.xd11cc.single.config.annotation.PayClientScan;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * @Author: xd11cc
 * @Date: 2025/6/13 14:24
 **/
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@MapperScan("com.xd11cc.single.mapper")
@PayClientScan(basePackages = "com.xd11cc.single.config.pay.impl")
@EnableAsync
public class SingleController {

    public static void main(String[] args) {
        SpringApplication.run(SingleController.class, args);
    }
}
