package com.xd11cc.single.config.aspectj;

import com.xd11cc.single.config.annotation.RequestLimit;
import com.xd11cc.single.config.exception.RateLimitException;
import com.xd11cc.single.constants.CacheConstants;
import com.xd11cc.single.enums.RequestLimitEnum;
import com.xd11cc.single.utils.IpUtils;
import com.xd11cc.single.utils.SecurityUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RedissonClient;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

class RequestLimitAspectTest {
    private final RequestLimitAspect aspect = new RequestLimitAspect();

    // ==================== 参数校验 ====================

    @Test
    void validateRateLimitParams_count为0_抛异常() {
        ProceedingJoinPoint joinPoint = mockJoinPoint(InvalidCountService.METHOD);

        assertThatThrownBy(() -> aspect.around(joinPoint))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("count必须大于0");
    }

    @Test
    void validateRateLimitParams_time为0_抛异常() {
        ProceedingJoinPoint joinPoint = mockJoinPoint(InvalidTimeService.METHOD);

        assertThatThrownBy(() -> aspect.around(joinPoint))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("time必须大于0");
    }

    // ==================== 限流放行 ====================

    @Test
    void around_限流未触发_放行并返回结果() throws Throwable {
        ProceedingJoinPoint joinPoint = mockJoinPoint(ValidLimitService.METHOD);
        given(joinPoint.proceed()).willReturn("result");

        RRateLimiter rateLimiter = mock(RRateLimiter.class);
        given(rateLimiter.tryAcquire(1)).willReturn(true);
        given(rateLimiter.trySetRate(any(), anyLong(), anyLong(), any())).willReturn(true);
        RedissonClient redissonClient = mock(RedissonClient.class);
        given(redissonClient.getRateLimiter(anyString())).willReturn(rateLimiter);

        injectRedissonClient(redissonClient);

        try (MockedStatic<IpUtils> ipMock = mockStatic(IpUtils.class);
             MockedStatic<SecurityUtils> secMock = mockStatic(SecurityUtils.class)) {
            ipMock.when(IpUtils::getIpAddr).thenReturn("127.0.0.1");
            secMock.when(SecurityUtils::getUserId).thenReturn(99L);

            Object result = aspect.around(joinPoint);

            assertThat(result).isEqualTo("result");
            verify(rateLimiter).trySetRate(any(), anyLong(), anyLong(), any());
            verify(rateLimiter).expire(any(java.time.Duration.class));
        }
    }

    // ==================== 限流触发 ====================

    @Test
    void around_限流触发_抛RateLimitException() throws Throwable {
        ProceedingJoinPoint joinPoint = mockJoinPoint(ValidLimitService.METHOD);

        RRateLimiter rateLimiter = mock(RRateLimiter.class);
        given(rateLimiter.tryAcquire(1)).willReturn(false);
        given(rateLimiter.trySetRate(any(), anyLong(), anyLong(), any())).willReturn(true);
        RedissonClient redissonClient = mock(RedissonClient.class);
        given(redissonClient.getRateLimiter(anyString())).willReturn(rateLimiter);

        injectRedissonClient(redissonClient);

        try (MockedStatic<IpUtils> ipMock = mockStatic(IpUtils.class);
             MockedStatic<SecurityUtils> secMock = mockStatic(SecurityUtils.class)) {
            ipMock.when(IpUtils::getIpAddr).thenReturn("127.0.0.1");
            secMock.when(SecurityUtils::getUserId).thenReturn(99L);

            assertThatThrownBy(() -> aspect.around(joinPoint))
                    .isInstanceOf(RateLimitException.class);
        }
    }

    // ==================== 不同限流类型 ====================

