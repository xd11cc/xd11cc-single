package com.xd11cc.single.service.impl;

import com.xd11cc.single.entity.domain.SystemUserDO;
import com.xd11cc.single.entity.dto.LoginUserDTO;
import com.xd11cc.single.enums.SystemErrorEnum;
import com.xd11cc.single.common.exception.ServiceException;
import com.xd11cc.single.service.ISystemMenuService;
import com.xd11cc.single.service.ISystemUserService;
import com.xd11cc.single.service.DataScopeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


/**
 * @Author: xd11cc
 * @Date: 2025/6/18 14:59
 **/
@Slf4j
@Service
public class UserDetailServiceImpl implements UserDetailsService {

    @Autowired
    private ISystemUserService systemUserService;
    @Autowired
    private ISystemMenuService systemMenuService;
    @Autowired
    private DataScopeService dataScopeService;
    

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SystemUserDO systemUserDO = systemUserService.getByUsername(username);
        // 用户不存在与密码错误返回同一个错误码，避免暴露用户名是否存在。
        // 账户禁用不在这一层判断：它在密码校验通过之后才检查（见 PasswordAuthenticationProvider），
        // 否则任何人不带正确密码就能探测出「该账号存在但被禁用」。
        if (systemUserDO == null) {
            throw new ServiceException(SystemErrorEnum.LOGIN_FAILED);
        }
        // 构建用户信息；密码校验、错误计数与状态校验由 PasswordAuthenticationProvider 负责
        LoginUserDTO loginUserDTO = new LoginUserDTO(systemMenuService.getPermission(systemUserDO.getId()), systemUserDO);
        dataScopeService.resolveDataScope(loginUserDTO);
        return loginUserDTO;
    }
}
