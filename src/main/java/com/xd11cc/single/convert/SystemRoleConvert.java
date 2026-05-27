package com.xd11cc.single.convert;

import com.xd11cc.single.entity.domain.SystemRoleDO;
import com.xd11cc.single.entity.vo.SystemRoleAddVO;
import com.xd11cc.single.entity.vo.SystemRoleUpdateVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * @author xd11cc
 * @date 2026-05-27
 */
@Mapper
public interface SystemRoleConvert {

    SystemRoleConvert INSTANCE = Mappers.getMapper(SystemRoleConvert.class);

    SystemRoleDO addVO2DO(SystemRoleAddVO systemRoleAddVO);

    SystemRoleDO updateVO2DO(SystemRoleUpdateVO systemRoleUpdateVO);
}
