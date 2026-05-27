package com.xd11cc.single.convert;

import com.xd11cc.single.entity.domain.SystemPostDO;
import com.xd11cc.single.entity.vo.SystemPostAddVO;
import com.xd11cc.single.entity.vo.SystemPostUpdateVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * @author xd11cc
 * @date 2026-05-27
 */
@Mapper
public interface SystemPostConvert {

    SystemPostConvert INSTANCE = Mappers.getMapper(SystemPostConvert.class);

    SystemPostDO addVO2DO(SystemPostAddVO systemPostAddVO);

    SystemPostDO updateVO2DO(SystemPostUpdateVO systemPostUpdateVO);
}
