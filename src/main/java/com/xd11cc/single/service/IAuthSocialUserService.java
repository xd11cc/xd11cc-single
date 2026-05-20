package com.xd11cc.single.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xd11cc.single.entity.domain.AuthSocialUserDO;


/**
* @author xd11cc
* @date 2026-05-09
*/
public interface IAuthSocialUserService extends IService<AuthSocialUserDO>{

    AuthSocialUserDO getBySourceAndUuid(String sourceType, String uuid);
}