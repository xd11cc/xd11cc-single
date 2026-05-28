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

    /**
     * 设置基本类型
     * @param key
     * @param value
     * @param <T>
     */
    public <T> void setCacheObject(String key, final T value) {
        setCacheObject(key, value, true);
    }

    public <T> void setCacheObject(String key, final T value, boolean isTenant) {
        if (isTenant) {
            key = key + ":" + TenantContextHolder.getTenantId();
        }
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
        setCacheObject(key, value, timeout, unit, true);
    }

    public <T> void setCacheObject(String key, final T value, long timeout, TimeUnit unit, boolean isTenant) {
        if (isTenant) {
            key = key + ":" + TenantContextHolder.getTenantId();
        }
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
        return expire(key, timeout, unit, true);
    }

    public boolean expire(String key, final long timeout, final TimeUnit unit, boolean isTenant) {
        if (isTenant) {
            key = key + ":" + TenantContextHolder.getTenantId();
        }
        return redisTemplate.expire(key, timeout, unit);
    }

    /**
     * 获取过期时间
     * @param key
     * @return
     */
    public long getExpire(String key){
        return getExpire(key, true);
    }

    public long getExpire(String key, final TimeUnit unit) {
        return getExpire(key, unit, true);
    }

    public long getExpire(String key, boolean isTenant) {
        if (isTenant) {
            key = key + ":" + TenantContextHolder.getTenantId();
        }
        return redisTemplate.getExpire(key);
    }

    public long getExpire(String key, TimeUnit unit, boolean isTenant) {
        if (isTenant) {
            key = key + ":" + TenantContextHolder.getTenantId();
        }
        return redisTemplate.getExpire(key, unit);
    }

    /**
     * 是否存在
     * @param key
     * @return
     */
    public boolean hasKey(String key) {
        return hasKey(key, true);
    }

    public boolean hasKey(String key, boolean isTenant) {
        if (isTenant) {
            key = key + ":" + TenantContextHolder.getTenantId();
        }
        return redisTemplate.hasKey(key);
    }

    /**
     * 根据key获取设置的对象
     * @param key
     * @return
     * @param <T>
     */
    public <T> T getCacheObject(String key) {
        return getCacheObject(key, true);
    }

    public <T> T getCacheObject(String key, boolean isTenant) {
        if (isTenant) {
            key = key + ":" + TenantContextHolder.getTenantId();
        }
        ValueOperations<String, T> valueOperations = redisTemplate.opsForValue();
        return valueOperations.get(key);
    }

    /**
     * 移除设置的key
     * @param key
     */
    public void removeCacheObject(String key) {
        removeCacheObject(key, true);
    }

    public void removeCacheObject(String key, boolean isTenant) {
        if (isTenant) {
            key = key + ":" + TenantContextHolder.getTenantId();
        }
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
        return setCacheList(key, dataList, true);
    }

    public <T> long setCacheList(String key, final List<T> dataList, boolean isTenant) {
        if (isTenant) {
            key = key + ":" + TenantContextHolder.getTenantId();
        }
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
        return getCacheList(key, true);
    }

    public <T> List<T> getCacheList(String key, boolean isTenant) {
        if (isTenant) {
            key = key + ":" + TenantContextHolder.getTenantId();
        }
        return redisTemplate.opsForList().range(key, 0, -1);
    }

    /**
     * 设置set集合
     * @param key
     * @param dataSet
     * @return
     * @param <T>
     */
    public <T> BoundSetOperations<String, T> setCacheSet(String key, final Set<T> dataSet) {
        return setCacheSet(key, dataSet, true);
    }

    public <T> BoundSetOperations<String, T> setCacheSet(String key, final Set<T> dataSet, boolean isTenant) {
        if (isTenant) {
            key = key + ":" + TenantContextHolder.getTenantId();
        }
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
        return getCacheSet(key, true);
    }

    public <T> Set<T> getCacheSet(String key, boolean isTenant) {
        if (isTenant) {
            key = key + ":" + TenantContextHolder.getTenantId();
        }
        return redisTemplate.opsForSet().members(key);
    }

    /**
     * 设置map集合
     * @param key
     * @param dataMap
     * @param <T>
     */
    public <T> void setCacheMap(String key, final Map<String, T> dataMap) {
        setCacheMap(key, dataMap, true);
    }

    public <T> void setCacheMap(String key, final Map<String, T> dataMap, boolean isTenant) {
        if (isTenant) {
            key = key + ":" + TenantContextHolder.getTenantId();
        }
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
        return getCacheMap(key, true);
    }

    public <T> Map<String, T> getCacheMap(String key, boolean isTenant) {
        if (isTenant) {
            key = key + ":" + TenantContextHolder.getTenantId();
        }
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

    public <T> void setCacheMapValue(String key, final String hKey,  final T value, boolean isTenant){
        if (isTenant){
            key = key + ":" + TenantContextHolder.getTenantId();
        }
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
        return getCacheMapValue(key, hKey, true);
    }

    public <T> T getCacheMapValue(String key, final String hKey, boolean isTenant) {
        if (isTenant){
            key = key + ":" + TenantContextHolder.getTenantId();
        }
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
        return getMultiCacheMapValue(key, hKeys, true);
    }

    public <T> List<T> getMultiCacheMapValue(String key, Collection<String> hKeys, boolean isTenant) {
        if (isTenant) {
            key = key + ":" + TenantContextHolder.getTenantId();
        }
        return redisTemplate.opsForHash().multiGet(key, hKeys);
    }

    /**
     * 移除map中key的值
     * @param key
     * @param hKey
     * @return
     */
    public boolean removeCacheMapValue(String key, final String hKey) {
        return removeCacheMapValue(key, hKey, true);
    }

    public boolean removeCacheMapValue(String key, final String hKey, boolean isTenant) {
        if (isTenant) {
            key = key + ":" + TenantContextHolder.getTenantId();
        }
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
        return zSetAdd(key, value, score, true);
    }

    public <T> Boolean zSetAdd(String key, final T value, double score, boolean isTenant) {
        if (isTenant) {
            key = key + ":" + TenantContextHolder.getTenantId();
        }
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
        return zSetRemove(key, value, true);
    }

    public <T> Long zSetRemove(String key, final T value, boolean isTenant) {
        if (isTenant) {
            key = key + ":" + TenantContextHolder.getTenantId();
        }
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
        return zSetRangeByScore(key, min, max, offset, count, true);
    }

    public <V> Set<V> zSetRangeByScore(String key, final double min, final double max, final long offset, final long count, boolean isTenant) {
        if (isTenant) {
            key = key + ":" + TenantContextHolder.getTenantId();
        }
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
        return zSetReverseRangeByScore(key, min, max, offset, count, true);
    }

    public <V> Set<V> zSetReverseRangeByScore(String key, final double min, final double max, final long offset, final long count, boolean isTenant) {
        if (isTenant) {
            key = key + ":" + TenantContextHolder.getTenantId();
        }
        return redisTemplate.opsForZSet().reverseRangeByScore(key, min, max, offset, count);
    }

    /**
     * 获取redisTemplate对象
     * @return
     */
    public RedisTemplate getRedisTemplate() {
        return redisTemplate;
    }

    /**
     * 获取匹配的key集合
     * @param prefix key前缀
     * @return
     */
    public Set<String> keys(String prefix) {
        return keys(prefix, true);
    }

    public Set<String> keys(String prefix, boolean isTenant) {
        String pattern;
        if (isTenant) {
            pattern = prefix + "*:" + TenantContextHolder.getTenantId();
        } else {
            pattern = prefix + "*";
        }
        return redisTemplate.keys(pattern);
    }

}
