package com.xd11cc.single.infrastructure.redis;

import com.xd11cc.single.common.context.TenantContextHolder;
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

    /**
     * SCAN 每次迭代建议返回的 key 数量。
     * 只是「建议值」：Redis 可能一次返回更多或更少，调用方不应依赖其精确性。
     */
    private static final long SCAN_COUNT = 500;

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
     * 原子地取出并删除（GETDEL）。适用于「一次性凭证」的消费场景，
     * 例如验证码校验——读取与删除之间不能有窗口，否则并发请求会
     * 各自读到同一个值，凭证就被用了两次。
     *
     * @param key key
     * @param <T> 值类型
     * @return key 存在时返回值，不存在返回 null
     */
    public <T> T getAndDelete(String key) {
        ValueOperations<String, T> valueOperations = redisTemplate.opsForValue();
        return valueOperations.getAndDelete(resolveKey(key));
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
     * 按前缀扫描 key，底层使用 SCAN 游标分批迭代。
     * <p>
     * 不使用 Redis 的 {@code KEYS} 命令：它是 O(N) 且全程阻塞单线程的 Redis，
     * key 越多阻塞越久，会拖慢所有其他请求。SCAN 每次只返回一批，
     * 虽然总复杂度仍是 O(N)，但不会长时间独占 Redis。
     * </p>
     *
     * @param prefix key前缀
     * @return 匹配的key集合
     */
    public Set<String> scanKeys(String prefix) {
        String pattern = prefix + "*";
        if (TenantContextHolder.isTenantAware()) {
            Long tenantId = TenantContextHolder.getTenantId();
            if (tenantId != null) {
                pattern = prefix + "*:" + tenantId;
            }
        }
        ScanOptions options = ScanOptions.scanOptions().match(pattern).count(SCAN_COUNT).build();
        Set<String> result = new HashSet<>();
        // Cursor 持有连接上的游标状态，必须关闭以释放资源
        try (Cursor<String> cursor = redisTemplate.scan(options)) {
            while (cursor.hasNext()) {
                result.add(cursor.next());
            }
        }
        return result;
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
