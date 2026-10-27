package com.xd11cc.single.common.mq;

import com.xd11cc.single.infrastructure.redis.RedisCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * MQ 消费幂等保护。
 * <p>
 * 消息可能因为重试、broker 重投、消费者重启等原因被重复投递，
 * 业务方法因此可能被执行多次。这里按 {@code messageId} 记录「已成功消费」标记，
 * 重复消息直接跳过并 ACK。
 * </p>
 *
 * <p><b>只标记成功</b>：失败的消息不写标记，否则下次投递会被误判为已处理，
 * 真正的重试机会就丢失了。代价是失败消息的每次重试都会真正执行业务逻辑，
 * 这需要业务本身具备一定的抗重复能力（这也正是只采纳 A 方案的原因）。</p>
 *
 * <p><b>失败即放行</b>：Redis 不可用时返回「未消费过」，宁可重复执行也不阻塞消息处理。</p>
 *
 * @author xd11cc
 */
@Slf4j
@Component
public class MqIdempotentGuard {

    /**
     * 幂等标记 key 前缀
     */
    private static final String KEY_PREFIX = "mq_consumed:";

    /**
     * 标记保留时长：需覆盖 broker 重投、人工重放的时间窗口
     */
    private static final long EXPIRE_HOURS = 24L;

    @Autowired
    private RedisCache redisCache;

    /**
     * 构建幂等 key。
     * <p>
     * tenantId 直接编进 key：消费时租户上下文可能尚未生效，
     * 不能依赖 {@code RedisCache} 的自动租户后缀。
     * </p>
     */
    String buildKey(String messageId, Long tenantId) {
        return KEY_PREFIX + (tenantId == null ? "0" : tenantId) + ":" + messageId;
    }

    /**
     * 判断消息是否已被成功消费过。
     *
     * @param messageId 消息 ID
     * @param tenantId  租户 ID
     * @return {@code true} 表示已消费，应跳过
     */
    public boolean isConsumed(String messageId, Long tenantId) {
        if (messageId == null) {
            return false;
        }
        try {
            return Boolean.TRUE.equals(redisCache.hasKey(buildKey(messageId, tenantId)));
        } catch (Exception e) {
            // 降级放行：幂等是优化项，不能因为 Redis 故障导致消息无法处理
            log.warn("[MQ] 幂等检查失败，按未消费处理 messageId={}", messageId, e);
            return false;
        }
    }

    /**
     * 标记消息已成功消费。
     *
     * @param messageId 消息 ID
     * @param tenantId  租户 ID
     */
    public void markConsumed(String messageId, Long tenantId) {
        if (messageId == null) {
            return;
        }
        try {
            redisCache.setCacheObject(buildKey(messageId, tenantId), "1", EXPIRE_HOURS, TimeUnit.HOURS);
        } catch (Exception e) {
            // 标记失败只影响幂等效果，不应影响本次消费结果
            log.warn("[MQ] 幂等标记写入失败 messageId={}", messageId, e);
        }
    }
}
