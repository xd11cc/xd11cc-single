package com.xd11cc.single.convert;

import com.xd11cc.single.entity.domain.SystemMenuDO;
import com.xd11cc.single.entity.vo.TreeMenuVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * @Author: xd11cc
 * @Date: 2025/7/10 10:44
 **/
@Mapper
public interface SystemMenuConvert {

    SystemMenuConvert INSTANCE = Mappers.getMapper(SystemMenuConvert.class);

    TreeMenuVO do2TreeVO(SystemMenuDO systemMenuDO);
}
