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
 * security白名单路由配置
 **/
@Data
@NoArgsConstructor
@Component
@ConfigurationProperties("security")
public class SecurityProperties {

    private List<String> permitAllUrls = Collections.emptyList();

    /**
     * JWT 签名密钥，必须通过环境变量或启动参数显式注入，禁止硬编码默认值。
     * 建议长度 &gt;= 32 字节（HS256 安全基线）。
     */
    private String tokenSecret;
}
