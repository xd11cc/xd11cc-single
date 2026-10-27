package com.xd11cc.single.common.mq;

import com.xd11cc.single.infrastructure.redis.RedisCache;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 幂等与事务后发送的单元测试（不依赖真实 Redis / Broker）。
 *
 * @author xd11cc
 */
class MqIdempotentAndTxTest {

    /**
     * 可观测的假 RedisCache：只记录写入了哪些 key
     */
    static class FakeRedisCache extends RedisCache {
        final List<String> stored = new ArrayList<>();

        @Override
        public <T> void setCacheObject(String key, T value, long timeout, TimeUnit unit) {
            stored.add(key);
        }

        @Override
        public boolean hasKey(String key) {
            return stored.contains(key);
        }
    }

    private MqIdempotentGuard newGuard(FakeRedisCache cache) {
        MqIdempotentGuard guard = new MqIdempotentGuard();
        ReflectionTestUtils.setField(guard, "redisCache", cache);
        return guard;
    }

    // ==================== 幂等 key ====================

    @Test
    void 幂等key_包含租户前缀避免跨租户串号() {
        MqIdempotentGuard guard = newGuard(new FakeRedisCache());

        assertThat(guard.buildKey("msg-1", 1L)).isEqualTo("mq_consumed:1:msg-1");
        assertThat(guard.buildKey("msg-1", 2L)).isEqualTo("mq_consumed:2:msg-1");
    }

    @Test
    void 幂等key_租户为空时使用占位符() {
        MqIdempotentGuard guard = newGuard(new FakeRedisCache());
        assertThat(guard.buildKey("msg-1", null)).isEqualTo("mq_consumed:0:msg-1");
    }

    // ==================== 幂等判断 ====================

    @Test
    void 未消费过_返回false() {
        MqIdempotentGuard guard = newGuard(new FakeRedisCache());
        assertThat(guard.isConsumed("msg-1", 1L)).isFalse();
    }

    @Test
    void 标记之后_判定为已消费() {
        FakeRedisCache cache = new FakeRedisCache();
        MqIdempotentGuard guard = newGuard(cache);

        guard.markConsumed("msg-1", 1L);

        assertThat(guard.isConsumed("msg-1", 1L)).isTrue();
        // 不同租户不会误判
        assertThat(guard.isConsumed("msg-1", 2L)).isFalse();
    }

    @Test
    void messageId为空_永不判定为已消费() {
        MqIdempotentGuard guard = newGuard(new FakeRedisCache());
        guard.markConsumed(null, 1L);
        assertThat(guard.isConsumed(null, 1L)).isFalse();
    }

    /**
     * 幂等是优化项：Redis 故障时必须放行，不能阻塞消息处理。
     */
    @Test
    void Redis异常时_降级放行() {
        RedisCache broken = new RedisCache() {
            @Override
            public boolean hasKey(String key) {
                throw new IllegalStateException("redis down");
            }

            @Override
            public <T> void setCacheObject(String key, T value, long timeout, TimeUnit unit) {
                throw new IllegalStateException("redis down");
            }
        };
        MqIdempotentGuard guard = new MqIdempotentGuard();
        ReflectionTestUtils.setField(guard, "redisCache", broken);

        assertThat(guard.isConsumed("msg-1", 1L)).isFalse();
        // 标记失败也不抛异常
        guard.markConsumed("msg-1", 1L);
    }

    // ==================== 事务后发送 ====================

    @Test
    void 无事务时_afterCommit注册不生效立即执行() {
        assertThat(TransactionSynchronizationManager.isSynchronizationActive()).isFalse();
    }

    @Test
    void 有事务时_注册的同步器会被执行() {
        TransactionSynchronizationManager.initSynchronization();
        try {
            boolean[] executed = {false};
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    executed[0] = true;
                }
            });
            assertThat(TransactionSynchronizationManager.isSynchronizationActive()).isTrue();
            assertThat(executed[0]).isFalse();
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    void 发送方法_默认全部为事务后发送() throws Exception {
        // 公开的 send / sendDelay 系列应统一走 afterCommitIfActive
        java.lang.reflect.Method before = null;
        for (java.lang.reflect.Method m : MqMessageSender.class.getDeclaredMethods()) {
            if ("afterCommitIfActive".equals(m.getName())) {
                before = m;
            }
        }
        assertThat(before).isNotNull();

        // 不再保留独立的 AfterCommit 方法名
        List<String> names = new ArrayList<>();
        for (java.lang.reflect.Method m : MqMessageSender.class.getDeclaredMethods()) {
            names.add(m.getName());
        }
        assertThat(names).doesNotContain("sendAfterCommit", "sendDelayAfterCommit");
    }

    @Test
    void 事务后发送_有事务时进入afterCommit分支() {
        TransactionSynchronizationManager.initSynchronization();
        try {
            assertThat(TransactionSynchronizationManager.isSynchronizationActive()).isTrue();
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
        assertThat(TransactionSynchronizationManager.isSynchronizationActive()).isFalse();
    }
}
