package com.xd11cc.single.common.aspectj;

import com.xd11cc.single.common.annotation.RateLimit;
import com.xd11cc.single.common.exception.ServiceException;
import com.xd11cc.single.common.ratelimit.RateLimitKeyResolver;
import com.xd11cc.single.common.ratelimit.RateLimitSpec;
import com.xd11cc.single.common.ratelimit.RateLimiter;
import com.xd11cc.single.common.ratelimit.RateLimiterUnavailableException;
import com.xd11cc.single.common.ratelimit.Subject;
import com.xd11cc.single.enums.SystemErrorEnum;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

/**
 * {@link RateLimitAspect} 的测试。
 * <p>
 * 这一层只测策略：多维度短路、超限抛哪个业务码、组件故障时 fail-closed。
 * 身份派生与建桶分别由 {@code RateLimitKeyResolverTest} 和 {@code RateLimiterTest} 覆盖。
 * </p>
 *
 * @author xd11cc
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RateLimitAspectTest {

    @Mock
    private RateLimiter rateLimiter;

    @Mock
    private RateLimitKeyResolver keyResolver;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private MethodSignature methodSignature;

    private RateLimitAspect aspect;

    @BeforeEach
    void setUp() {
        aspect = new RateLimitAspect(rateLimiter, keyResolver);
        given(joinPoint.getSignature()).willReturn(methodSignature);
    }

    @Test
    @DisplayName("放行：调用原方法并返回其结果")
    void around_allowed() throws Throwable {
        given(methodSignature.getMethod()).willReturn(methodOf("single"));
        given(joinPoint.getArgs()).willReturn(new Object[]{"alice"});
        given(keyResolver.resolve(any(), anyString(), any(), any())).willReturn("login:ip:1.1.1.1");
        given(rateLimiter.tryAcquire(anyString(), any())).willReturn(true);
        given(joinPoint.proceed()).willReturn("ok");

        Object result = aspect.around(joinPoint, annotationOf("single"));

        assertThat(result).isEqualTo("ok");
        then(joinPoint).should().proceed();
    }

    @Test
    @DisplayName("超限：抛 TOO_MANY_REQUESTS，不执行原方法")
    void around_denied() throws Throwable {
        given(methodSignature.getMethod()).willReturn(methodOf("single"));
        given(joinPoint.getArgs()).willReturn(new Object[]{"alice"});
        given(keyResolver.resolve(any(), anyString(), any(), any())).willReturn("k");
        given(rateLimiter.tryAcquire(anyString(), any())).willReturn(false);

        assertThatThrownBy(() -> aspect.around(joinPoint, annotationOf("single")))
                .isInstanceOf(ServiceException.class)
                .satisfies(e -> assertThat(((ServiceException) e).getErrorCode())
                        .isEqualTo(SystemErrorEnum.TOO_MANY_REQUESTS));
        then(joinPoint).should(never()).proceed();
    }

    @Test
    @DisplayName("多维度：第一个维度拒绝后短路，不再查第二个")
    void around_multiSubject_shortCircuits() throws Throwable {
        given(methodSignature.getMethod()).willReturn(methodOf("multi"));
        given(joinPoint.getArgs()).willReturn(new Object[]{"alice"});
        given(keyResolver.resolve(eq(Subject.IP), anyString(), any(), any())).willReturn("ip-key");
        given(rateLimiter.tryAcquire(anyString(), any())).willReturn(false);

        assertThatThrownBy(() -> aspect.around(joinPoint, annotationOf("multi")))
                .isInstanceOf(ServiceException.class);

        then(keyResolver).should(never()).resolve(eq(Subject.EXPRESSION), anyString(), any(), any());
    }

    @Test
    @DisplayName("多维度：两个维度都放行才执行原方法")
    void around_multiSubject_allAllowed() throws Throwable {
        given(methodSignature.getMethod()).willReturn(methodOf("multi"));
        given(joinPoint.getArgs()).willReturn(new Object[]{"alice"});
        given(keyResolver.resolve(any(), anyString(), any(), any())).willReturn("k");
        given(rateLimiter.tryAcquire(anyString(), any())).willReturn(true);
        given(joinPoint.proceed()).willReturn("ok");

        assertThat(aspect.around(joinPoint, annotationOf("multi"))).isEqualTo("ok");
        then(rateLimiter).should(org.mockito.Mockito.times(2)).tryAcquire(anyString(), any());
    }

    @Test
    @DisplayName("组件故障：fail-closed，抛 RATE_LIMIT_UNAVAILABLE")
    void around_componentDown_failClosed() throws Throwable {
        given(methodSignature.getMethod()).willReturn(methodOf("single"));
        given(joinPoint.getArgs()).willReturn(new Object[]{"alice"});
        given(keyResolver.resolve(any(), anyString(), any(), any())).willReturn("k");
        given(rateLimiter.tryAcquire(anyString(), any()))
                .willThrow(new RateLimiterUnavailableException("k", new IllegalStateException("down")));

        assertThatThrownBy(() -> aspect.around(joinPoint, annotationOf("single")))
                .isInstanceOf(ServiceException.class)
                .satisfies(e -> assertThat(((ServiceException) e).getErrorCode())
                        .isEqualTo(SystemErrorEnum.RATE_LIMIT_UNAVAILABLE));
        then(joinPoint).should(never()).proceed();
    }

    @Test
    @DisplayName("配置非法：permits <= 0 直接报错")
    void around_invalidPermits() {
        assertThatThrownBy(() -> aspect.around(joinPoint, annotationOf("invalid")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("permits");
    }

    // ==================== 夹具 ====================

    private Method methodOf(String name) {
        try {
            return Sample.class.getDeclaredMethod(name, String.class);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
    }

    private RateLimit annotationOf(String name) {
        return methodOf(name).getAnnotation(RateLimit.class);
    }

    private static class Sample {

        @RateLimit(permits = 5, windowSeconds = 60)
        void single(String username) {
        }

        @RateLimit(permits = 5, windowSeconds = 60,
                subject = {Subject.IP, Subject.EXPRESSION}, expression = "#p0")
        void multi(String username) {
        }

        @RateLimit(permits = 0)
        void invalid(String username) {
        }
    }
}
