package com.xd11cc.single.convert;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * @Author: xd11cc
 * @Date: 2025/6/23 17:54
 **/
@Mapper
public interface SystemUserConvert {

    SystemUserConvert INSTANCE = Mappers.getMapper(SystemUserConvert.class);

}
