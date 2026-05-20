package com.xd11cc.single.convert;

import com.xd11cc.single.entity.domain.SystemConfigDO;
import com.xd11cc.single.entity.vo.SystemConfigAddVO;
import com.xd11cc.single.entity.vo.SystemConfigUpdateVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * @author xd11cc
 * @date 2026-05-20
 */
@Mapper
public interface SystemConfigConvert {

    SystemConfigConvert INSTANCE = Mappers.getMapper(SystemConfigConvert.class);

    SystemConfigDO addVO2DO(SystemConfigAddVO systemConfigAddVO);

    SystemConfigDO updateVO2DO(SystemConfigUpdateVO systemConfigUpdateVO);
}
