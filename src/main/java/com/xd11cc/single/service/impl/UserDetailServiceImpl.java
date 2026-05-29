package com.xd11cc.single.service.impl;

import com.xd11cc.single.config.RedisCache;
import com.xd11cc.single.constants.CacheConstants;
import com.xd11cc.single.constants.UserConstants;
import com.xd11cc.single.entity.domain.SystemUserDO;
import com.xd11cc.single.entity.dto.LoginUserDTO;
import com.xd11cc.single.enums.SystemErrorEnum;
import com.xd11cc.single.enums.SystemStatusEnum;
import com.xd11cc.single.config.exception.ServiceException;
import com.xd11cc.single.service.ISystemMenuService;
import com.xd11cc.single.service.ISystemUserService;
import com.xd11cc.single.service.DataScopeService;
import com.xd11cc.single.utils.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private RedisCache redisCache;
    @Autowired
    private DataScopeService dataScopeService;
    
    private static String getPasswordErrorCountKey(Long userId){
        return CacheConstants.PASSWORD_ERROR_COUNT_KEY + userId;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SystemUserDO systemUserDO = systemUserService.getByUsername(username);
        // 校验用户信息
        if (systemUserDO == null) {
            throw new ServiceException(SystemErrorEnum.USER_NOT_FOUND);
        }
        if (SystemStatusEnum.FORBIDDEN.getCode().equals(systemUserDO.getStatus())) {
            throw new ServiceException(SystemErrorEnum.USER_FORBIDDEN);
        }
        Long userId = systemUserDO.getId();
        // 判断密码是否正确或超过最大重试次数
        Integer retryCount = redisCache.getCacheObject(getPasswordErrorCountKey(userId));
        if (retryCount == null) {
            retryCount = 0;
        }
        if (retryCount >= UserConstants.PASSWORD_MAX_RETRY_COUNT) {
            throw new ServiceException(SystemErrorEnum.USER_LOCKED, new Object[]{UserConstants.PASSWORD_MAX_RETRY_COUNT});
        }
        // 获取上下文中用户输入的密码凭证
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String password = authentication.getCredentials().toString();
        if (!SecurityUtils.matchPassword(password, systemUserDO.getPassword())) {
            retryCount += 1;
            redisCache.setCacheObject(getPasswordErrorCountKey(userId), retryCount);
            throw new ServiceException(SystemErrorEnum.PASSWORD_ERROR);
        }
        if (redisCache.hasKey(getPasswordErrorCountKey(userId))) {
            redisCache.removeCacheObject(getPasswordErrorCountKey(userId));
        }
        // 构建用户信息
        LoginUserDTO loginUserDTO = new LoginUserDTO(systemMenuService.getPermission(systemUserDO.getId()), systemUserDO);
        dataScopeService.resolveDataScope(loginUserDTO);
        return loginUserDTO;
    }
}
