package com.xd11cc.single.convert;

import com.xd11cc.single.entity.domain.SystemDictTypeDO;
import com.xd11cc.single.entity.vo.SystemDictTypeAddVO;
import com.xd11cc.single.entity.vo.SystemDictTypeUpdateVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * @author xd11cc
 * @date 2026-01-27 16:02:38
 * @description
 */
@Mapper
public interface SystemDictTypeConvert {

    SystemDictTypeConvert INSTANCE = Mappers.getMapper(SystemDictTypeConvert.class);

    SystemDictTypeDO addVO2DO(SystemDictTypeAddVO systemDictTypeAddVO);

    SystemDictTypeDO updateVO2DO(SystemDictTypeUpdateVO systemDictTypeUpdateVO);
}
