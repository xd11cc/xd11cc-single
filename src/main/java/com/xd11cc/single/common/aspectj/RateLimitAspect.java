package com.xd11cc.single.common.aspectj;

import com.xd11cc.single.common.annotation.RateLimit;
import com.xd11cc.single.common.exception.ServiceException;
import com.xd11cc.single.common.ratelimit.RateLimitKeyResolver;
import com.xd11cc.single.common.ratelimit.RateLimitSpec;
import com.xd11cc.single.common.ratelimit.RateLimiter;
import com.xd11cc.single.common.ratelimit.RateLimiterUnavailableException;
import com.xd11cc.single.common.ratelimit.Subject;
import com.xd11cc.single.enums.SystemErrorEnum;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.Duration;

/**
 * 接口限流的策略层。
 * <p>
 * 与 {@link RateLimiter} 的分工：那边只管桶，这里决定
 * 「按哪些维度派生身份」以及「超限/组件故障时怎么办」。
 * </p>
 * <p>
 * HTTP 入口的策略是 <b>fail-closed</b>：限流组件不可用时拒绝请求。
 * WebSocket 入口正相反（fail-open），因为断连的代价远大于丢一条消息。
 * 这个差异是刻意保留的，不要"统一"。
 * </p>
 *
 * @author xd11cc
 */
@Slf4j
@Aspect
@Component
@Order(5)
public class RateLimitAspect {

    private final RateLimiter rateLimiter;

    private final RateLimitKeyResolver keyResolver;

    public RateLimitAspect(RateLimiter rateLimiter, RateLimitKeyResolver keyResolver) {
        this.rateLimiter = rateLimiter;
        this.keyResolver = keyResolver;
    }

    @Around("@annotation(rateLimit)")
    public Object around(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        validate(rateLimit);

        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        Object[] args = joinPoint.getArgs();
        RateLimitSpec spec = new RateLimitSpec(rateLimit.permits(),
                Duration.ofSeconds(rateLimit.windowSeconds()));

        // 多维度：任一超限即拒绝；按声明顺序短路，已被拦下的请求不再消耗后续维度的配额
        for (Subject subject : rateLimit.subject()) {
            String key = keyResolver.resolve(subject, rateLimit.expression(), method, args);

            boolean allowed;
            try {
                allowed = rateLimiter.tryAcquire(key, spec);
            } catch (RateLimiterUnavailableException e) {
                // fail-closed：宁可拒绝，也不在失去保护的情况下放行
                log.error("[ratelimit] 组件不可用，拒绝请求 key={}", key, e);
                throw new ServiceException(SystemErrorEnum.RATE_LIMIT_UNAVAILABLE);
            }

            if (!allowed) {
                log.warn("[ratelimit] 触发限流 key={} 规则={} 维度={} 接口={}.{}",
                        key, spec.fingerprint(), subject,
                        method.getDeclaringClass().getSimpleName(), method.getName());
                throw new ServiceException(SystemErrorEnum.TOO_MANY_REQUESTS);
            }
        }

        return joinPoint.proceed();
    }

    private void validate(RateLimit rateLimit) {
        if (rateLimit.permits() <= 0) {
            throw new IllegalArgumentException("@RateLimit 配置错误：permits 必须大于 0");
        }
        if (rateLimit.windowSeconds() <= 0) {
            throw new IllegalArgumentException("@RateLimit 配置错误：windowSeconds 必须大于 0");
        }
    }
}
