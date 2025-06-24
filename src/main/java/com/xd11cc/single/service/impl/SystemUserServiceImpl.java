package com.xd11cc.single.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xd11cc.single.entity.domain.SystemUserDO;
import com.xd11cc.single.mapper.SystemUserMapper;
import com.xd11cc.single.service.ISystemUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @Author: xd11cc
 * @Date: 2025/6/14 00:27
 **/
@Slf4j
@Service
public class SystemUserServiceImpl extends ServiceImpl<SystemUserMapper, SystemUserDO> implements ISystemUserService {

    @Override
    public SystemUserDO getByUsername(String username) {
        return baseMapper.selectOne(new LambdaQueryWrapper<SystemUserDO>()
                .eq(SystemUserDO::getUsername, username));
    }

}
