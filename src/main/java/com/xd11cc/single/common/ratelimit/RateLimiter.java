package com.xd11cc.single.common.ratelimit;

import com.xd11cc.single.constants.CacheConstants;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

/**
 * 限流的机制层：只管「桶」，不管策略。
 * <p>
 * 职责只有三件事：拼 Redis key（含规格指纹）、按需创建桶并设 TTL、取一次配额。
 * 是否放行、超限该抛异常还是静默丢弃、组件故障时降级放行还是拒绝，
 * 全部由调用方（{@code RateLimitAspect} / {@code WsRateLimiter}）决定。
 * </p>
 *
 * @author xd11cc
 */
@Slf4j
@Component
public class RateLimiter {

    private final RedissonClient redissonClient;

    public RateLimiter(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    /**
     * 尝试获取一次配额。
     *
     * @param key  业务标识（不含规格），通常由租户 + 身份组成
     * @param spec 限流规格
     * @return true 放行；false 超限
     * @throws RateLimiterUnavailableException 限流组件本身不可用，策略由调用方决定
     */
    public boolean tryAcquire(String key, RateLimitSpec spec) {
        String bucketKey = buildBucketKey(key, spec);
        try {
            RRateLimiter limiter = redissonClient.getRateLimiter(bucketKey);
            if (limiter.trySetRate(RateType.OVERALL, spec.getPermits(),
                    spec.getWindow().getSeconds(), RateIntervalUnit.SECONDS)) {
                limiter.expire(spec.ttl());
                log.debug("[ratelimit] 初始化桶 key={}", bucketKey);
            }
            return limiter.tryAcquire(1);
        } catch (Exception e) {
            throw new RateLimiterUnavailableException(bucketKey, e);
        }
    }

    /**
     * 桶 key = 前缀 + 业务标识 + 规格指纹。
     * <p>
     * 规格指纹参与 key，是为了让阈值调整立即生效：key 变了就是新桶，
     * 旧桶无人访问、随 TTL 淘汰。
     * </p>
     */
    String buildBucketKey(String key, RateLimitSpec spec) {
        return CacheConstants.RATE_LIMIT_KEY + key + ":" + spec.fingerprint();
    }
}
