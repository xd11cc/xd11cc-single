package com.xd11cc.single.common.properties;

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
     * 是否下发 HSTS 头。
     * <p>
     * 只在 HTTPS 部署下有意义；本地 HTTP 调试时浏览器会忽略，但响应头仍是噪音，
     * 所以在 dev / test profile 里关掉。
     * </p>
     */
    private boolean hstsEnabled = true;

    /**
     * JWT 签名密钥，必须通过环境变量或启动参数显式注入，禁止硬编码默认值。
     * 建议长度 &gt;= 32 字节（HS256 安全基线）。
     */
    private String tokenSecret;
}
