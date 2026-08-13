package com.xd11cc.single.config.auth;

import com.xd11cc.single.config.RedisCache;
import com.xd11cc.single.constants.CacheConstants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RedisAuthStateCacheTest {

    @Mock
    private RedisCache redisCache;

    @InjectMocks
    private RedisAuthStateCache authStateCache;

    // ==================== cache(key, value) 默认超时 ====================

    @Test
    void cache_默认超时_调用setCacheObject带默认超时() {
        authStateCache.cache("myKey", "myValue");

        verify(redisCache).setCacheObject(
                org.mockito.ArgumentMatchers.eq(CacheConstants.AUTH_STATE_KEY + "myKey"),
                org.mockito.ArgumentMatchers.eq("myValue"),
                org.mockito.ArgumentMatchers.anyLong(),
                org.mockito.ArgumentMatchers.eq(java.util.concurrent.TimeUnit.MILLISECONDS));
    }

    // ==================== cache(key, value, timeout) 自定义超时 ====================

    @Test
    void cache_自定义超时_调用setCacheObject带指定超时() {
        authStateCache.cache("stateKey", "stateValue", 600000L);

        verify(redisCache).setCacheObject(
                CacheConstants.AUTH_STATE_KEY + "stateKey",
                "stateValue",
                600000L,
                java.util.concurrent.TimeUnit.MILLISECONDS);
    }

    // ==================== get ====================

    @Test
    void get_返回缓存值() {
        given(redisCache.getCacheObject(CacheConstants.AUTH_STATE_KEY + "k"))
                .willReturn("cachedValue");

        String value = authStateCache.get("k");

        assertThat(value).isEqualTo("cachedValue");
        verify(redisCache).getCacheObject(CacheConstants.AUTH_STATE_KEY + "k");
    }

    @Test
    void get_不存在_返回null() {
        given(redisCache.getCacheObject(CacheConstants.AUTH_STATE_KEY + "missing"))
                .willReturn(null);

        String value = authStateCache.get("missing");

        assertThat(value).isNull();
    }

    // ==================== containsKey ====================

    @Test
    void containsKey_存在_返回true() {
        given(redisCache.hasKey(CacheConstants.AUTH_STATE_KEY + "k")).willReturn(true);

        boolean exists = authStateCache.containsKey("k");

        assertThat(exists).isTrue();
        verify(redisCache).hasKey(CacheConstants.AUTH_STATE_KEY + "k");
    }

    @Test
    void containsKey_不存在_返回false() {
        given(redisCache.hasKey(CacheConstants.AUTH_STATE_KEY + "k")).willReturn(false);

        boolean exists = authStateCache.containsKey("k");

        assertThat(exists).isFalse();
    }

    // ==================== key前缀 ====================

    @Test
    void cache_拼接AUTH_STATE_KEY前缀() {
        authStateCache.cache("rawKey", "rawValue");
        // 验证 RedisCache.setCacheObject 的调用参数包含正确前缀
        verify(redisCache).setCacheObject(
                org.mockito.ArgumentMatchers.eq(CacheConstants.AUTH_STATE_KEY + "rawKey"),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyLong(),
                org.mockito.ArgumentMatchers.any());
    }
}
