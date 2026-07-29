package com.xd11cc.single.utils;

import com.xd11cc.single.config.properties.SecurityProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * JWT 工具类（Spring Bean）。
 * <p>
 * 签名密钥从 {@link SecurityProperties#tokenSecret} 注入，禁止硬编码。
 * 启动时校验密钥非空且长度 &gt;= 32 字节，校验失败则应用启动失败。
 *
 * @Author: xd11cc
 * @Date: 2025/6/16 15:48
 **/
@Slf4j
@Component
public class JwtUtils {

    private SecretKey key;

    @Autowired
    private SecurityProperties securityProperties;

    @PostConstruct
    public void init() {
        String secret = securityProperties.getTokenSecret();
        if (StringUtils.isBlank(secret)) {
            throw new IllegalStateException(
                    "JWT 签名密钥未配置。请通过环境变量 SECURITY_TOKEN_SECRET 或启动参数 "
                    + "--security.token-secret=xxx 显式注入，禁止使用默认值。");
        }
        if (secret.length() < 32) {
            throw new IllegalStateException(
                    "JWT 签名密钥长度不足（当前 " + secret.length() + " 字节，HS256 安全基线要求 >= 32 字节）。"
                    + "请使用更强的密钥后重启。");
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        log.info("JWT 签名密钥初始化完成，长度: {} 字节", secret.length());
    }

    public String createToken(Map<String, Object> claims) {
        return Jwts.builder()
                .setClaims(claims)
                .signWith(key)
                .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
