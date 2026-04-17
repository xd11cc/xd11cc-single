package com.xd11cc.single.service.impl;

import cn.hutool.captcha.*;
import com.xd11cc.single.config.RedisCache;
import com.xd11cc.single.constants.CacheConstants;
import com.xd11cc.single.constants.UserConstants;
import com.xd11cc.single.convert.SystemUserConvert;
import com.xd11cc.single.entity.domain.SystemUserDO;
import com.xd11cc.single.entity.dto.LoginUserDTO;
import com.xd11cc.single.entity.vo.CaptchaVO;
import com.xd11cc.single.entity.vo.LoginPasswordVO;
import com.xd11cc.single.entity.vo.UserLoginInfoVO;
import com.xd11cc.single.enums.LoginWayEnum;
import com.xd11cc.single.enums.SystemErrorEnum;
import com.xd11cc.single.config.exception.ServiceException;
import com.xd11cc.single.service.ISystemMenuService;
import com.xd11cc.single.service.ISystemUserService;
import com.xd11cc.single.service.LoginService;
import com.xd11cc.single.service.TokenService;
import com.xd11cc.single.utils.IdUtils;
import com.xd11cc.single.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

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
    @Autowired
    private ISystemMenuService systemMenuService;
    @Autowired
    private RedisCache redisCache;


    private String getCaptchaKey(String uuid){
        return CacheConstants.CAPTCHA_KEY + uuid;
    }

    @Override
    @Transactional
    public String loginByPassword(LoginPasswordVO loginPasswordVO) {
        // 1、校验验证码
        checkCaptcha(loginPasswordVO.getCaptchaId(), loginPasswordVO.getCaptcha());
        // 2、校验用户信息
        // 校验是否非法登录获取token
        if (LoginWayEnum.PC.getCode() != loginPasswordVO.getWay()) {
            throw new ServiceException(SystemErrorEnum.ILLEGAL_VISIT);
        }
        // 校验密码规则是否正确
        if (loginPasswordVO.getPassword().length() > UserConstants.USER_PASSWORD_MAX_LENGTH ||
                loginPasswordVO.getPassword().length() < UserConstants.USER_PASSWORD_MIN_LENGTH) {
            throw new ServiceException(SystemErrorEnum.PASSWORD_ERROR);
        }
        // 3、判断当前用户是否已登陆
        checkLoginStatus(loginPasswordVO);
        Authentication authentication;
        try {
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(loginPasswordVO.getUsername(), loginPasswordVO.getPassword());
            // 设置用户上下文
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            // 该方法会去调用UserDetailsServiceImpl.loadUserByUsername
            authentication = authenticationManager.authenticate(authenticationToken);
        } finally {
            SecurityContextHolder.clearContext();
        }
        // 4、创建token信息
        LoginUserDTO loginUserDTO = (LoginUserDTO) authentication.getPrincipal();
        return tokenService.createToken(loginUserDTO);
    }

    /**
     * 校验验证码
     */
    private void checkCaptcha(String captchaId, String captcha) {
        String captchaValue = redisCache.getCacheObject(getCaptchaKey(captchaId));
        if (StringUtils.isEmpty(captchaValue)) {
            throw new ServiceException(SystemErrorEnum.CAPTCHA_EXPIRE);
        }
        if (!captcha.equals(captchaValue)) {
            throw new ServiceException(SystemErrorEnum.CAPTCHA_ERROR);
        }
    }

    @Override
    public UserLoginInfoVO getUserLoginInfo(Long userId) {
        SystemUserDO systemUserDO = systemUserService.getById(userId);
        UserLoginInfoVO userLoginInfoVO = SystemUserConvert.INSTANCE.do2vo(systemUserDO);
        userLoginInfoVO.setPermissions(systemMenuService.getPermission(systemUserDO.getId()));
        return userLoginInfoVO;
    }

    @Override
    public CaptchaVO getCaptcha() {
        CaptchaVO captchaVO = new CaptchaVO();
        GifCaptcha captcha = CaptchaUtil.createGifCaptcha(120, 40);
        String uuid = IdUtils.fastUUID();
        redisCache.setCacheObject(getCaptchaKey(uuid), captcha.getCode(), 1, TimeUnit.MINUTES);
        captchaVO.setCaptchaId(uuid);
        captchaVO.setImage(captcha.getImageBase64());
        return captchaVO;
    }

    /**
     * 检查登录状态 todo
     * @param loginPasswordVO
     */
    private void checkLoginStatus(LoginPasswordVO loginPasswordVO) {
        //
    }
}
