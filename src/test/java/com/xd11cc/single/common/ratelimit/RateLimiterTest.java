package com.xd11cc.single.common.ratelimit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

/**
 * {@link RateLimiter} 的测试。
 * <p>
 * 这一层只测「桶」：放行/拒绝、key 里带规格指纹、创建时设 TTL、组件异常抛专用异常。
 * 超限后怎么处理（抛业务异常还是丢消息）属于策略，由两个调用方各自测。
 * </p>
 *
 * @author xd11cc
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RateLimiterTest {

    private static final RateLimitSpec SPEC = new RateLimitSpec(5, Duration.ofSeconds(60));

    @Mock
    private RedissonClient redissonClient;

    @Mock
    private RRateLimiter bucket;

    private RateLimiter rateLimiter;

    @BeforeEach
    void setUp() {
        given(redissonClient.getRateLimiter(anyString())).willReturn(bucket);
        rateLimiter = new RateLimiter(redissonClient);
    }

    @Test
    @DisplayName("未超限：放行")
    void tryAcquire_allowed() {
        given(bucket.tryAcquire(1)).willReturn(true);

        assertThat(rateLimiter.tryAcquire("login:ip:1.2.3.4", SPEC)).isTrue();
    }

    @Test
    @DisplayName("超限：拒绝")
    void tryAcquire_denied() {
        given(bucket.tryAcquire(1)).willReturn(false);

        assertThat(rateLimiter.tryAcquire("login:ip:1.2.3.4", SPEC)).isFalse();
    }

    @Test
    @DisplayName("桶 key 含规格指纹：改阈值即换新桶")
    void buildBucketKey_containsFingerprint() {
        assertThat(rateLimiter.buildBucketKey("login:ip:1.2.3.4", SPEC))
                .isEqualTo("rate_limit:login:ip:1.2.3.4:5/60s");

        RateLimitSpec changed = new RateLimitSpec(10, Duration.ofSeconds(60));
        assertThat(rateLimiter.buildBucketKey("login:ip:1.2.3.4", changed))
                .as("阈值变化后 key 必须不同，否则 trySetRate 返回 false 导致新配额不生效")
                .isNotEqualTo(rateLimiter.buildBucketKey("login:ip:1.2.3.4", SPEC));
    }

    @Test
    @DisplayName("首次创建桶时按窗口两倍设 TTL")
    void tryAcquire_firstTime_setsTtl() {
        given(bucket.trySetRate(any(RateType.class), anyLong(), anyLong(), any(RateIntervalUnit.class)))
                .willReturn(true);
        given(bucket.tryAcquire(1)).willReturn(true);

        rateLimiter.tryAcquire("k", SPEC);

        then(bucket).should().expire(eq(Duration.ofSeconds(120)));
    }

    @Test
    @DisplayName("桶已存在时不再重复设置 TTL")
    void tryAcquire_existingBucket_doesNotResetTtl() {
        given(bucket.trySetRate(any(RateType.class), anyLong(), anyLong(), any(RateIntervalUnit.class)))
                .willReturn(false);
        given(bucket.tryAcquire(1)).willReturn(true);

        rateLimiter.tryAcquire("k", SPEC);

        then(bucket).should(never()).expire(org.mockito.ArgumentMatchers.<java.time.Duration>any());
    }

    @Test
    @DisplayName("组件异常：抛专用异常，策略交给调用方")
    void tryAcquire_redissonFailure_throwsUnavailable() {
        given(redissonClient.getRateLimiter(anyString()))
                .willThrow(new IllegalStateException("redis down"));

        assertThatThrownBy(() -> rateLimiter.tryAcquire("k", SPEC))
                .isInstanceOf(RateLimiterUnavailableException.class)
                .hasMessageContaining("rate_limit:k:5/60s");
    }
}
