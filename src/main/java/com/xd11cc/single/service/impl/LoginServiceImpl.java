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
import com.xd11cc.single.enums.OperateStatusEnum;
import com.xd11cc.single.enums.LoginTypeEnum;
import com.xd11cc.single.enums.LoginWayEnum;
import com.xd11cc.single.enums.SystemErrorEnum;
import com.xd11cc.single.config.exception.ServiceException;
import com.xd11cc.single.service.*;
import cn.hutool.core.util.IdUtil;
import com.xd11cc.single.entity.domain.SystemUserRoleDO;
import com.xd11cc.single.entity.domain.SystemRoleDO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

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
    @Autowired
    private ISystemLoginLogService systemLoginLogService;
    @Autowired
    private ISystemUserRoleService systemUserRoleService;
    @Autowired
    private ISystemRoleService systemRoleService;

    private String getCaptchaKey(String uuid){
        return CacheConstants.CAPTCHA_KEY + uuid;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String loginByPassword(LoginPasswordVO loginPasswordVO) {
        checkCaptcha(loginPasswordVO.getCaptchaId(), loginPasswordVO.getCaptcha());
        if (LoginWayEnum.PC.getCode() != loginPasswordVO.getWay()) {
            throw new ServiceException(SystemErrorEnum.ILLEGAL_VISIT);
        }
        if (loginPasswordVO.getPassword().length() > UserConstants.USER_PASSWORD_MAX_LENGTH ||
                loginPasswordVO.getPassword().length() < UserConstants.USER_PASSWORD_MIN_LENGTH) {
            throw new ServiceException(SystemErrorEnum.PASSWORD_ERROR);
        }
        Authentication authentication;
        try {
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(loginPasswordVO.getUsername(), loginPasswordVO.getPassword());
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            authentication = authenticationManager.authenticate(authenticationToken);
        } catch (Exception e) {
            systemLoginLogService.recordLoginLog(loginPasswordVO.getUsername(), LoginTypeEnum.PASSWORD, OperateStatusEnum.FAIL, e.getMessage());
            throw e;
        } finally {
            SecurityContextHolder.clearContext();
        }
        LoginUserDTO loginUserDTO = (LoginUserDTO) authentication.getPrincipal();
        String token = tokenService.createToken(loginUserDTO);
        systemLoginLogService.recordLoginLog(loginPasswordVO.getUsername(), LoginTypeEnum.PASSWORD, OperateStatusEnum.SUCCESS, "登录成功");
        return token;
    }

    private void checkCaptcha(String captchaId, String captcha) {
        String captchaValue = redisCache.getCacheObject(getCaptchaKey(captchaId));
        if (StringUtils.isEmpty(captchaValue)) {
            throw new ServiceException(SystemErrorEnum.CAPTCHA_EXPIRE);
        }
        if (!captcha.equals(captchaValue)) {
            throw new ServiceException(SystemErrorEnum.CAPTCHA_ERROR);
        }
        redisCache.removeCacheObject(getCaptchaKey(captchaId));
    }

    @Override
    public UserLoginInfoVO getUserLoginInfo(Long userId) {
        SystemUserDO systemUserDO = systemUserService.getById(userId);
        UserLoginInfoVO userLoginInfoVO = SystemUserConvert.INSTANCE.do2vo(systemUserDO);
        List<SystemUserRoleDO> userRoles = systemUserRoleService.list(new LambdaQueryWrapper<SystemUserRoleDO>()
                .eq(SystemUserRoleDO::getUserId, userId));
        if (!userRoles.isEmpty()) {
            Set<Long> roleIds = userRoles.stream()
                    .map(SystemUserRoleDO::getRoleId)
                    .collect(Collectors.toSet());
            userLoginInfoVO.setRoleIds(roleIds);
            List<SystemRoleDO> roles = systemRoleService.listByIds(roleIds);
            userLoginInfoVO.setRoles(roles.stream()
                    .map(SystemRoleDO::getRoleCode)
                    .collect(Collectors.toSet()));
            userLoginInfoVO.setRoleNames(roles.stream()
                    .map(SystemRoleDO::getRoleName)
                    .collect(Collectors.toSet()));
        }
        userLoginInfoVO.setPermissions(systemMenuService.getPermission(systemUserDO.getId()));
        return userLoginInfoVO;
    }

    @Override
    public CaptchaVO getCaptcha() {
        CaptchaVO captchaVO = new CaptchaVO();
        GifCaptcha captcha = CaptchaUtil.createGifCaptcha(120, 40);
        String uuid = IdUtil.fastUUID();
        redisCache.setCacheObject(getCaptchaKey(uuid), captcha.getCode(), 1, TimeUnit.MINUTES);
        captchaVO.setCaptchaId(uuid);
        captchaVO.setImage(captcha.getImageBase64());
        return captchaVO;
    }
}
