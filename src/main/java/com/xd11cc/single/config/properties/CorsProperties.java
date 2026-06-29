package com.xd11cc.single.config.properties;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * @Author: xd11cc
 * @Date: 2025/6/21 21:39
 *
 * CORS跨域域名白名单配置
 **/
@Data
@NoArgsConstructor
@Component
@ConfigurationProperties("cors")
public class CorsProperties {

    private List<String> allowedOrigins = Collections.singletonList("*");
}
