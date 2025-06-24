package com.xd11cc.single.service.impl;

import com.xd11cc.single.config.context.SecurityContextHolder;
import com.xd11cc.single.entity.domain.SystemUserDO;
import com.xd11cc.single.entity.dto.LoginUserDTO;
import com.xd11cc.single.enums.SingleErrorEnum;
import com.xd11cc.single.exception.ServiceException;
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
            throw new ServiceException(SingleErrorEnum.USER_NOT_FOUND);
        }
        // 设置用户信息
        systemUserService.validateUser(systemUserDO);
        // 构建用户信息
        LoginUserDTO loginUserDTO = new LoginUserDTO(systemMenuService.getPermissionMenu(systemUserDO), systemUserDO);
        // 设置用户上下文
        SecurityContextHolder.setContext(loginUserDTO);
        return loginUserDTO;
    }
}
