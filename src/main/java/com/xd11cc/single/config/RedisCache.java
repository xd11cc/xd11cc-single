package com.xd11cc.single.config;

import com.xd11cc.single.config.context.TenantContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.*;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @Author: xd11cc
 * @Date: 2025/6/16 13:35
 *
 * redis方法封装
 **/
@SuppressWarnings(value = {"unchecked", "rawtypes"})
@Component
public class RedisCache {

    @Autowired
    private RedisTemplate redisTemplate;

    // ==================== String ====================

    /**
     * @param key key
     * @param value 值
     * @param <T> 值类型
     */
    public <T> void setCacheObject(String key, final T value) {
        redisTemplate.opsForValue().set(resolveKey(key), value);
    }

    /**
     * @param key key
     * @param value 值
     * @param timeout 过期时间
     * @param unit 时间单位
     * @param <T> 值类型
     */
    public <T> void setCacheObject(String key, final T value, final long timeout, final TimeUnit unit) {
        redisTemplate.opsForValue().set(resolveKey(key), value, timeout, unit);
    }

    /**
     * @param key key
     * @param timeout 过期时间
     * @param unit 时间单位
     * @return 是否成功
     */
    public boolean expire(String key, final long timeout, final TimeUnit unit) {
        return redisTemplate.expire(resolveKey(key), timeout, unit);
    }

    /**
     * @param key key
     * @return 剩余过期时间（秒）
     */
    public long getExpire(String key) {
        return redisTemplate.getExpire(resolveKey(key));
    }

    /**
     * @param key key
     * @param unit 时间单位
     * @return 剩余过期时间
     */
    public long getExpire(String key, final TimeUnit unit) {
        return redisTemplate.getExpire(resolveKey(key), unit);
    }

    /**
     * @param key key
     * @return 是否存在
     */
    public boolean hasKey(String key) {
        return redisTemplate.hasKey(resolveKey(key));
    }

    /**
     * @param key key
     * @param <T> 值类型
     * @return 值
     */
    public <T> T getCacheObject(String key) {
        ValueOperations<String, T> valueOperations = redisTemplate.opsForValue();
        return valueOperations.get(resolveKey(key));
    }

    /**
     * @param key key
     */
    public void removeCacheObject(String key) {
        redisTemplate.delete(resolveKey(key));
    }

    // ==================== List ====================

    /**
     * @param key key
     * @param dataList 数据列表
     * @param <T> 值类型
     * @return 列表长度
     */
    public <T> long setCacheList(String key, final List<T> dataList) {
        Long count = redisTemplate.opsForList().rightPushAll(resolveKey(key), dataList);
        return null != count ? count : 0;
    }

    /**
     * @param key key
     * @param <T> 值类型
     * @return 列表数据
     */
    public <T> List<T> getCacheList(String key) {
        return redisTemplate.opsForList().range(resolveKey(key), 0, -1);
    }

    // ==================== Set ====================

    /**
     * @param key key
     * @param dataSet 数据集
     * @param <T> 值类型
     * @return BoundSetOperations
     */
    public <T> BoundSetOperations<String, T> setCacheSet(String key, final Set<T> dataSet) {
        BoundSetOperations<String, T> boundSetOperations = redisTemplate.boundSetOps(resolveKey(key));
        Iterator<T> iterator = dataSet.iterator();
        while (iterator.hasNext()) {
            boundSetOperations.add(iterator.next());
        }
        return boundSetOperations;
    }

    /**
     * @param key key
     * @param <T> 值类型
     * @return 集合数据
     */
    public <T> Set<T> getCacheSet(String key) {
        return redisTemplate.opsForSet().members(resolveKey(key));
    }

    // ==================== Hash ====================

    /**
     * @param key key
     * @param dataMap 数据Map
     * @param <T> 值类型
     */
    public <T> void setCacheMap(String key, final Map<String, T> dataMap) {
        if (null != dataMap && !dataMap.isEmpty()) {
            redisTemplate.opsForHash().putAll(resolveKey(key), dataMap);
        }
    }

