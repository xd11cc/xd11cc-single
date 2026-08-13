package com.xd11cc.single.config.aspectj;

import com.xd11cc.single.config.annotation.RedisLock;
import com.xd11cc.single.config.annotation.RedisLock.LockMode;
import com.xd11cc.single.constants.CacheConstants;
import com.xd11cc.single.enums.SystemErrorEnum;
import com.xd11cc.single.config.exception.ServiceException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.Test;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

class RedisLockAspectTest {
    private final RedisLockAspect aspect = new RedisLockAspect();

    // ==================== validateLockParams ====================

    @Test
    void validateLockParams_waitTime小于0_抛异常() {
        ProceedingJoinPoint joinPoint = mockJoinPoint(NegativeWaitTimeService.METHOD);

        assertThatThrownBy(() -> aspect.around(joinPoint))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("waitTime");
    }

    @Test
    void validateLockParams_leaseTime小于0_抛异常() {
        ProceedingJoinPoint joinPoint = mockJoinPoint(NegativeLeaseTimeService.METHOD);

        assertThatThrownBy(() -> aspect.around(joinPoint))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("leaseTime");
    }

    // ==================== tryLock 参数组合 ====================

    @Test
    void around_waitTime加leaseTime_调用三参tryLock() throws Throwable {
        ProceedingJoinPoint joinPoint = mockJoinPoint(WaitAndLeaseService.METHOD);
        given(joinPoint.proceed()).willReturn("result");

        RLock rLock = mock(RLock.class);
        given(rLock.tryLock(5, 10, TimeUnit.SECONDS)).willReturn(true);
        given(rLock.isHeldByCurrentThread()).willReturn(true);
        RedissonClient redissonClient = mock(RedissonClient.class);
        given(redissonClient.getLock(anyString())).willReturn(rLock);

        injectRedissonClient(redissonClient);

        Object result = aspect.around(joinPoint);

        assertThat(result).isEqualTo("result");
        verify(rLock).unlock();
    }

    @Test
    void around_仅waitTime_调用二参tryLock() throws Throwable {
        ProceedingJoinPoint joinPoint = mockJoinPoint(WaitTimeService.METHOD);
        given(joinPoint.proceed()).willReturn("result");

        RLock rLock = mock(RLock.class);
        given(rLock.tryLock(5, TimeUnit.SECONDS)).willReturn(true);
        given(rLock.isHeldByCurrentThread()).willReturn(true);
        RedissonClient redissonClient = mock(RedissonClient.class);
        given(redissonClient.getLock(anyString())).willReturn(rLock);

        injectRedissonClient(redissonClient);

        Object result = aspect.around(joinPoint);

        assertThat(result).isEqualTo("result");
    }

    @Test
    void around_仅leaseTime_调用二参tryLock() throws Throwable {
        ProceedingJoinPoint joinPoint = mockJoinPoint(LeaseTimeService.METHOD);
        given(joinPoint.proceed()).willReturn("result");

        RLock rLock = mock(RLock.class);
        given(rLock.tryLock(10, TimeUnit.MINUTES)).willReturn(true);
        given(rLock.isHeldByCurrentThread()).willReturn(true);
        RedissonClient redissonClient = mock(RedissonClient.class);
        given(redissonClient.getLock(anyString())).willReturn(rLock);

        injectRedissonClient(redissonClient);

        Object result = aspect.around(joinPoint);

        assertThat(result).isEqualTo("result");
    }

    @Test
    void around_均未设置_调用无参tryLock() throws Throwable {
        ProceedingJoinPoint joinPoint = mockJoinPoint(NoWaitService.METHOD);
        given(joinPoint.proceed()).willReturn("result");

        RLock rLock = mock(RLock.class);
        given(rLock.tryLock()).willReturn(true);
        given(rLock.isHeldByCurrentThread()).willReturn(true);
        RedissonClient redissonClient = mock(RedissonClient.class);
        given(redissonClient.getLock(anyString())).willReturn(rLock);

        injectRedissonClient(redissonClient);

        Object result = aspect.around(joinPoint);

        assertThat(result).isEqualTo("result");
    }

    // ==================== 锁获取失败 ====================

