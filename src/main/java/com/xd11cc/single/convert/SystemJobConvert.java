package com.xd11cc.single.convert;

import com.xd11cc.single.entity.domain.SystemJobDO;
import com.xd11cc.single.entity.vo.SystemJobAddVO;
import com.xd11cc.single.entity.vo.SystemJobUpdateVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * @author xd11cc
 * @date 2026-06-11
 */
@Mapper
public interface SystemJobConvert {

    SystemJobConvert INSTANCE = Mappers.getMapper(SystemJobConvert.class);

    SystemJobDO addVO2DO(SystemJobAddVO addVO);

    SystemJobDO updateVO2DO(SystemJobUpdateVO updateVO);
}
