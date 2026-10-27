package com.xd11cc.single.common.mq;

import com.xd11cc.single.common.context.TenantContextHolder;
import com.xd11cc.single.common.exception.ServiceException;
import com.xd11cc.single.common.mq.message.MqMessage;
import com.xd11cc.single.enums.SystemErrorEnum;
import com.xd11cc.single.infrastructure.redis.RedisCache;
import com.xd11cc.single.constants.CacheConstants;
import com.xd11cc.single.entity.dto.TenantDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 消费前租户校验。
 * <p>
 * 基于 {@code TenantCacheInitializer} 预热的租户缓存（domain → TenantDTO）做存在性校验，
 * 避免为不存在的租户执行业务逻辑。
 * </p>
 *
 * <p>
 * 校验失败抛 {@link ServiceException}，由 {@link MqConsumeTemplate} 捕获后落表并 NACK。
 * </p>
 *
 * @author xd11cc
 */
@Slf4j
@Component
public class MqTenantValidator {

    @Autowired
    private RedisCache redisCache;

    /**
     * 校验租户存在，返回合法租户 ID。
     *
     * @param tenantId 消息中的租户 ID
     * @return 合法租户 ID
     * @throws ServiceException 租户为空或不存在时抛出
     */
    public Long requireValidTenant(Long tenantId) {
        if (tenantId == null) {
            throw new ServiceException(SystemErrorEnum.MQ_TENANT_ID_EMPTY);
        }

        Map<String, TenantDTO> tenantMap = TenantContextHolder.runWithoutTenantAwareness(
                () -> redisCache.getCacheMap(CacheConstants.TENANT_DOMAIN_KEY));

        if (tenantMap == null || tenantMap.isEmpty()) {
            log.warn("[MQ] 租户缓存为空，跳过租户校验 tenantId={}", tenantId);
            return tenantId;
        }

        boolean exists = tenantMap.values().stream()
                .anyMatch(t -> tenantId.equals(t.getId()));
        if (!exists) {
            throw new ServiceException(SystemErrorEnum.MQ_TENANT_NOT_EXISTS.getErrorCode(),
                    "MQ消费失败：租户不存在或已停用，tenantId=" + tenantId);
        }
        return tenantId;
    }

    /**
     * 从消费方法的业务对象参数中解析租户 ID。
     * <p>
     * 优先取 {@link MqMessage#getTenantId()}；消息体没有则退化为消息头 / 当前上下文。
     * </p>
     *
     * @param body    业务对象（可能为 null）
     * @param delivery 投递上下文
     */
    public Long resolveTenantId(Object body, MqDelivery delivery) {
        if (body instanceof MqMessage) {
            Long tenantId = ((MqMessage) body).getTenantId();
            if (tenantId != null) {
                return tenantId;
            }
        }
        return delivery.getTenantId();
    }
}
