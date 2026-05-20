package com.xd11cc.single.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xd11cc.single.convert.SystemUserConvert;
import com.xd11cc.single.entity.domain.SystemUserDO;
import com.xd11cc.single.entity.vo.UserLoginInfoVO;
import com.xd11cc.single.mapper.SystemUserMapper;
import com.xd11cc.single.service.ISystemUserRoleService;
import com.xd11cc.single.service.ISystemUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Author: xd11cc
 * @Date: 2025/6/14 00:27
 **/
@Slf4j
@Service
public class SystemUserServiceImpl extends ServiceImpl<SystemUserMapper, SystemUserDO> implements ISystemUserService {

    @Autowired
    private ISystemUserRoleService systemUserRoleService;

    @Override
    public SystemUserDO getByUsername(String username) {
        return baseMapper.selectOne(new LambdaQueryWrapper<SystemUserDO>()
                .eq(SystemUserDO::getUsername, username));
    }

    @Override
    public SystemUserDO getByEmail(String email) {
        return baseMapper.selectOne(new LambdaQueryWrapper<SystemUserDO>()
                .eq(SystemUserDO::getEmail, email));
    }

}
