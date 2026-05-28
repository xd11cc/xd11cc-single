package com.xd11cc.single.convert;

import com.xd11cc.single.entity.domain.SystemTenantDO;
import com.xd11cc.single.entity.vo.SystemTenantAddVO;
import com.xd11cc.single.entity.vo.SystemTenantUpdateVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * @author xd11cc
 * @date 2026-05-28
 */
@Mapper
public interface SystemTenantConvert {

    SystemTenantConvert INSTANCE = Mappers.getMapper(SystemTenantConvert.class);

    SystemTenantDO addVO2DO(SystemTenantAddVO systemTenantAddVO);

    SystemTenantDO updateVO2DO(SystemTenantUpdateVO systemTenantUpdateVO);
}