    @Test
    void around_IP限流类型_key包含IP() throws Throwable {
        ProceedingJoinPoint joinPoint = mockJoinPoint(IpLimitService.METHOD);
        given(joinPoint.proceed()).willReturn("result");

        RRateLimiter rateLimiter = mock(RRateLimiter.class);
        given(rateLimiter.tryAcquire(1)).willReturn(true);
        given(rateLimiter.trySetRate(any(), anyLong(), anyLong(), any())).willReturn(true);
        RedissonClient redissonClient = mock(RedissonClient.class);
        given(redissonClient.getRateLimiter(anyString())).willReturn(rateLimiter);

        injectRedissonClient(redissonClient);

        try (MockedStatic<IpUtils> ipMock = mockStatic(IpUtils.class);
             MockedStatic<SecurityUtils> secMock = mockStatic(SecurityUtils.class)) {
            ipMock.when(IpUtils::getIpAddr).thenReturn("192.168.1.1");
            secMock.when(SecurityUtils::getUserId).thenReturn(99L);

            aspect.around(joinPoint);

            verify(redissonClient).getRateLimiter(startsWith(
                    CacheConstants.REQUEST_LIMIT_KEY + "default:ip:192.168.1.1"));
        }
    }

    @Test
    void around_USER限流类型_key包含userId() throws Throwable {
        ProceedingJoinPoint joinPoint = mockJoinPoint(UserLimitService.METHOD);
        given(joinPoint.proceed()).willReturn("result");

        RRateLimiter rateLimiter = mock(RRateLimiter.class);
        given(rateLimiter.tryAcquire(1)).willReturn(true);
        given(rateLimiter.trySetRate(any(), anyLong(), anyLong(), any())).willReturn(true);
        RedissonClient redissonClient = mock(RedissonClient.class);
        given(redissonClient.getRateLimiter(anyString())).willReturn(rateLimiter);

        injectRedissonClient(redissonClient);

        try (MockedStatic<IpUtils> ipMock = mockStatic(IpUtils.class);
             MockedStatic<SecurityUtils> secMock = mockStatic(SecurityUtils.class)) {
            ipMock.when(IpUtils::getIpAddr).thenReturn("127.0.0.1");
            secMock.when(SecurityUtils::getUserId).thenReturn(88L);

            aspect.around(joinPoint);

            verify(redissonClient).getRateLimiter(startsWith(
                    CacheConstants.REQUEST_LIMIT_KEY + "default:user:88"));
        }
    }

    @Test
    void around_DEFAULT限流类型_key包含方法名() throws Throwable {
        ProceedingJoinPoint joinPoint = mockJoinPoint(ValidLimitService.METHOD);
        given(joinPoint.proceed()).willReturn("result");

        RRateLimiter rateLimiter = mock(RRateLimiter.class);
        given(rateLimiter.tryAcquire(1)).willReturn(true);
        given(rateLimiter.trySetRate(any(), anyLong(), anyLong(), any())).willReturn(true);
        RedissonClient redissonClient = mock(RedissonClient.class);
        given(redissonClient.getRateLimiter(anyString())).willReturn(rateLimiter);

        injectRedissonClient(redissonClient);

        try (MockedStatic<IpUtils> ipMock = mockStatic(IpUtils.class);
             MockedStatic<SecurityUtils> secMock = mockStatic(SecurityUtils.class)) {
            ipMock.when(IpUtils::getIpAddr).thenReturn("127.0.0.1");
            secMock.when(SecurityUtils::getUserId).thenReturn(99L);

            aspect.around(joinPoint);

            verify(redissonClient).getRateLimiter(org.mockito.ArgumentMatchers.matches(
                    CacheConstants.REQUEST_LIMIT_KEY + "default:method:"
                            + ValidLimitService.class.getSimpleName() + "\\.someMethod"));
        }
    }

    // ==================== 自定义 key 前缀 ====================

