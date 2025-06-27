package com.xd11cc.single.config;

import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
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
    @Autowired
    private RedissonClient redissonClient;

    /**
     * 设置基本类型
     * @param key
     * @param value
     * @param <T>
     */
    public <T> void setCacheObject(String key, final T value) {
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * 设置基本类型
     * @param key
     * @param value
     * @param timeout
     * @param unit
     * @param <T>
     */
    public <T> void setCacheObject(String key, final T value, final long timeout, final TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    /**
     * 设置过期时间
     * @param key
     * @param timeout
     * @param unit
     * @return
     */
    public boolean expire(String key, final long timeout, final TimeUnit unit) {
        return redisTemplate.expire(key, timeout, unit);
    }

    /**
     * 获取过期时间
     * @param key
     * @param unit
     * @return
     */
    public long getExpire(String key, final TimeUnit unit) {
        return redisTemplate.getExpire(key, unit);
    }

    /**
     * 是否存在
     * @param key
     * @return
     */
    public boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }

    /**
     * 根据key获取设置的对象
     * @param key
     * @return
     * @param <T>
     */
    public <T> T getCacheObject(String key) {
        ValueOperations<String, T> valueOperations = redisTemplate.opsForValue();
        return valueOperations.get(key);
    }

    /**
     * 移除设置的key
     * @param key
     */
    public void removeCacheObject(String key) {
        redisTemplate.delete(key);
    }

    /**
     * 设置集合
     * @param key
     * @param dataList
     * @return
     * @param <T>
     */
    public <T> long setCacheList(String key, final List<T> dataList) {
        Long count = redisTemplate.opsForList().rightPushAll(key, dataList);
        return null != count ? count : 0;
    }

    /**
     * 获取集合
     * @param key
     * @return
     * @param <T>
     */
    public <T> List<T> getCacheList(String key) {
        return redisTemplate.opsForList().range(key, 0, -1);
    }

    /**
     * 设置set集合
     * @param key
     * @param dataSet
     * @return
     * @param <T>
     */
    public <T> BoundSetOperations<String, T> boundSetOps(String key, final Set<T> dataSet) {
        BoundSetOperations<String, T> boundSetOperations = redisTemplate.boundSetOps(key);
        Iterator<T> iterator = dataSet.iterator();
        if (iterator.hasNext()) {
            boundSetOperations.add(iterator.next());
        }
        return boundSetOperations;
    }

    /**
     * 获取set集合
     * @param key
     * @return
     * @param <T>
     */
    public <T> Set<T> getCacheSet(String key) {
        return redisTemplate.opsForSet().members(key);
    }

    /**
     * 设置map集合
     * @param key
     * @param dataMap
     * @param <T>
     */
    public <T> void setCacheMap(String key, final Map<String, T> dataMap) {
        if (null != dataMap && !dataMap.isEmpty()) {
            redisTemplate.opsForHash().putAll(key, dataMap);
        }
    }

    /**
     * 获取map集合
     * @param key
     * @return
     * @param <T>
     */
    public <T> Map<String, T> getCacheMap(String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    /**
     * 设置map集合中key的值
     * @param key
     * @param hKey
     * @param value
     * @param <T>
     */
    public <T> void setCacheMapValue(String key, final String hKey,  final T value) {
        redisTemplate.opsForHash().put(key, hKey, value);
    }

    /**
     * 获取map中key的值
     * @param key
     * @param hKey
     * @return
     * @param <T>
     */
    public <T> T getCacheMapValue(String key, final String hKey) {
        HashOperations<String, String, T> hashOperations = redisTemplate.opsForHash();
        return hashOperations.get(key, hKey);
    }

    /**
     * 获取map中keys的值
     * @param key
     * @param hKeys
     * @return
     * @param <T>
     */
    public <T> List<T> getMultiCacheMapValue(String key, final Collection<String> hKeys) {
        return redisTemplate.opsForHash().multiGet(key, hKeys);
    }

    /**
     * 移除map中key的值
     * @param key
     * @param hKey
     * @return
     */
    public boolean removeCacheMapValue(String key, final String hKey) {
        return redisTemplate.opsForHash().delete(key, hKey) > 0;
    }

    /**
     * 设置zSet集合
     * @param key
     * @param value
     * @param score
     * @return
     * @param <T>
     */
    public <T> Boolean zSetAdd(String key, final T value, double score) {
        return redisTemplate.opsForZSet().add(key, value, score);
    }

    /**
     * 移除zSet集合
     * @param key
     * @param value
     * @return
     * @param <T>
     */
    public <T> Long zSetRemove(String key, final T value) {
        return redisTemplate.opsForZSet().remove(key, value);
    }

    /**
     * 获取score范围之内的zSet数据
     * @param key
     * @param min
     * @param max
     * @param offset
     * @param count
     * @return
     * @param <V>
     */
    public <V> Set<V> zSetRangeByScore(String key, final double min, final double max, final long offset, final long count) {
        return redisTemplate.opsForZSet().rangeByScore(key, min, max, offset, count);
    }

    /**
     * 同上排序方向相反（降序排列）
     * @param key
     * @param min
     * @param max
     * @param offset
     * @param count
     * @return
     * @param <V>
     */
    public <V> Set<V> zSetReverseRangeByScore(String key, final double min, final double max, final long offset, final long count) {
        return redisTemplate.opsForZSet().reverseRangeByScore(key, min, max, offset, count);
    }

    /**
     * 获取redisTemplate对象
     * @return
     */
    public RedisTemplate getRedisTemplate() {
        return redisTemplate;
    }

    public RedissonClient getRedissonClient() {
        return redissonClient;
    }
}
