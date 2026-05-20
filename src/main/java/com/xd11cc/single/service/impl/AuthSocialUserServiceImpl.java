package com.xd11cc.single.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xd11cc.single.entity.domain.AuthSocialUserDO;
import com.xd11cc.single.service.IAuthSocialUserService;
import com.xd11cc.single.mapper.AuthSocialUserMapper;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;


/**
* @author xd11cc
* @date 2026-05-09
*/
@Service
public class AuthSocialUserServiceImpl extends ServiceImpl<AuthSocialUserMapper, AuthSocialUserDO> implements IAuthSocialUserService {

    @Override
    public AuthSocialUserDO getBySourceAndUuid(String sourceType, String uuid) {
        LambdaQueryWrapper<AuthSocialUserDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AuthSocialUserDO::getSource, sourceType)
                .eq(AuthSocialUserDO::getUuid, uuid);
        return baseMapper.selectOne(wrapper);
    }
}