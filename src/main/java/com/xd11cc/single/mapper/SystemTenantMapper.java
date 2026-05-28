package com.xd11cc.single.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xd11cc.single.entity.domain.SystemTenantDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author xd11cc
 * @date 2026-05-28
 */
@Mapper
@InterceptorIgnore(tenantLine = "true")
public interface SystemTenantMapper extends BaseMapper<SystemTenantDO> {
}
