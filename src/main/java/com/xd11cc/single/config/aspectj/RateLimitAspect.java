package com.xd11cc.single.config.aspectj;

import com.xd11cc.single.config.annotation.RateLimit;
import com.xd11cc.single.constants.CacheConstants;
import com.xd11cc.single.config.exception.RateLimitException;
import com.xd11cc.single.utils.IpUtils;
import com.xd11cc.single.utils.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

@Aspect
@Component
@Slf4j
public class RateLimitAspect {

    @Autowired
    private RedissonClient redissonClient;

    @Pointcut("@annotation(com.xd11cc.single.config.annotation.RateLimit)")
    public void pointCut() {}

    @Around("pointCut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RateLimit rateLimit = method.getAnnotation(RateLimit.class);
        if (rateLimit == null) {
            return joinPoint.proceed();
        }

        // 1、校验参数的合法性
        validateRateLimitParams(rateLimit);

        // 2、构建Key时传入参数
        String rateLimitKey = buildRateLimitKey(method, rateLimit);

        // 3、获取或创建限流器
        RRateLimiter rateLimiter = getLimiter(rateLimitKey, rateLimit);
        rateLimiter.trySetRate(
                RateType.OVERALL,
                rateLimit.count(),
                rateLimit.time(),
                converToRateIntervalUnit(rateLimit.timeUnit())
        );

        // 4. 尝试获取令牌，显式指定获取1个，语义更清晰
        if (rateLimiter.tryAcquire(1)) {
            log.debug("限流放行 -> key:{}，接口:{}.{}", rateLimitKey, method.getDeclaringClass().getName(), method.getName());
            return joinPoint.proceed();
        } else {
            log.info("触发限流 -> key:{}, 限流规则:{}次/{}秒, 接口:{}.{}",
                    rateLimitKey, rateLimit.count(), rateLimit.time(),
                    method.getDeclaringClass().getName(), method.getName());
            throw new RateLimitException(rateLimit.message());
        }
    }

    private RRateLimiter getLimiter(String key, RateLimit rateLimit) {
        // 直接从Redisson获取，依赖Redisson的本地缓存
        RRateLimiter rateLimiter = redissonClient.getRateLimiter(key);

        // 原子性地设置速率（Redisson保证线程安全）
        if (rateLimiter.trySetRate(
                RateType.OVERALL,
                rateLimit.count(),
                rateLimit.time(),
                converToRateIntervalUnit(rateLimit.timeUnit())
        )) {
            log.debug("Rate limiter initialized: {}", key);
        }

        return rateLimiter;
    }

    private String buildRateLimitKey(Method method, RateLimit rateLimit) {
        StringBuilder sb = new StringBuilder(CacheConstants.RATE_LIMIT_KEY);
        sb.append(rateLimit.key());
        switch (rateLimit.type()) {
            case IP:
                sb.append("ip:").append(IpUtils.getIpAddr());
                break;
            case USER:
                Long userId = SecurityUtils.getUserId();
                sb.append("user:").append(userId != null ? userId : "anonymous");
                break;
            default:
                sb.append("method:")
                        .append(method.getDeclaringClass().getSimpleName())
                        .append(".")
                        .append(method.getName());
                break;
        }
        return sb.toString();
    }

    /**
     * 构建限制器单位
     * @param timeUnit
     * @return
     */
    private RateIntervalUnit converToRateIntervalUnit(TimeUnit timeUnit) {
        switch (timeUnit) {
            case MINUTES: return RateIntervalUnit.MINUTES;
            case HOURS: return RateIntervalUnit.HOURS;
            case DAYS: return RateIntervalUnit.DAYS;
            default: return RateIntervalUnit.SECONDS;
        }
    }

    /**
     * 校验限流注解参数合法性，启动时暴露配置错误
     */
    private void validateRateLimitParams(RateLimit rateLimit) {
        if (rateLimit.count() <= 0) {
            throw new IllegalArgumentException("限流配置错误：count必须大于0");
        }
        if (rateLimit.time() <= 0) {
            throw new IllegalArgumentException("限流配置错误：time必须大于0");
        }
    }
}
