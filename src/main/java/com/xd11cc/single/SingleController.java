package com.xd11cc.single;

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
@EnableAsync
public class SingleController {

    public static void main(String[] args) {
        SpringApplication.run(SingleController.class, args);
    }
}
