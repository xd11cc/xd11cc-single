package com.xd11cc.single.entity.base;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author xd11cc
 * @date 2026-01-27 16:11:08
 * @description
 */
@EqualsAndHashCode(callSuper = true)
@Data
public abstract class BaseTenantDO extends BaseDO {

    /**
     * 多租户id
     */
    private Long tenantId;
}