    @Test
    void around_锁获取失败_抛ServiceException() throws Throwable {
        ProceedingJoinPoint joinPoint = mockJoinPoint(WaitAndLeaseService.METHOD);

        RLock rLock = mock(RLock.class);
        given(rLock.tryLock(5, 10, TimeUnit.SECONDS)).willReturn(false);
        RedissonClient redissonClient = mock(RedissonClient.class);
        given(redissonClient.getLock(anyString())).willReturn(rLock);

        injectRedissonClient(redissonClient);

        assertThatThrownBy(() -> aspect.around(joinPoint))
                .isInstanceOf(ServiceException.class)
                .satisfies(ex -> {
                    ServiceException se = (ServiceException) ex;
                    assertThat(se.getErrorCode()).isEqualTo(SystemErrorEnum.LOCK_ACQUIRE_FAILED);
                });
    }

    // ==================== InterruptedException ====================

    @Test
    void around_tryLock抛InterruptedException_抛ServiceException() throws Throwable {
        ProceedingJoinPoint joinPoint = mockJoinPoint(WaitAndLeaseService.METHOD);

        RLock rLock = mock(RLock.class);
        given(rLock.tryLock(5, 10, TimeUnit.SECONDS)).willThrow(new InterruptedException("中断"));
        RedissonClient redissonClient = mock(RedissonClient.class);
        given(redissonClient.getLock(anyString())).willReturn(rLock);

        injectRedissonClient(redissonClient);

        assertThatThrownBy(() -> aspect.around(joinPoint))
                .isInstanceOf(ServiceException.class)
                .satisfies(ex -> assertThat(((ServiceException) ex).getErrorCode()).isEqualTo(SystemErrorEnum.LOCK_ACQUIRE_FAILED));

        assertThat(Thread.currentThread().isInterrupted()).isTrue();
        Thread.interrupted(); // 清除中断状态，避免影响其他测试
    }

    // ==================== buildLockKey ====================

    @Test
    void around_key为空_拼接所有参数() throws Throwable {
        ProceedingJoinPoint joinPoint = mockJoinPoint(TargetService.METHOD, "arg1", "arg2");
        given(joinPoint.proceed()).willReturn("result");

        RLock rLock = mock(RLock.class);
        given(rLock.tryLock()).willReturn(true);
        given(rLock.isHeldByCurrentThread()).willReturn(true);
        RedissonClient redissonClient = mock(RedissonClient.class);
        given(redissonClient.getLock(anyString())).willReturn(rLock);

        injectRedissonClient(redissonClient);

        aspect.around(joinPoint);

        verify(redissonClient).getLock(startsWith(
                CacheConstants.REDIS_LOCK_KEY + "lock:" + TargetService.class.getSimpleName() + ".noKeyMethod:arg1|arg2|"));
    }

    // ==================== SpEL 解析 ====================

    @Test
    void around_SpEL表达式_解析参数值() throws Throwable {
        ProceedingJoinPoint joinPoint = mockJoinPoint(TargetLockMethod.METHOD, 42L, "other");
        given(joinPoint.proceed()).willReturn("result");

        RLock rLock = mock(RLock.class);
        given(rLock.tryLock()).willReturn(true);
        given(rLock.isHeldByCurrentThread()).willReturn(true);
        RedissonClient redissonClient = mock(RedissonClient.class);
        given(redissonClient.getLock(anyString())).willReturn(rLock);

        injectRedissonClient(redissonClient);

        aspect.around(joinPoint);

        verify(redissonClient).getLock(startsWith(
                CacheConstants.REDIS_LOCK_KEY + "lock:" + TargetLockMethod.class.getSimpleName() + ".spelMethod:42"));
    }

    // ==================== 自定义 prefix ====================

    @Test
    void around_自定义prefix_前缀包含自定义值() throws Throwable {
        ProceedingJoinPoint joinPoint = mockJoinPoint(CustomPrefixService.METHOD);
        given(joinPoint.proceed()).willReturn("result");

        RLock rLock = mock(RLock.class);
        given(rLock.tryLock()).willReturn(true);
        given(rLock.isHeldByCurrentThread()).willReturn(true);
        RedissonClient redissonClient = mock(RedissonClient.class);
        given(redissonClient.getLock(anyString())).willReturn(rLock);

        injectRedissonClient(redissonClient);

        aspect.around(joinPoint);

        verify(redissonClient).getLock(startsWith(
                CacheConstants.REDIS_LOCK_KEY + "myprefix:" + CustomPrefixService.class.getSimpleName() + "."));
    }

