package com.xd11cc.single.service.impl;

import com.xd11cc.single.config.context.SecurityContextHolder;
import com.xd11cc.single.constants.UserConstants;
import com.xd11cc.single.entity.dto.LoginUserDTO;
import com.xd11cc.single.entity.vo.LoginPasswordVO;
import com.xd11cc.single.entity.vo.LoginResultVO;
import com.xd11cc.single.enums.LoginWayEnum;
import com.xd11cc.single.enums.SingleErrorEnum;
import com.xd11cc.single.exception.ServiceException;
import com.xd11cc.single.service.ISystemUserService;
import com.xd11cc.single.service.LoginService;
import com.xd11cc.single.service.TokenService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

/**
 * @Author: xd11cc
 * @Date: 2025/6/23 21:55
 **/
@Slf4j
@Service
public class LoginServiceImpl implements LoginService {

    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private TokenService tokenService;
    @Autowired
    private ISystemUserService systemUserService;

    @Override
    public LoginResultVO loginByPassword(LoginPasswordVO loginPasswordVO) {
        // 1、校验验证码 todo
//        checkCaptcha();
        // 2、校验用户信息
        // 校验是否非法登录获取token
        if (LoginWayEnum.PC.getCode() != loginPasswordVO.getWay()) {
            throw new ServiceException(SingleErrorEnum.ILLEGAL_VISIT);
        }
        // 校验密码规则是否正确
        if (loginPasswordVO.getPassword().length() > UserConstants.USER_PASSWORD_MAX_LENGTH ||
                loginPasswordVO.getPassword().length() < UserConstants.USER_PASSWORD_MIN_LENGTH) {
            throw new ServiceException(SingleErrorEnum.PASSWORD_ERROR);
        }
        // 3、判断当前用户是否已登陆
        checkLoginStatus(loginPasswordVO);
        Authentication authentication;
        try {
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(loginPasswordVO.getUsername(), loginPasswordVO.getPassword());
            // 设置用户上下文
            SecurityContextHolder.setContext(authenticationToken);
            // 该方法会去调用UserDetailsServiceImpl.loadUserByUsername
            authentication = authenticationManager.authenticate(authenticationToken);
        } finally {
            SecurityContextHolder.clearContext();
        }
        // 4、创建token信息
        LoginUserDTO loginUserDTO = (LoginUserDTO) authentication.getPrincipal();
        String accessToken = tokenService.createToken(loginUserDTO);
        return LoginResultVO.builder()
                .userId(loginUserDTO.getUserId())
                .accessToken(accessToken).build();
    }

    /**
     * 检查登录状态
     * @param loginPasswordVO
     */
    private void checkLoginStatus(LoginPasswordVO loginPasswordVO) {

    }
}
