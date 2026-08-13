package com.xd11cc.single.utils;

import com.xd11cc.single.config.properties.SecurityProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtUtilsTest {

    private static final String TEST_SECRET = "unit-test-secret-which-is-long-enough-1234567890";
    private JwtUtils jwtUtils;

    @BeforeEach
    void setUp() throws Exception {
        jwtUtils = new JwtUtils();
        // 绕过 @PostConstruct：直接通过反射注入 SecretKey
        SecretKey key = Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8));
        Field keyField = JwtUtils.class.getDeclaredField("key");
        keyField.setAccessible(true);
        keyField.set(jwtUtils, key);
    }

    @Test
    void createToken_正常claims_返回非空token() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", 1L);
        claims.put("username", "admin");

        String token = jwtUtils.createToken(claims);

        assertThat(token).isNotBlank();
        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    void createToken_空claims_返回有效token() {
        String token = jwtUtils.createToken(new HashMap<>());
        assertThat(token).isNotBlank();
    }

    @Test
    void parseToken_合法token_解析出claims() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", 42L);
        claims.put("username", "tester");
        String token = jwtUtils.createToken(claims);

        Claims parsed = jwtUtils.parseToken(token);

        // JJWT 将整数型 claim 解析为 Integer（而非 Long），与写入类型可能不同
        assertThat(parsed.get("userId")).isEqualTo(42);
        assertThat(parsed.get("username")).isEqualTo("tester");
    }

    @Test
    void parseToken_空字符串_抛异常() {
        assertThrows(Exception.class, () -> jwtUtils.parseToken(""));
    }

    @Test
    void parseToken_非法格式_抛异常() {
        assertThrows(Exception.class, () -> jwtUtils.parseToken("not.a.valid.token"));
    }

    @Test
    void parseToken_被篡改的token_抛异常() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", 1L);
        String token = jwtUtils.createToken(claims);

        // 篡改 payload 段
        String tampered = token.substring(0, token.lastIndexOf('.') + 1) + "xxxxx";
        assertThrows(Exception.class, () -> jwtUtils.parseToken(tampered));
    }
}
