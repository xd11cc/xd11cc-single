package com.xd11cc.single.config;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @Author: xd11cc
 * @Date: 2025/6/16 22:41
 *
 * swagger自定义参数
 **/
@Data
@NoArgsConstructor
@Component
@ConfigurationProperties("swagger")
public class SwaggerProperties {

    /**
     * 是否开启swagger
     */
    private boolean enabled;

    /**
     * 请求前缀
     */
    private String pathMapping = "";

    /**
     * 标题
     */
    private String title = "";

    /**
     * 描述
     */
    private String description = "";

    /**
     * 版本
     */
    private String version = "";
}
