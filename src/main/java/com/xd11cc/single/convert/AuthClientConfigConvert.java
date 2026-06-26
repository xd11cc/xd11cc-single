package com.xd11cc.single.convert;

import com.xd11cc.single.entity.domain.AuthClientConfigDO;
import com.xd11cc.single.entity.vo.AuthClientConfigAddVO;
import com.xd11cc.single.entity.vo.AuthClientConfigListVO;
import com.xd11cc.single.entity.vo.AuthClientConfigUpdateVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * @author xd11cc
 * @date 2026-05-20
 */
@Mapper
public interface AuthClientConfigConvert {

    AuthClientConfigConvert INSTANCE = Mappers.getMapper(AuthClientConfigConvert.class);

    AuthClientConfigListVO do2listVO(AuthClientConfigDO authClientConfigDO);

    AuthClientConfigDO addVO2DO(AuthClientConfigAddVO authClientConfigAddVO);

    AuthClientConfigDO updateVO2DO(AuthClientConfigUpdateVO authClientConfigUpdateVO);
}
