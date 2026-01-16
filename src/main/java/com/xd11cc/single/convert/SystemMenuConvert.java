package com.xd11cc.single.convert;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * @Author: xd11cc
 * @Date: 2025/7/10 10:44
 **/
@Mapper
public interface SystemMenuConvert {

    SystemMenuConvert INSTANCE = Mappers.getMapper(SystemMenuConvert.class);

}
