package com.xd11cc.single.convert;

import com.xd11cc.single.entity.domain.SystemDictDataDO;
import com.xd11cc.single.entity.vo.SystemDictDataAddVO;
import com.xd11cc.single.entity.vo.SystemDictDataUpdateVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * @author xd11cc
 * @date 2026-01-27 16:02:38
 * @description
 */
@Mapper
public interface SystemDictDataConvert {

    SystemDictDataConvert INSTANCE = Mappers.getMapper(SystemDictDataConvert.class);

    SystemDictDataDO addVO2DO(SystemDictDataAddVO systemDictDataAddVO);

    SystemDictDataDO updateVO2DO(SystemDictDataUpdateVO systemDictDataUpdateVO);
}