    // ==================== 辅助方法 ====================

    @SuppressWarnings("unchecked")
    private ProceedingJoinPoint mockJoinPoint(Method method, Object... args) {
        try {
            MethodSignature signature = mock(MethodSignature.class);
            given(signature.getMethod()).willReturn(method);
            given(signature.getDeclaringType()).willReturn(method.getDeclaringClass());
            given(signature.getDeclaringTypeName()).willReturn(method.getDeclaringClass().getName());
            given(signature.getName()).willReturn(method.getName());

            Object targetInstance = method.getDeclaringClass().newInstance();
            ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
            given(joinPoint.getSignature()).willReturn(signature);
            given(joinPoint.getTarget()).willReturn(targetInstance);
            given(joinPoint.getArgs()).willReturn(args);
            return joinPoint;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void injectRedissonClient(RedissonClient redissonClient) {
        try {
            java.lang.reflect.Field field = RedisLockAspect.class.getDeclaredField("redissonClient");
            field.setAccessible(true);
            field.set(aspect, redissonClient);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // ==================== 测试用服务类 ====================

    static class NegativeWaitTimeService {
        public static final Method METHOD;
        static {
            try { METHOD = NegativeWaitTimeService.class.getMethod("invalidMethod"); } catch (NoSuchMethodException e) { throw new RuntimeException(e); }
        }
        @RedisLock(waitTime = -1, leaseTime = 0, unit = TimeUnit.SECONDS)
        public void invalidMethod() {}
    }

    static class NegativeLeaseTimeService {
        public static final Method METHOD;
        static {
            try { METHOD = NegativeLeaseTimeService.class.getMethod("invalidMethod"); } catch (NoSuchMethodException e) { throw new RuntimeException(e); }
        }
        @RedisLock(waitTime = 0, leaseTime = -1, unit = TimeUnit.SECONDS)
        public void invalidMethod() {}
    }

    static class TargetService {
        public static final Method METHOD;
        static {
            try { METHOD = TargetService.class.getMethod("noKeyMethod"); } catch (NoSuchMethodException e) { throw new RuntimeException(e); }
        }
        @RedisLock
        public void noKeyMethod() {}
    }

    static class WaitAndLeaseService {
        public static final Method METHOD;
        static {
            try { METHOD = WaitAndLeaseService.class.getMethod("lockMethod"); } catch (NoSuchMethodException e) { throw new RuntimeException(e); }
        }
        @RedisLock(waitTime = 5, leaseTime = 10, unit = TimeUnit.SECONDS)
        public void lockMethod() {}
    }

    static class WaitTimeService {
        public static final Method METHOD;
        static {
            try { METHOD = WaitTimeService.class.getMethod("lockMethod"); } catch (NoSuchMethodException e) { throw new RuntimeException(e); }
        }
        @RedisLock(waitTime = 5, leaseTime = 0, unit = TimeUnit.SECONDS)
        public void lockMethod() {}
    }

    static class LeaseTimeService {
        public static final Method METHOD;
        static {
            try { METHOD = LeaseTimeService.class.getMethod("lockMethod"); } catch (NoSuchMethodException e) { throw new RuntimeException(e); }
        }
        @RedisLock(waitTime = 0, leaseTime = 10, unit = TimeUnit.MINUTES)
        public void lockMethod() {}
    }

    static class NoWaitService {
        public static final Method METHOD;
        static {
            try { METHOD = NoWaitService.class.getMethod("lockMethod"); } catch (NoSuchMethodException e) { throw new RuntimeException(e); }
        }
        @RedisLock(waitTime = 0, leaseTime = 0, unit = TimeUnit.SECONDS)
        public void lockMethod() {}
    }

    static class TargetLockMethod {
        public static final Method METHOD;
        static {
            try { METHOD = TargetLockMethod.class.getMethod("spelMethod", Long.class, String.class); } catch (NoSuchMethodException e) { throw new RuntimeException(e); }
        }
        @RedisLock(key = "#userId")
        public void spelMethod(Long userId, String other) {}
    }

    static class CustomPrefixService {
        public static final Method METHOD;
        static {
            try { METHOD = CustomPrefixService.class.getMethod("customMethod"); } catch (NoSuchMethodException e) { throw new RuntimeException(e); }
        }
        @RedisLock(prefix = "myprefix")
        public void customMethod() {}
    }
}
