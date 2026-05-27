package com.xd11cc.single.convert;

import com.xd11cc.single.entity.domain.SystemDeptDO;
import com.xd11cc.single.entity.vo.SystemDeptAddVO;
import com.xd11cc.single.entity.vo.SystemDeptTreeVO;
import com.xd11cc.single.entity.vo.SystemDeptUpdateVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * @author xd11cc
 * @date 2026-05-26
 */
@Mapper
public interface SystemDeptConvert {

    SystemDeptConvert INSTANCE = Mappers.getMapper(SystemDeptConvert.class);

    SystemDeptDO addVO2DO(SystemDeptAddVO systemDeptAddVO);

    SystemDeptDO updateVO2DO(SystemDeptUpdateVO systemDeptUpdateVO);

    SystemDeptTreeVO do2TreeVO(SystemDeptDO systemDeptDO);
}
