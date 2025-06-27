package com.xd11cc.single.aspectj;

import com.xd11cc.single.annotation.RateLimit;
import com.xd11cc.single.config.RedisCache;
import com.xd11cc.single.exception.RateLimitException;
import com.xd11cc.single.utils.IpUtils;
import com.xd11cc.single.utils.SecurityUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * @Author: xd11cc
 * @Date: 2025/6/27 21:36
 **/
@Aspect
@Component
public class RateLimitAspect {
    
    @Autowired
    private RedisCache redisCache;

    @Pointcut("@annotation(com.xd11cc.single.annotation.RateLimit)")
    public void pointCut() {

    }

    @Around("pointCut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        RateLimit rateLimit = AnnotationUtils.findAnnotation(signature.getMethod(), RateLimit.class);
        String rateLimitKey = buildRateLimitKey(signature.getMethod(), rateLimit);
        // 获取限流器
        RRateLimiter rateLimiter = redisCache.getRedissonClient().getRateLimiter(rateLimitKey);
        // 设置限流规则
        if (!rateLimiter.isExists()){
            rateLimiter.trySetRate(
                    RateType.OVERALL,
                    rateLimit.count(),
                    rateLimit.time(),
                    converToRateIntervalUnit(rateLimit.timeUnit())
            );
        }

        if (rateLimiter.tryAcquire()){
            return joinPoint.proceed();
        }else {
            throw new RateLimitException(rateLimit.message());
        }
    }

    private String buildRateLimitKey(Method method, RateLimit rateLimit) {
        StringBuilder sb = new StringBuilder();
        switch (rateLimit.type()){
            case IP:
                sb.append(IpUtils.getIpAddr());
                break;
            case USER:
                sb.append(SecurityUtils.getUserId());
                break;
            default:
                sb.append(method.getDeclaringClass().getName())
                    .append(".").append(method.getName());
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
}
