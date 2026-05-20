package com.xd11cc.single.convert;

import com.xd11cc.single.entity.domain.AuthClientConfigDO;
import com.xd11cc.single.entity.vo.AuthClientConfigListVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * @author xd11cc
 * @date 2026-05-20 09:26:45
 * @description
 */
@Mapper
public interface AuthClientConfigConvert {

    AuthClientConfigConvert CONVERT = Mappers.getMapper(AuthClientConfigConvert.class);

    AuthClientConfigListVO do2listVO(AuthClientConfigDO authClientConfigDO);
}
