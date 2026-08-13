package com.xd11cc.single.config;

import com.xd11cc.single.config.context.TenantContextHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.*;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

class RedisCacheTest {

    private RedisTemplate<String, Object> redisTemplate;
    private RedisCache redisCache;

    @BeforeEach
    void setUp() {
        redisTemplate = mock(RedisTemplate.class);
        redisCache = new RedisCache();
        // 用反射注入 redisTemplate
        try {
            java.lang.reflect.Field field = RedisCache.class.getDeclaredField("redisTemplate");
            field.setAccessible(true);
            field.set(redisCache, redisTemplate);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @AfterEach
    void tearDown() {
        TenantContextHolder.clear();
    }

    // ==================== String ====================

    @Test
    @SuppressWarnings("unchecked")
    void setCacheObject_委托给redisTemplate() {
        org.springframework.data.redis.core.ValueOperations<String, Object> valueOps =
                mock(org.springframework.data.redis.core.ValueOperations.class);
        given(redisTemplate.opsForValue()).willReturn(valueOps);

        redisCache.setCacheObject("key", "value");

        verify(valueOps).set("key", "value");
    }

    @Test
    void getCacheObject_返回缓存值() {
        @SuppressWarnings("unchecked")
        org.springframework.data.redis.core.ValueOperations<String, Object> valueOps =
                mock(org.springframework.data.redis.core.ValueOperations.class);
        given(redisTemplate.opsForValue()).willReturn(valueOps);
        given(valueOps.get("mykey")).willReturn("cached");

        Object result = redisCache.getCacheObject("mykey");

        assertThat(result).isEqualTo("cached");
    }

    @Test
    void getCacheObject_不存在_返回null() {
        @SuppressWarnings("unchecked")
        org.springframework.data.redis.core.ValueOperations<String, Object> valueOps =
                mock(org.springframework.data.redis.core.ValueOperations.class);
        given(redisTemplate.opsForValue()).willReturn(valueOps);
        given(valueOps.get("miss")).willReturn(null);

        Object result = redisCache.getCacheObject("miss");

        assertThat(result).isNull();
    }

    // ==================== Hash ====================

    @Test
    @SuppressWarnings("unchecked")
    void getCacheMapValue_返回Hash中的值() {
        HashOperations<String, Object, Object> hashOps = mock(HashOperations.class);
        given(redisTemplate.opsForHash()).willReturn(hashOps);
        given(hashOps.get("myhash", "field1")).willReturn("value1");

        Object result = redisCache.getCacheMapValue("myhash", "field1");

        assertThat(result).isEqualTo("value1");
    }

    @Test
    @SuppressWarnings("unchecked")
    void getCacheMapValue_不存在_返回null() {
        HashOperations<String, Object, Object> hashOps = mock(HashOperations.class);
        given(redisTemplate.opsForHash()).willReturn(hashOps);
        given(hashOps.get("myhash", "missing")).willReturn(null);

        Object result = redisCache.getCacheMapValue("myhash", "missing");

        assertThat(result).isNull();
    }

    @Test
    @SuppressWarnings("unchecked")
    void setCacheMap_空map_不调用putAll() {
        HashOperations<String, Object, Object> hashOps = mock(HashOperations.class);
        given(redisTemplate.opsForHash()).willReturn(hashOps);

        redisCache.setCacheMap("myhash", null);

        verify(hashOps, org.mockito.Mockito.never()).putAll(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.<String, Object>anyMap());
    }

    @Test
    @SuppressWarnings("unchecked")
    void setCacheMap_有值_调用redisTemplate() {
        HashOperations<String, Object, Object> hashOps = mock(HashOperations.class);
        given(redisTemplate.opsForHash()).willReturn(hashOps);

        Map<String, String> dataMap = new HashMap<>();
        dataMap.put("k1", "v1");
        dataMap.put("k2", "v2");

        redisCache.setCacheMap("myhash", dataMap);

        verify(redisTemplate).opsForHash();
    }

    @Test
    @SuppressWarnings("unchecked")
    void getMultiCacheMapValue_批量获取Hash值() {
        HashOperations<String, Object, Object> hashOps = mock(HashOperations.class);
        given(redisTemplate.opsForHash()).willReturn(hashOps);

        given(hashOps.multiGet("myhash", Arrays.asList("f1", "f2"))).willReturn(Arrays.asList("v1", "v2"));

        java.util.List<Object> result = redisCache.getMultiCacheMapValue("myhash", Arrays.asList("f1", "f2"));

        assertThat(result).containsExactly("v1", "v2");
    }

    // ==================== Key Resolution (with tenant) ====================

    @Test
    void getCacheObject_有租户时_拼接租户ID到key() {
        @SuppressWarnings("unchecked")
        org.springframework.data.redis.core.ValueOperations<String, Object> valueOps =
                mock(org.springframework.data.redis.core.ValueOperations.class);
        given(redisTemplate.opsForValue()).willReturn(valueOps);
        given(valueOps.get("mykey:10")).willReturn("value");

        TenantContextHolder.setTenantId(10L);

        Object result = redisCache.getCacheObject("mykey");

        assertThat(result).isEqualTo("value");
        verify(valueOps).get("mykey:10");
    }

    // ==================== List ====================

    @Test
    @SuppressWarnings("unchecked")
    void setCacheList_委托给redisTemplate() {
        org.springframework.data.redis.core.ListOperations<String, Object> listOps =
                mock(org.springframework.data.redis.core.ListOperations.class);
        given(redisTemplate.opsForList()).willReturn(listOps);

        redisCache.setCacheList("mylist", Arrays.asList("a", "b", "c"));

        verify(listOps).rightPushAll("mylist", Arrays.asList("a", "b", "c"));
    }

    @Test
    void getCacheList_返回列表数据() {
        @SuppressWarnings("unchecked")
        org.springframework.data.redis.core.ListOperations<String, Object> listOps =
                mock(org.springframework.data.redis.core.ListOperations.class);
        given(redisTemplate.opsForList()).willReturn(listOps);
        given(listOps.range("mylist", 0, -1)).willReturn(Arrays.asList("a", "b", "c"));

        java.util.List<Object> result = redisCache.getCacheList("mylist");

        assertThat(result).containsExactly("a", "b", "c");
    }

    // ==================== TTL ====================

    @Test
    void setCacheObject_withTTL_委托timeout给redisTemplate() {
        @SuppressWarnings("unchecked")
        org.springframework.data.redis.core.ValueOperations<String, Object> valueOps =
                mock(org.springframework.data.redis.core.ValueOperations.class);
        given(redisTemplate.opsForValue()).willReturn(valueOps);

        redisCache.setCacheObject("mykey", "value", 60, TimeUnit.SECONDS);

        verify(valueOps).set(eq("mykey"), eq("value"), eq(60L), eq(TimeUnit.SECONDS));
    }

    @Test
    void setCacheObject_withTTL_带租户_拼接租户ID() {
        @SuppressWarnings("unchecked")
        org.springframework.data.redis.core.ValueOperations<String, Object> valueOps =
                mock(org.springframework.data.redis.core.ValueOperations.class);
        given(redisTemplate.opsForValue()).willReturn(valueOps);

        TenantContextHolder.setTenantId(99L);
        redisCache.setCacheObject("mykey", "value", 30, TimeUnit.MINUTES);

        verify(valueOps).set(eq("mykey:99"), eq("value"), eq(30L), eq(TimeUnit.MINUTES));
    }

    @Test
    void expire_委托给redisTemplate() {
        given(redisTemplate.expire("mykey", 120, TimeUnit.SECONDS)).willReturn(true);

        boolean result = redisCache.expire("mykey", 120, TimeUnit.SECONDS);

        assertThat(result).isTrue();
        verify(redisTemplate).expire("mykey", 120, TimeUnit.SECONDS);
    }

    @Test
    void getExpire_委托给redisTemplate() {
        given(redisTemplate.getExpire("mykey")).willReturn(42L);

        long result = redisCache.getExpire("mykey");

        assertThat(result).isEqualTo(42L);
    }

    @Test
    void getExpire_withUnit_委托给redisTemplate() {
        given(redisTemplate.getExpire("mykey", TimeUnit.MINUTES)).willReturn(7L);

        long result = redisCache.getExpire("mykey", TimeUnit.MINUTES);

        assertThat(result).isEqualTo(7L);
    }

    // ==================== Set ====================

    @Test
    @SuppressWarnings("unchecked")
    void setCacheSet_委托给redisTemplate() {
        BoundSetOperations<String, Object> boundSetOps = mock(BoundSetOperations.class);
        given(redisTemplate.boundSetOps("myset")).willReturn(boundSetOps);

        Set<Object> data = new HashSet<>(Arrays.asList("a", "b"));
        redisCache.setCacheSet("myset", data);

        verify(boundSetOps).add("a");
        verify(boundSetOps).add("b");
    }

    @Test
    @SuppressWarnings("unchecked")
    void getCacheSet_返回集合数据() {
        BoundSetOperations<String, Object> boundSetOps = mock(BoundSetOperations.class);
        given(redisTemplate.boundSetOps("myset")).willReturn(boundSetOps);
        given(redisTemplate.opsForSet()).willReturn(mock(SetOperations.class));
        given(redisTemplate.opsForSet().members("myset")).willReturn(new HashSet<>(Arrays.asList("x", "y")));

        Set<Object> result = redisCache.getCacheSet("myset");

        assertThat(result).containsExactlyInAnyOrder("x", "y");
    }

    // ==================== Hash 单项操作 ====================

    @Test
    @SuppressWarnings("unchecked")
    void setCacheMapValue_委托给redisTemplate() {
        HashOperations<String, Object, Object> hashOps = mock(HashOperations.class);
        given(redisTemplate.opsForHash()).willReturn(hashOps);

        redisCache.setCacheMapValue("myhash", "field", "value");

        verify(hashOps).put("myhash", "field", "value");
    }

    @Test
    @SuppressWarnings("unchecked")
    void removeCacheMapValue_删除成功返回true() {
        HashOperations<String, Object, Object> hashOps = mock(HashOperations.class);
        given(redisTemplate.opsForHash()).willReturn(hashOps);
        given(hashOps.delete("myhash", "field")).willReturn(1L);

        boolean result = redisCache.removeCacheMapValue("myhash", "field");

        assertThat(result).isTrue();
    }

    @Test
    @SuppressWarnings("unchecked")
    void removeCacheMapValue_不存在返回false() {
        HashOperations<String, Object, Object> hashOps = mock(HashOperations.class);
        given(redisTemplate.opsForHash()).willReturn(hashOps);
        given(hashOps.delete("myhash", "missing")).willReturn(0L);

        boolean result = redisCache.removeCacheMapValue("myhash", "missing");

        assertThat(result).isFalse();
    }

    @Test
    @SuppressWarnings("unchecked")
    void getCacheMap_返回整个Hash() {
        HashOperations<String, Object, Object> hashOps = mock(HashOperations.class);
        given(redisTemplate.opsForHash()).willReturn(hashOps);
        Map<Object, Object> entries = new HashMap<>();
        entries.put("k1", "v1");
        entries.put("k2", "v2");
        given(hashOps.entries("myhash")).willReturn(entries);

        Map<String, Object> result = redisCache.getCacheMap("myhash");

        assertThat(result).containsEntry("k1", "v1").containsEntry("k2", "v2");
    }

    // ==================== ZSet ====================

    @Test
    @SuppressWarnings("unchecked")
    void zSetAdd_委托给redisTemplate() {
        ZSetOperations<String, Object> zSetOps = mock(ZSetOperations.class);
        given(redisTemplate.opsForZSet()).willReturn(zSetOps);
        given(zSetOps.add("myzset", "value", 1.5)).willReturn(true);

        Boolean result = redisCache.zSetAdd("myzset", "value", 1.5);

        assertThat(result).isTrue();
    }

    @Test
    @SuppressWarnings("unchecked")
    void zSetRemove_委托给redisTemplate() {
        ZSetOperations<String, Object> zSetOps = mock(ZSetOperations.class);
        given(redisTemplate.opsForZSet()).willReturn(zSetOps);
        given(zSetOps.remove("myzset", "value")).willReturn(1L);

        Long result = redisCache.zSetRemove("myzset", "value");

        assertThat(result).isEqualTo(1L);
    }

    @Test
    @SuppressWarnings("unchecked")
    void zSetRangeByScore_返回分数范围结果() {
        ZSetOperations<String, Object> zSetOps = mock(ZSetOperations.class);
        given(redisTemplate.opsForZSet()).willReturn(zSetOps);
        Set<Object> scored = new HashSet<>(Arrays.asList("a", "b"));
        given(zSetOps.rangeByScore("myzset", 0.0, 100.0, 0, 10)).willReturn(scored);

        Set<Object> result = redisCache.zSetRangeByScore("myzset", 0.0, 100.0, 0, 10);

        assertThat(result).containsExactlyInAnyOrder("a", "b");
    }

    @Test
    @SuppressWarnings("unchecked")
    void zSetReverseRangeByScore_降序返回分数范围结果() {
        ZSetOperations<String, Object> zSetOps = mock(ZSetOperations.class);
        given(redisTemplate.opsForZSet()).willReturn(zSetOps);
        Set<Object> scored = new HashSet<>(Arrays.asList("top"));
        given(zSetOps.reverseRangeByScore("myzset", 0.0, 100.0, 0, 10)).willReturn(scored);

        Set<Object> result = redisCache.zSetReverseRangeByScore("myzset", 0.0, 100.0, 0, 10);

        assertThat(result).containsExactly("top");
    }

    // ==================== Key / 异常传播 ====================

    @Test
    void hasKey_存在返回true() {
        given(redisTemplate.hasKey("mykey")).willReturn(true);

        assertThat(redisCache.hasKey("mykey")).isTrue();
    }

    @Test
    void hasKey_不存在返回false() {
        given(redisTemplate.hasKey("mykey")).willReturn(false);

        assertThat(redisCache.hasKey("mykey")).isFalse();
    }

    @Test
    void removeCacheObject_委托给redisTemplate() {
        redisCache.removeCacheObject("mykey");

        verify(redisTemplate).delete("mykey");
    }

    @Test
    void setCacheObject_redis连接失败_向上抛出() {
        @SuppressWarnings("unchecked")
        org.springframework.data.redis.core.ValueOperations<String, Object> valueOps =
                mock(org.springframework.data.redis.core.ValueOperations.class);
        given(redisTemplate.opsForValue()).willReturn(valueOps);
        // willThrow() 在 void / Boolean 返回方法上无法推断 T，改用 doThrow 风格
        org.mockito.Mockito.doThrow(new RuntimeException("redis down"))
                .when(valueOps).set("mykey", "value");

        assertThatThrownBy(() -> redisCache.setCacheObject("mykey", "value"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("redis down");
    }

    @Test
    void getCacheObject_redis连接失败_向上抛出() {
        @SuppressWarnings("unchecked")
        org.springframework.data.redis.core.ValueOperations<String, Object> valueOps =
                mock(org.springframework.data.redis.core.ValueOperations.class);
        given(redisTemplate.opsForValue()).willReturn(valueOps);
        given(valueOps.get(anyString())).willThrow(new RuntimeException("redis down"));

        assertThatThrownBy(() -> redisCache.getCacheObject("mykey"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("redis down");
    }

    @Test
    void setCacheObject_空key_向下透传() {
        @SuppressWarnings("unchecked")
        org.springframework.data.redis.core.ValueOperations<String, Object> valueOps =
                mock(org.springframework.data.redis.core.ValueOperations.class);
        given(redisTemplate.opsForValue()).willReturn(valueOps);

        redisCache.setCacheObject("", "value");

        verify(valueOps).set(eq(""), eq("value"));
    }

    @Test
    void getCacheObject_空key_向下透传() {
        @SuppressWarnings("unchecked")
        org.springframework.data.redis.core.ValueOperations<String, Object> valueOps =
                mock(org.springframework.data.redis.core.ValueOperations.class);
        given(redisTemplate.opsForValue()).willReturn(valueOps);
        given(valueOps.get("")).willReturn("empty");

        Object result = redisCache.getCacheObject("");

        assertThat(result).isEqualTo("empty");
    }

    // ==================== List 边界 ====================

    @Test
    @SuppressWarnings("unchecked")
    void setCacheList_null列表_返回0() {
        org.springframework.data.redis.core.ListOperations<String, Object> listOps =
                mock(org.springframework.data.redis.core.ListOperations.class);
        given(redisTemplate.opsForList()).willReturn(listOps);

        long result = redisCache.setCacheList("mylist", null);

        assertThat(result).isEqualTo(0L);
        verify(listOps, never()).rightPushAll(anyString(), anyList());
    }

    @Test
    void setCacheList_空列表_调用rightPushAll() {
        @SuppressWarnings("unchecked")
        org.springframework.data.redis.core.ListOperations<String, Object> listOps =
                mock(org.springframework.data.redis.core.ListOperations.class);
        given(redisTemplate.opsForList()).willReturn(listOps);
        given(listOps.rightPushAll("mylist", Collections.<Object>emptyList())).willReturn(0L);

        long result = redisCache.setCacheList("mylist", Collections.emptyList());

        assertThat(result).isEqualTo(0L);
        verify(listOps).rightPushAll("mylist", Collections.<Object>emptyList());
    }
}