    /**
     * @param key key
     * @param <T> 值类型
     * @return Map数据
     */
    public <T> Map<String, T> getCacheMap(String key) {
        return redisTemplate.opsForHash().entries(resolveKey(key));
    }

    /**
     * @param key key
     * @param hKey hashKey
     * @param value 值
     * @param <T> 值类型
     */
    public <T> void setCacheMapValue(String key, final String hKey, final T value) {
        redisTemplate.opsForHash().put(resolveKey(key), hKey, value);
    }

    /**
     * @param key key
     * @param hKey hashKey
     * @param <T> 值类型
     * @return hashKey对应的值
     */
    public <T> T getCacheMapValue(String key, final String hKey) {
        HashOperations<String, String, T> hashOperations = redisTemplate.opsForHash();
        return hashOperations.get(resolveKey(key), hKey);
    }

    /**
     * @param key key
     * @param hKeys hashKey列表
     * @param <T> 值类型
     * @return 值列表
     */
    public <T> List<T> getMultiCacheMapValue(String key, final Collection<String> hKeys) {
        return redisTemplate.opsForHash().multiGet(resolveKey(key), hKeys);
    }

    /**
     * @param key key
     * @param hKey hashKey
     * @return 是否删除成功
     */
    public boolean removeCacheMapValue(String key, final String hKey) {
        return redisTemplate.opsForHash().delete(resolveKey(key), hKey) > 0;
    }

    // ==================== ZSet ====================

    /**
     * @param key key
     * @param value 值
     * @param score 分数
     * @param <T> 值类型
     * @return 是否添加成功
     */
    public <T> Boolean zSetAdd(String key, final T value, double score) {
        return redisTemplate.opsForZSet().add(resolveKey(key), value, score);
    }

    /**
     * @param key key
     * @param value 值
     * @param <T> 值类型
     * @return 删除数量
     */
    public <T> Long zSetRemove(String key, final T value) {
        return redisTemplate.opsForZSet().remove(resolveKey(key), value);
    }

    /**
     * @param key key
     * @param min 最小分数
     * @param max 最大分数
     * @param offset 偏移量
     * @param count 数量
     * @param <V> 值类型
     * @return 分数范围内的值
     */
    public <V> Set<V> zSetRangeByScore(String key, final double min, final double max, final long offset, final long count) {
        return redisTemplate.opsForZSet().rangeByScore(resolveKey(key), min, max, offset, count);
    }

    /**
     * @param key key
     * @param min 最小分数
     * @param max 最大分数
     * @param offset 偏移量
     * @param count 数量
     * @param <V> 值类型
     * @return 分数范围内的值（降序）
     */
    public <V> Set<V> zSetReverseRangeByScore(String key, final double min, final double max, final long offset, final long count) {
        return redisTemplate.opsForZSet().reverseRangeByScore(resolveKey(key), min, max, offset, count);
    }

    // ==================== Utilities ====================

    public RedisTemplate getRedisTemplate() {
        return redisTemplate;
    }

    /**
     * @param prefix key前缀
     * @return 匹配的key集合
     */
    public Set<String> keys(String prefix) {
        String pattern = prefix + "*";
        if (TenantContextHolder.isTenantAware()) {
            Long tenantId = TenantContextHolder.getTenantId();
            if (tenantId != null) {
                pattern = prefix + "*:" + tenantId;
            }
        }
        return redisTemplate.keys(pattern);
    }

    // ==================== Key Resolution ====================

    private String resolveKey(String key) {
        if (TenantContextHolder.isTenantAware()) {
            Long tenantId = TenantContextHolder.getTenantId();
            if (tenantId != null) {
                return key + ":" + tenantId;
            }
        }
        return key;
    }

    /**
     * @param key key
     * @param explicitTenantId 显式指定的租户 ID（非空时优先于当前线程租户）
     * @return 拼接租户 ID 后的 key
     */
    private String resolveKey(String key, Long explicitTenantId) {
        if (explicitTenantId != null) {
            return key + ":" + explicitTenantId;
        }
        return resolveKey(key);
    }
}
