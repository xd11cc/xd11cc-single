package com.xd11cc.single.convert;

import com.xd11cc.single.entity.domain.SystemMenuDO;
import com.xd11cc.single.entity.vo.SystemMenuAddVO;
import com.xd11cc.single.entity.vo.SystemMenuUpdateVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * @Author: xd11cc
 * @Date: 2025/7/10 10:44
 **/
@Mapper
public interface SystemMenuConvert {

    SystemMenuConvert INSTANCE = Mappers.getMapper(SystemMenuConvert.class);

    SystemMenuDO addVO2DO(SystemMenuAddVO systemMenuAddVO);

    SystemMenuDO updateVO2DO(SystemMenuUpdateVO systemMenuUpdateVO);

}
