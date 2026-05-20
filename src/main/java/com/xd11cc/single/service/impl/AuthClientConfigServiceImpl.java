package com.xd11cc.single.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xd11cc.single.entity.domain.AuthClientConfigDO;
import com.xd11cc.single.service.IAuthClientConfigService;
import com.xd11cc.single.mapper.AuthClientConfigMapper;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

/**
* @author xd11cc
* @date 2026-05-20
*/
@Service
public class AuthClientConfigServiceImpl extends ServiceImpl<AuthClientConfigMapper, AuthClientConfigDO> implements IAuthClientConfigService {

    @Override
    public AuthClientConfigDO getBySource(String source) {
        LambdaQueryWrapper<AuthClientConfigDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AuthClientConfigDO::getSource, source);
        return baseMapper.selectOne(wrapper);
    }
}