    @Test
    void around_自定义key前缀_使用自定义前缀() throws Throwable {
        ProceedingJoinPoint joinPoint = mockJoinPoint(CustomPrefixService.METHOD);
        given(joinPoint.proceed()).willReturn("result");

        RRateLimiter rateLimiter = mock(RRateLimiter.class);
        given(rateLimiter.tryAcquire(1)).willReturn(true);
        given(rateLimiter.trySetRate(any(), anyLong(), anyLong(), any())).willReturn(true);
        RedissonClient redissonClient = mock(RedissonClient.class);
        given(redissonClient.getRateLimiter(anyString())).willReturn(rateLimiter);

        injectRedissonClient(redissonClient);

        try (MockedStatic<IpUtils> ipMock = mockStatic(IpUtils.class);
             MockedStatic<SecurityUtils> secMock = mockStatic(SecurityUtils.class)) {
            ipMock.when(IpUtils::getIpAddr).thenReturn("127.0.0.1");
            secMock.when(SecurityUtils::getUserId).thenReturn(99L);

            aspect.around(joinPoint);

            verify(redissonClient).getRateLimiter(startsWith(
                    CacheConstants.REQUEST_LIMIT_KEY + "custom:"));
        }
    }

    // ==================== 辅助方法 ====================

    @SuppressWarnings("unchecked")
    private ProceedingJoinPoint mockJoinPoint(Method method) {
        try {
            MethodSignature signature = mock(MethodSignature.class);
            given(signature.getMethod()).willReturn(method);
            given(signature.getDeclaringTypeName()).willReturn(method.getDeclaringClass().getName());
            given(signature.getName()).willReturn(method.getName());

            Object targetInstance = method.getDeclaringClass().newInstance();
            ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
            given(joinPoint.getSignature()).willReturn(signature);
            given(joinPoint.getTarget()).willReturn(targetInstance);
            return joinPoint;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void injectRedissonClient(RedissonClient redissonClient) {
        try {
            java.lang.reflect.Field field = RequestLimitAspect.class.getDeclaredField("redissonClient");
            field.setAccessible(true);
            field.set(aspect, redissonClient);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // ==================== 测试用服务类 ====================

    static class ValidLimitService {
        public static final Method METHOD;
        static {
            try { METHOD = ValidLimitService.class.getMethod("someMethod"); } catch (NoSuchMethodException e) { throw new RuntimeException(e); }
        }
        @RequestLimit(count = 10, time = 60)
        public void someMethod() {}
    }

    static class InvalidCountService {
        public static final Method METHOD;
        static {
            try { METHOD = InvalidCountService.class.getMethod("invalidCount"); } catch (NoSuchMethodException e) { throw new RuntimeException(e); }
        }
        @RequestLimit(count = 0, time = 60)
        public void invalidCount() {}
    }

    static class InvalidTimeService {
        public static final Method METHOD;
        static {
            try { METHOD = InvalidTimeService.class.getMethod("invalidTime"); } catch (NoSuchMethodException e) { throw new RuntimeException(e); }
        }
        @RequestLimit(count = 10, time = 0)
        public void invalidTime() {}
    }

    static class IpLimitService {
        public static final Method METHOD;
        static {
            try { METHOD = IpLimitService.class.getMethod("ipMethod"); } catch (NoSuchMethodException e) { throw new RuntimeException(e); }
        }
        @RequestLimit(count = 10, time = 60, type = RequestLimitEnum.IP)
        public void ipMethod() {}
    }

    static class UserLimitService {
        public static final Method METHOD;
        static {
            try { METHOD = UserLimitService.class.getMethod("userMethod"); } catch (NoSuchMethodException e) { throw new RuntimeException(e); }
        }
        @RequestLimit(count = 10, time = 60, type = RequestLimitEnum.USER)
        public void userMethod() {}
    }

    static class CustomPrefixService {
        public static final Method METHOD;
        static {
            try { METHOD = CustomPrefixService.class.getMethod("customMethod"); } catch (NoSuchMethodException e) { throw new RuntimeException(e); }
        }
        @RequestLimit(key = "custom:", count = 10, time = 60)
        public void customMethod() {}
    }
}
