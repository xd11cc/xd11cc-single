package com.xd11cc.single.infrastructure.netty;

import com.xd11cc.single.common.properties.NettyProperties;
import com.xd11cc.single.common.ratelimit.RateLimitSpec;
import com.xd11cc.single.common.ratelimit.RateLimiter;
import com.xd11cc.single.common.ratelimit.RateLimiterUnavailableException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
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
    private RateLimiter rateLimiter;

    private NettyProperties properties;

    private WsRateLimiter wsRateLimiter;

    @BeforeEach
    void setUp() {
        properties = new NettyProperties();
        properties.getRateLimit().setEnabled(true);
        properties.getRateLimit().setCount(20);
        properties.getRateLimit().setIntervalSecond(10);

        wsRateLimiter = new WsRateLimiter(rateLimiter, properties);
    }

    @Test
    @DisplayName("未超额：放行，key 形如 ws:user:{tenant}:{user}")
    void tryAcquire_allowed() {
        given(rateLimiter.tryAcquire(anyString(), any())).willReturn(true);

        assertThat(wsRateLimiter.tryAcquire(TENANT_ID, USER_ID)).isTrue();

        then(rateLimiter).should().tryAcquire(eq("ws:user:10:1"), any());
    }

    @Test
    @DisplayName("超额：拦截")
    void tryAcquire_denied() {
        given(rateLimiter.tryAcquire(anyString(), any())).willReturn(false);

        assertThat(wsRateLimiter.tryAcquire(TENANT_ID, USER_ID)).isFalse();
    }

    @Test
    @DisplayName("限流组件异常：放行（fail-open），不阻断聊天")
    void tryAcquire_componentDown_failOpen() {
        given(rateLimiter.tryAcquire(anyString(), any()))
                .willThrow(new RateLimiterUnavailableException("k", new IllegalStateException("redis down")));

        assertThat(wsRateLimiter.tryAcquire(TENANT_ID, USER_ID)).isTrue();
    }

    @Test
    @DisplayName("限流关闭：直接放行，不访问限流组件")
    void tryAcquire_disabled_skipsLimiter() {
        properties.getRateLimit().setEnabled(false);

        assertThat(wsRateLimiter.tryAcquire(TENANT_ID, USER_ID)).isTrue();
        then(rateLimiter).should(never()).tryAcquire(anyString(), any());
    }

    @Test
    @DisplayName("身份为空：放行且不消耗额度")
    void tryAcquire_missingIdentity_allowsWithoutLimiter() {
        assertThat(wsRateLimiter.tryAcquire(null, USER_ID)).isTrue();
        assertThat(wsRateLimiter.tryAcquire(TENANT_ID, null)).isTrue();
        then(rateLimiter).should(never()).tryAcquire(anyString(), any());
    }

    @Test
    @DisplayName("按配置构造规格：20 次 / 10 秒")
    void tryAcquire_buildsSpecFromConfig() {
        given(rateLimiter.tryAcquire(anyString(), any())).willReturn(true);

        wsRateLimiter.tryAcquire(TENANT_ID, USER_ID);

        org.mockito.ArgumentCaptor<RateLimitSpec> captor =
                org.mockito.ArgumentCaptor.forClass(RateLimitSpec.class);
        then(rateLimiter).should().tryAcquire(anyString(), captor.capture());
        assertThat(captor.getValue().getPermits()).isEqualTo(20);
        assertThat(captor.getValue().getWindow().getSeconds()).isEqualTo(10);
    }
}
