package com.xd11cc.single.config.aspectj;

import com.xd11cc.single.config.annotation.Lock;
import com.xd11cc.single.constants.CacheConstants;
import com.xd11cc.single.enums.SystemErrorEnum;
import com.xd11cc.single.config.exception.ServiceException;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.annotation.Order;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * @author xd11cc
 * @date 2026-05-30
 */
@Aspect
@Component
@Slf4j
@Order(Ordered.LOWEST_PRECEDENCE - 2)
public class LockAspect {

    private static final ExpressionParser PARSER = new SpelExpressionParser();
    private static final ParameterNameDiscoverer NAME_DISCOVERER = new DefaultParameterNameDiscoverer();

    @Autowired
    private RedissonClient redissonClient;

    @org.aspectj.lang.annotation.Pointcut("@annotation(com.xd11cc.single.config.annotation.Lock)")
    public void pointCut() {}

    @Around("pointCut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Lock lock = method.getAnnotation(Lock.class);
        if (lock == null) {
            return joinPoint.proceed();
        }

        validateLockParams(lock);

        // 构建锁 key
        String lockKey = buildLockKey(method, lock, joinPoint);
        RLock rLock = redissonClient.getLock(lockKey);

        try {
            boolean acquired;
            long waitTime = lock.waitTime();
            long leaseTime = lock.leaseTime();
            TimeUnit unit = lock.unit();

            if (waitTime > 0 && leaseTime > 0) {
                // 等待时间 + 自动释放时间都设置
                acquired = rLock.tryLock(waitTime, leaseTime, unit);
            } else if (waitTime > 0) {
                // 只设置等待时间，不自动释放（靠finally手动unlock）
                acquired = rLock.tryLock(waitTime, TimeUnit.SECONDS);
            } else if (leaseTime > 0) {
                // 不等待，直接尝试获取并设置自动释放时间
                acquired = rLock.tryLock(leaseTime, unit);
            } else {
                // 不等待、不自动释放，立即尝试
                acquired = rLock.tryLock();
            }

            if (!acquired) {
                log.info("分布式锁获取失败 -> key:{}, 接口:{}.{}",
                        lockKey, method.getDeclaringClass().getName(), method.getName());
                throw new ServiceException(SystemErrorEnum.LOCK_ACQUIRE_FAILED);
            }

            log.debug("分布式锁获取成功 -> key:{}, 接口:{}.{}",
                    lockKey, method.getDeclaringClass().getName(), method.getName());
            return joinPoint.proceed();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ServiceException(SystemErrorEnum.LOCK_ACQUIRE_FAILED);
        } finally {
            // 防止误解锁：只在当前线程持有锁时才释放
            if (rLock.isHeldByCurrentThread()) {
                rLock.unlock();
                log.debug("分布式锁释放成功 -> key:{}, 接口:{}.{}",
                        lockKey, method.getDeclaringClass().getName(), method.getName());
            }
        }
    }

    /**
     * 构建锁 key
     * 格式：redlock:{prefix}:{className.methodName}:{keyValue}
     * key 解析优先级：SpEL 表达式 > 全部参数拼接
     */
    private String buildLockKey(Method method, Lock lock, ProceedingJoinPoint joinPoint) {
        StringBuilder sb = new StringBuilder(CacheConstants.REDLOCK_KEY_PREFIX)
                .append(lock.prefix()).append(":");

        sb.append(method.getDeclaringClass().getSimpleName()).append(".")
                .append(method.getName()).append(":");

        if (lock.key().isEmpty()) {
            // 未设置 SpEL：拼接所有入参
            Object[] args = joinPoint.getArgs();
            for (Object arg : args) {
                sb.append(arg).append("|");
            }
        } else {
            // 使用 SpEL 表达式解析指定入参
            Object keyValue = parseSpEL(method, joinPoint.getArgs(), lock.key());
            sb.append(keyValue);
        }

        return sb.toString();
    }

    /**
     * 解析 SpEL 表达式，从方法入参中提取锁维度值
     * 示例：
     *   parseSpEL(method, args, "#userId")        → 提取 userId 参数值
     *   parseSpEL(method, args, "#vo.id")          → 提取 vo 对象的 id 字段
     *   parseSpEL(method, args, "#userId + ':' + #orderId") → 组合多个参数
     */
    private Object parseSpEL(Method method, Object[] args, String expression) {
        EvaluationContext context = new MethodBasedEvaluationContext(
                new Object(), method, args, NAME_DISCOVERER);
        return PARSER.parseExpression(expression).getValue(context);
    }

    private void validateLockParams(Lock lock) {
        if (lock.waitTime() < 0 || lock.leaseTime() < 0) {
            throw new IllegalArgumentException("分布式锁配置错误：waitTime 和 leaseTime 不能小于 0");
        }
    }
}
