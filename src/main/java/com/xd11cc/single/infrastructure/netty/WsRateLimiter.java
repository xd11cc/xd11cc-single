package com.xd11cc.single.infrastructure.netty;

import com.xd11cc.single.common.properties.NettyProperties;
import com.xd11cc.single.common.ratelimit.RateLimitSpec;
import com.xd11cc.single.common.ratelimit.RateLimiter;
import com.xd11cc.single.common.ratelimit.RateLimiterUnavailableException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket 消息限流（按发送者）。
 * <p>
 * 维度是 {@code tenantId + userId}，同一用户的多个连接共享一份额度，
 * 避免「多开连接 = 多份额度」这种最廉价的绕过方式。
 * </p>
 * <p>
 * <b>失败策略与 HTTP 限流相反</b>：这里的限流是体验保护而非安全边界，
 * Redis 不可用时<b>放行</b>（fail-open）——聊天不该因为限流组件抖动而整体中断。
 * 登录限流那类安全边界才应该 fail-closed。
 * </p>
 *
 * @author xd11cc
 */
@Slf4j
@Component
public class WsRateLimiter {

    /**
     * 同一 key 的超限日志间隔，避免日志被打爆
     */
    private static final long WARN_INTERVAL_MS = 10_000L;

    /**
     * 日志限频记录上限，兜底防止无限增长
     */
    private static final int WARN_CACHE_LIMIT = 10_000;

    private final RateLimiter rateLimiter;

    private final NettyProperties nettyProperties;

    /**
     * key -> 上次打印超限日志的时间戳
     */
    private final ConcurrentHashMap<String, Long> lastWarnAt = new ConcurrentHashMap<>();

    public WsRateLimiter(RateLimiter rateLimiter, NettyProperties nettyProperties) {
        this.rateLimiter = rateLimiter;
        this.nettyProperties = nettyProperties;
    }

    /**
     * 尝试为一条消息获取发送额度。
     *
     * @param tenantId 发送方租户
     * @param userId   发送方用户
     * @return true 放行；false 超限应丢弃
     */
    public boolean tryAcquire(Long tenantId, Long userId) {
        NettyProperties.RateLimit config = nettyProperties.getRateLimit();
        if (config == null || !config.isEnabled()) {
            return true;
        }
        if (tenantId == null || userId == null) {
            // 身份不全的消息由调用方拦截，这里不额外消耗额度
            return true;
        }

        String key = "ws:user:" + tenantId + ":" + userId;
        RateLimitSpec spec = new RateLimitSpec(config.getCount(),
                Duration.ofSeconds(config.getIntervalSecond()));
        try {
            boolean allowed = rateLimiter.tryAcquire(key, spec);
            if (!allowed) {
                logThrottled(key, tenantId, userId, config);
            }
            return allowed;
        } catch (RateLimiterUnavailableException e) {
            // fail-open：聊天不该因为限流组件抖动而整体中断
            log.error("[netty] 限流组件异常，本条消息放行 tenantId={}, userId={}", tenantId, userId, e);
            return true;
        }
    }

    /**
     * 超限日志按 key 限频：同一用户 10 秒内最多记一条
     */
    private void logThrottled(String key, Long tenantId, Long userId, NettyProperties.RateLimit config) {
        long now = System.currentTimeMillis();
        Long last = lastWarnAt.get(key);
        if (last != null && now - last < WARN_INTERVAL_MS) {
            return;
        }
        if (lastWarnAt.size() >= WARN_CACHE_LIMIT) {
            lastWarnAt.clear();
        }
        lastWarnAt.put(key, now);
        log.warn("[netty] 消息被限流：tenantId={}, userId={}, 规则={}条/{}秒",
                tenantId, userId, config.getCount(), config.getIntervalSecond());
    }
}
