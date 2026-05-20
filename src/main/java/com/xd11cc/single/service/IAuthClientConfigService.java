package com.xd11cc.single.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xd11cc.single.entity.domain.AuthClientConfigDO;

/**
* @author xd11cc
* @date 2026-05-20
*/
public interface IAuthClientConfigService extends IService<AuthClientConfigDO>{

    AuthClientConfigDO getBySource(String source);
}