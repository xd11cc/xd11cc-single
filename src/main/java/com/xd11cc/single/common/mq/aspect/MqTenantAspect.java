package com.xd11cc.single.common.mq.aspect;

import com.xd11cc.single.common.context.TenantContextHolder;
import com.xd11cc.single.common.mq.MqDelivery;
import com.xd11cc.single.common.mq.MqDeliveryContext;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * MQ 消费端租户上下文切面。
 * <p>
 * 拦截 {@code @RabbitHandler} 方法，在业务逻辑执行前按投递上下文中的 tenantId
 * 设置 {@link TenantContextHolder}，执行结束后恢复。
 * </p>
 *
 * <p>
 * <b>为什么必须在方法进入前设置</b>：业务方法在 {@code consume(...)} 之外
 * 可能已有数据库操作（查配置、写日志等），而租户拦截器
 * 通过 {@code getRequiredTenantId()} 取租户，为空直接抛 {@code NOT_FOUND_TENANT}。
 * 因此不能只在 {@code MqConsumeTemplate} 内部设置。
 * </p>
 *
 * <p>
 * 本切面只负责「设置 / 恢复上下文」；租户<b>校验</b>、ACK/NACK、重试、落表
 * 统一由 {@code MqConsumeTemplate} 处理，避免职责重叠。
 * </p>
 *
 * @author xd11cc
 */
@Slf4j
@Aspect
@Component
public class MqTenantAspect {

    @Around("@annotation(org.springframework.amqp.rabbit.annotation.RabbitHandler)")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        // 租户来源唯一：监听适配器写入的投递上下文
        MqDelivery delivery = MqDeliveryContext.current();
        Long tenantId = delivery == null ? null : delivery.getTenantId();
        if (tenantId == null) {
            // 拿不到租户时不设置上下文，交由 MqConsumeTemplate 判为前置失败并落表
            return pjp.proceed();
        }

        Long previous = TenantContextHolder.getTenantId();
        TenantContextHolder.setTenantId(tenantId);
        try {
            return pjp.proceed();
        } finally {
            // 恢复而非直接 clear，兼容嵌套场景
            if (previous == null) {
                TenantContextHolder.clear();
            } else {
                TenantContextHolder.setTenantId(previous);
            }
        }
    }
}
