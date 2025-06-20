package com.xd11cc.single;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @Author: xd11cc
 * @Date: 2025/6/13 14:24
 **/
@SpringBootApplication
@MapperScan("com.xd11cc.single.mapper")
public class SingleController {

    public static void main(String[] args) {
        SpringApplication.run(SingleController.class, args);
    }
}
