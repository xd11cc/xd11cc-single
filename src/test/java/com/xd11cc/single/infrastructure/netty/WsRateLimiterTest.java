package com.xd11cc.single.infrastructure.netty;

import com.xd11cc.single.common.properties.NettyProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RedissonClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

/**
 * {@link WsRateLimiter} 的测试。
 * <p>
 * 三条关键路径：放行、超限拦截、组件异常时放行（fail-open）。
 * 最后一条是刻意与 HTTP 限流相反的策略，必须钉住，否则后来人容易"顺手"改成拒绝。
 * </p>
 *
 * @author xd11cc
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class WsRateLimiterTest {

    private static final Long TENANT_ID = 10L;
    private static final Long USER_ID = 1L;

    @Mock
    private RedissonClient redissonClient;

    @Mock
    private RRateLimiter rateLimiter;

    private NettyProperties properties;

    private WsRateLimiter wsRateLimiter;

    @BeforeEach
    void setUp() {
        properties = new NettyProperties();
        properties.getRateLimit().setEnabled(true);
        properties.getRateLimit().setCount(20);
        properties.getRateLimit().setIntervalSecond(10);

        given(redissonClient.getRateLimiter(anyString())).willReturn(rateLimiter);
        wsRateLimiter = new WsRateLimiter(redissonClient, properties);
    }

    @Test
    @DisplayName("未超额：放行，且 key 含租户与用户")
    void tryAcquire_allowed() {
        given(rateLimiter.tryAcquire(1)).willReturn(true);

        assertThat(wsRateLimiter.tryAcquire(TENANT_ID, USER_ID)).isTrue();
        then(redissonClient).should().getRateLimiter("request_limit:ws:10:1");
    }

    @Test
    @DisplayName("超额：拦截")
    void tryAcquire_denied() {
        given(rateLimiter.tryAcquire(1)).willReturn(false);

        assertThat(wsRateLimiter.tryAcquire(TENANT_ID, USER_ID)).isFalse();
    }

    @Test
    @DisplayName("Redis 异常：放行（fail-open），不阻断聊天")
    void tryAcquire_redisFailure_allowsMessage() {
        given(redissonClient.getRateLimiter(anyString()))
                .willThrow(new IllegalStateException("redis down"));

        assertThat(wsRateLimiter.tryAcquire(TENANT_ID, USER_ID)).isTrue();
    }

    @Test
    @DisplayName("限流关闭：直接放行，不访问 Redis")
    void tryAcquire_disabled_skipsRedis() {
        properties.getRateLimit().setEnabled(false);

        assertThat(wsRateLimiter.tryAcquire(TENANT_ID, USER_ID)).isTrue();
        then(redissonClient).should(never()).getRateLimiter(anyString());
    }

    @Test
    @DisplayName("身份为空：放行且不消耗额度")
    void tryAcquire_missingIdentity_allowsWithoutRedis() {
        assertThat(wsRateLimiter.tryAcquire(null, USER_ID)).isTrue();
        assertThat(wsRateLimiter.tryAcquire(TENANT_ID, null)).isTrue();
        then(redissonClient).should(never()).getRateLimiter(anyString());
    }

    @Test
    @DisplayName("首次创建 limiter 时设置过期，避免 key 残留")
    void tryAcquire_firstTime_setsExpire() {
        given(rateLimiter.trySetRate(any(), anyLong(), anyLong(), any())).willReturn(true);
        given(rateLimiter.tryAcquire(1)).willReturn(true);

        wsRateLimiter.tryAcquire(TENANT_ID, USER_ID);

        then(rateLimiter).should().expire(eq(java.time.Duration.ofSeconds(20)));
    }
}
