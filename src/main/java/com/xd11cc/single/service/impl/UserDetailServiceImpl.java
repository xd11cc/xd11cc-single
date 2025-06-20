package com.xd11cc.single.service.impl;

import com.xd11cc.single.entity.domain.SystemUserDO;
import com.xd11cc.single.entity.dto.LoginUserDTO;
import com.xd11cc.single.enums.SingleErrorEnum;
import com.xd11cc.single.exception.ServiceExceptions;
import com.xd11cc.single.service.ISystemMenuService;
import com.xd11cc.single.service.ISystemUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * @Author: xd11cc
 * @Date: 2025/6/18 14:59
 **/
@Service
public class UserDetailServiceImpl implements UserDetailsService {

    @Autowired
    private ISystemUserService systemUserService;
    @Autowired
    private ISystemMenuService systemMenuService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SystemUserDO systemUserDO = systemUserService.getByUsername(username);
        if (systemUserDO == null) {
            ServiceExceptions.throwWithErrorCode(SingleErrorEnum.USER_NOT_FOUND);
        }
        return new LoginUserDTO(systemMenuService.getPermissionMenu(systemUserDO), systemUserDO);
    }
}
