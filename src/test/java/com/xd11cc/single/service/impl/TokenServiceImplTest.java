package com.xd11cc.single.service.impl;

import com.xd11cc.single.config.RedisCache;
import com.xd11cc.single.constants.SecurityConstants;
import com.xd11cc.single.entity.domain.SystemUserDO;
import com.xd11cc.single.entity.dto.LoginUserDTO;
import com.xd11cc.single.enums.SystemErrorEnum;
import com.xd11cc.single.utils.JwtUtils;
import com.xd11cc.single.util.BaseUnitTest;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.mock;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class TokenServiceImplTest extends BaseUnitTest {

    @Mock
    private RedisCache redisCache;
    @Mock
    private JwtUtils jwtUtils;

    @InjectMocks
    private TokenServiceImpl tokenService;

    @Test
    void getLoginUser_无Authorization头_返回null() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        given(request.getHeader(anyString())).willReturn(null);

        assertThat(tokenService.getLoginUser(request)).isNull();
    }

    @Test
    void getLoginUser_Authorization存在_解析token并返回用户() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        given(request.getHeader("Authorization")).willReturn("Bearer token123");
        Claims claims = mock(Claims.class);
        given(jwtUtils.parseToken("token123")).willReturn(claims);
        given(claims.get("login_user_key")).willReturn("uuid-abc");
        LoginUserDTO cached = buildLoginUserDTO(1L, "admin");
        given(redisCache.getCacheObject("login_tokens:uuid-abc")).willReturn(cached);

        LoginUserDTO result = tokenService.getLoginUser(request);

        assertThat(result).isSameAs(cached);
    }

    @Test
    void getLoginUser_解析token失败_抛未授权() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        given(request.getHeader("Authorization")).willReturn("Bearer badtoken");
        given(jwtUtils.parseToken("badtoken")).willThrow(new RuntimeException("bad"));

        assertThrows(com.xd11cc.single.config.exception.ServiceException.class,
                () -> tokenService.getLoginUser(request));
    }

    @Test
    void getLoginUser_String参数_空token_返回null() {
        assertThat(tokenService.getLoginUser("")).isNull();
    }

    @Test
    void getLoginUser_String参数_解析成功_返回缓存用户() {
        given(jwtUtils.parseToken("valid-token")).willReturn(mockClaimsWithTenant("uuid-1", 1L));
        LoginUserDTO cached = buildLoginUserDTO(1L, "admin");
        given(redisCache.getCacheObject("login_tokens:uuid-1")).willReturn(cached);

        LoginUserDTO result = tokenService.getLoginUser("Bearer valid-token");

        assertThat(result).isSameAs(cached);
    }

    @Test
    void getLoginUser_String参数_租户id非数值_抛未授权() {
        Claims claims = mock(Claims.class);
        given(claims.get("tenant_id")).willReturn("not-a-number");
        given(jwtUtils.parseToken("tok")).willReturn(claims);

        assertThrows(com.xd11cc.single.config.exception.ServiceException.class,
                () -> tokenService.getLoginUser("Bearer tok"));
    }

    @Test
    void getLoginUser_String参数_解析异常_抛未授权() {
        given(jwtUtils.parseToken(anyString())).willThrow(new RuntimeException("expired"));

        assertThrows(com.xd11cc.single.config.exception.ServiceException.class,
                () -> tokenService.getLoginUser("Bearer tok"));
    }

    @Test
    void verifyToken_过期时间距现在大于阈值_无需刷新() {
        LoginUserDTO dto = buildLoginUserDTO(1L, "admin");
        dto.setExpireTime(System.currentTimeMillis() + 2 * 60 * 60 * 1000L); // 2h later

        tokenService.verifyToken(dto);

        // 不应操作 redis（无续期逻辑）
        then(redisCache).should(org.mockito.BDDMockito.never()).setCacheObject(anyString(), any(), anyLong(), any());
    }

    @Test
    void verifyToken_即将过期_续期token() {
        LoginUserDTO dto = buildLoginUserDTO(1L, "admin");
        long expireTime = System.currentTimeMillis() + SecurityConstants.EXPIRE_REFRESH_TOKEN_TIME - 1000;
        dto.setExpireTime(expireTime);

        tokenService.verifyToken(dto);

        // 续期后 expireTime 应更新
        assertThat(dto.getExpireTime()).isGreaterThan(expireTime);
    }

    @Test
    void removeLoginUser_有token_清除缓存() {
        LoginUserDTO dto = buildLoginUserDTO(1L, "admin");
        dto.setToken("tok-1");

        tokenService.removeLoginUser(dto);

        then(redisCache).should().removeCacheObject("login_tokens:tok-1");
        then(redisCache).should().removeCacheObject("login_users:1");
    }

    @Test
    void removeLoginUser_token为空_不操作() {
        LoginUserDTO dto = buildLoginUserDTO(1L, "admin");
        dto.setToken("");

        tokenService.removeLoginUser(dto);

        then(redisCache).should(org.mockito.BDDMockito.never()).removeCacheObject(anyString());
    }

    @Test
    void createToken_生成有效JWT() {
        SystemUserDO userDO = new SystemUserDO();
        userDO.setId(1L);
        userDO.setUsername("admin");
        userDO.setTenantId(2L);
        LoginUserDTO dto = new LoginUserDTO();
        dto.setUserId(1L);
        dto.setSystemUserDO(userDO);
        given(jwtUtils.createToken(any(Map.class))).willReturn("jwt-token-string");

        String token = tokenService.createToken(dto);

        assertThat(token).isEqualTo("jwt-token-string");
        assertThat(dto.getToken()).isNotBlank();
        assertThat(dto.getLoginTime()).isGreaterThan(0);
        assertThat(dto.getExpireTime()).isGreaterThan(0);
        then(redisCache).should().setCacheObject(anyString(), eq(dto), anyLong(), any());
        then(redisCache).should().setCacheObject(anyString(), anyString(), anyLong(), any());
    }

    // ==================== 辅助方法 ====================

    private static Claims mockClaimsWithTenant(String uuidToken, long tenantId) {
        Map<String, Object> map = new HashMap<>();
        map.put("login_user_key", uuidToken);
        map.put("tenant_id", tenantId);
        return new TestClaims(map);
    }

    /** Minimal Claims stub backed by a Map. */
    private static class TestClaims implements Claims {
        private final Map<String, Object> data;
        TestClaims(Map<String, Object> data) { this.data = data; }

        // Map delegation
        @Override public int size() { return data.size(); }
        @Override public boolean isEmpty() { return data.isEmpty(); }
        @Override public boolean containsKey(Object key) { return data.containsKey(key); }
        @Override public boolean containsValue(Object value) { return data.containsValue(value); }
        @Override public Object get(Object key) { return data.get(key); }
        @Override public Object put(String key, Object value) { return data.put(key, value); }
        @Override public Object remove(Object key) { return data.remove(key); }
        @Override public void putAll(Map<? extends String, ?> m) { data.putAll(m); }
        @Override public void clear() { data.clear(); }
        @Override public Set<String> keySet() { return data.keySet(); }
        @Override public Collection<Object> values() { return data.values(); }
        @Override public Set<Entry<String, Object>> entrySet() { return data.entrySet(); }
        @Override public Object getOrDefault(Object key, Object defaultValue) { return data.getOrDefault(key, defaultValue); }
        @Override public <T> T get(String claimName, Class<T> requiredType) { return null; }
        @Override public boolean equals(Object o) { return data.equals(o); }
        @Override public int hashCode() { return data.hashCode(); }

        // Claims
        @Override public String getIssuer() { return null; }
        @Override public Claims setIssuer(String iss) { return this; }
        @Override public String getSubject() { return null; }
        @Override public Claims setSubject(String sub) { return this; }
        @Override public String getAudience() { return null; }
        @Override public Claims setAudience(String aud) { return this; }
        @Override public Date getExpiration() { return null; }
        @Override public Claims setExpiration(Date exp) { return this; }
        @Override public Date getNotBefore() { return null; }
        @Override public Claims setNotBefore(Date nbf) { return this; }
        @Override public Date getIssuedAt() { return null; }
        @Override public Claims setIssuedAt(Date iat) { return this; }
        @Override public String getId() { return null; }
        @Override public Claims setId(String jti) { return this; }
    }

    private static LoginUserDTO buildLoginUserDTO(Long userId, String username) {
        SystemUserDO userDO = new SystemUserDO();
        userDO.setId(userId);
        userDO.setUsername(username);
        userDO.setTenantId(1L);
        LoginUserDTO dto = new LoginUserDTO();
        dto.setUserId(userId);
        dto.setSystemUserDO(userDO);
        dto.setToken("token-" + userId);
        dto.setExpireTime(System.currentTimeMillis() + SecurityConstants.EXPIRE_TIME);
        return dto;
    }
}
