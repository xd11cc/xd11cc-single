package com.xd11cc.single.common.ratelimit;

import lombok.Value;

import java.time.Duration;

/**
 * 限流规格：一个窗口内允许多少次。
 * <p>
 * 取代原来注解上的 {@code time} + {@code timeUnit} 两个参数，
 * 让"窗口"成为一个不可分割的概念。
 * </p>
 *
 * @author xd11cc
 */
@Value
public class RateLimitSpec {

    /**
     * 窗口内允许的次数
     */
    int permits;

    /**
     * 窗口长度
     */
    Duration window;

    /**
     * 桶的存活时间：窗口的两倍。
     * <p>
     * 规格变更后旧桶不再被命中，靠 TTL 自然淘汰，无需手工清 Redis。
     * </p>
     */
    public Duration ttl() {
        return window.multipliedBy(2);
    }

    /**
     * 规格指纹，编进 Redis key。
     * <p>
     * 这是「改阈值立即生效」的实现方式：规格一变，key 就变，新桶从零开始计数；
     * 旧的桶没有人再访问，随 TTL 消失。
     * 若不这样做，Redisson 的 {@code trySetRate} 对已存在的 key 返回 false，
     * 改配置后要等桶过期才生效，且没有任何提示。
     * </p>
     */
    public String fingerprint() {
        return permits + "/" + window.getSeconds() + "s";
    }
}
