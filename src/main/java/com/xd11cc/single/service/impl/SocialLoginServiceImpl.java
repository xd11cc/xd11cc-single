package com.xd11cc.single.service.impl;

import com.alibaba.fastjson2.JSON;
import cn.hutool.core.util.StrUtil;
import com.xd11cc.single.config.RedisCache;
import com.xd11cc.single.config.auth.AuthRequestFactory;
import com.xd11cc.single.constants.CacheConstants;
import com.xd11cc.single.entity.domain.AuthSocialUserDO;
import com.xd11cc.single.entity.domain.SystemUserDO;
import com.xd11cc.single.entity.dto.LoginUserDTO;
import com.xd11cc.single.entity.vo.SocialUserBindVO;
import com.xd11cc.single.enums.OperateStatusEnum;
import com.xd11cc.single.enums.LoginTypeEnum;
import com.xd11cc.single.enums.SystemErrorEnum;
import com.xd11cc.single.config.exception.ServiceException;
import com.xd11cc.single.service.*;
import lombok.extern.slf4j.Slf4j;
import me.zhyd.oauth.model.AuthCallback;
import me.zhyd.oauth.model.AuthResponse;
import me.zhyd.oauth.model.AuthUser;
import me.zhyd.oauth.request.AuthRequest;
import me.zhyd.oauth.utils.AuthStateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @Author: xd11cc
 * @Date: 2025/6/23 21:55
 **/
@Slf4j
@Service
public class SocialLoginServiceImpl implements SocialLoginService {

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
    private AuthRequestFactory authRequestFactory;
    @Autowired
    private IAuthSocialUserService authSocialUserService;
    @Autowired
    private ISystemConfigService systemConfigService;
    @Autowired
    private DataScopeService dataScopeService;
    @Autowired
    private ISystemLoginLogService systemLoginLogService;

    private String getAuthStateKey(String key) {
        return CacheConstants.AUTH_STATE_KEY + key;
    }

    @Override
    public String getRedirectUri(String source) {
        AuthRequest authRequest = authRequestFactory.get(source);
        return authRequest.authorize(AuthStateUtils.createState());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void callback(String source, String code, String state, HttpServletResponse response) {
        AuthCallback authCallback = AuthCallback.builder()
                .state(state)
                .code(code).build();
        AuthResponse<AuthUser> authResponse;
        try {
            AuthRequest authRequest = authRequestFactory.get(source);
            authResponse = authRequest.login(authCallback);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            String errorMsg = StrUtil.blankToDefault(e.getMessage(), "请求第三方授权平台异常");
            log.error("{}授权登录请求异常", source, e);
            throw new ServiceException(SystemErrorEnum.SOCIAL_AUTH_FAILED, new Object[]{errorMsg});
        }
        if (authResponse == null || !authResponse.ok()) {
            String errorMsg = authResponse == null ? "授权平台未返回响应" : authResponse.getMsg();
            log.error("{}授权登录失败，原因:{}", source, errorMsg);
            throw new ServiceException(SystemErrorEnum.SOCIAL_AUTH_FAILED, new Object[]{errorMsg});
        }

        AuthUser authUser = authResponse.getData();
        if (authUser == null || StrUtil.isBlank(authUser.getSource())
                || StrUtil.isBlank(authUser.getUuid()) || authUser.getToken() == null) {
            log.error("{}授权登录失败，授权平台返回的用户信息不完整", source);
            throw new ServiceException(SystemErrorEnum.SOCIAL_AUTH_FAILED,
                    new Object[]{"授权平台返回的用户信息不完整"});
        }
        log.info("authUser:{}", JSON.toJSONString(authUser));

        String sourceType = authUser.getSource().toLowerCase();
        String uuid = authUser.getUuid();

        AuthSocialUserDO authSocialUserDO = authSocialUserService.getBySourceAndUuid(sourceType, uuid);
        if (null != authSocialUserDO) {
            updateSocialToken(authSocialUserDO, authUser);
            SystemUserDO systemUserDO = systemUserService.getById(authSocialUserDO.getUserId());
            if (systemUserDO == null) {
                throw new ServiceException(SystemErrorEnum.USER_NOT_FOUND);
            }
            String successUrl = getRequiredRedirectUrl("auth-redirect-successUrl");
            socialLoginAndRedirect(systemUserDO, successUrl, response);
            return;
        }

        SystemUserDO systemUserDO = StrUtil.isBlank(authUser.getEmail())
                ? null
                : systemUserService.getByEmail(authUser.getEmail());
        if (null != systemUserDO) {
            AuthSocialUserDO socialUser = buildSocialUser(authUser, systemUserDO.getId(), code, state);
            authSocialUserService.save(socialUser);
            String successUrl = getRequiredRedirectUrl("auth-redirect-successUrl");
            socialLoginAndRedirect(systemUserDO, successUrl, response);
            return;
        }

        String bindUserUrl = getRequiredRedirectUrl("auth-redirect-bindUserUrl");
        redisCache.setCacheObject(getAuthStateKey(state), authUser, 500, TimeUnit.MINUTES);
        sendRedirect(response, String.format(bindUserUrl, source, state));
    }

    private void socialLoginAndRedirect(SystemUserDO systemUserDO, String successUrl, HttpServletResponse response) {
        LoginUserDTO loginUser = new LoginUserDTO(systemMenuService.getPermission(systemUserDO.getId()), systemUserDO);
        dataScopeService.resolveDataScope(loginUser);
        String token = tokenService.createToken(loginUser);
        systemLoginLogService.recordLoginLog(systemUserDO.getUsername(), LoginTypeEnum.SOCIAL, OperateStatusEnum.SUCCESS, "登录成功");
        sendRedirect(response, String.format(successUrl, token));
    }

    private String getRequiredRedirectUrl(String configKey) {
        String redirectUrl = systemConfigService.getConfig(configKey);
        if (StrUtil.isBlank(redirectUrl)) {
            log.error("系统配置缺失: {}", configKey);
            throw new ServiceException(SystemErrorEnum.SOCIAL_AUTH_CONFIG_MISSING, new Object[]{configKey});
        }
        return redirectUrl;
    }

    private void sendRedirect(HttpServletResponse response, String redirectUrl) {
        try {
            response.sendRedirect(redirectUrl);
        } catch (IOException e) {
            log.error("社交登录页面跳转失败，地址:{}", redirectUrl, e);
            throw new ServiceException(SystemErrorEnum.SOCIAL_AUTH_REDIRECT_FAILED, e);
        }
    }

    private void updateSocialToken(AuthSocialUserDO socialUserDO, AuthUser authUser) {
        socialUserDO.setToken(authUser.getToken().getAccessToken());
        socialUserDO.setOpenId(authUser.getToken().getOpenId());
        socialUserDO.setRowTokenInfo(JSON.toJSONString(authUser.getToken()));
        authSocialUserService.updateById(socialUserDO);
    }

    private AuthSocialUserDO buildSocialUser(AuthUser authUser, Long userId, String code, String state) {
        AuthSocialUserDO socialUser = new AuthSocialUserDO();
        socialUser.setUuid(authUser.getUuid());
        socialUser.setUserId(userId);
        socialUser.setSource(authUser.getSource().toLowerCase());
        socialUser.setOpenId(authUser.getToken().getOpenId());
        socialUser.setToken(authUser.getToken().getAccessToken());
        socialUser.setRowTokenInfo(JSON.toJSONString(authUser.getToken()));
        socialUser.setNickname(authUser.getNickname());
        socialUser.setAvatar(authUser.getAvatar());
        socialUser.setRowUserInfo(JSON.toJSONString(authUser.getRawUserInfo()));
        socialUser.setCode(code);
        socialUser.setState(state);
        return socialUser;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String socialUserBind(SocialUserBindVO socialUserBindVO) {
        AuthUser authUser = redisCache.getCacheObject(getAuthStateKey(socialUserBindVO.getState()));
        if (authUser == null) {
            throw new ServiceException(SystemErrorEnum.SOCIAL_USER_NOT_FOUND);
        }

        Authentication authentication;
        try {
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(socialUserBindVO.getUsername(), socialUserBindVO.getPassword());
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            authentication = authenticationManager.authenticate(authenticationToken);
        } catch (Exception e) {
            SecurityContextHolder.clearContext();
            throw e;
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);
        try {
            LoginUserDTO loginUserDTO = (LoginUserDTO) authentication.getPrincipal();
            SystemUserDO systemUserDO = loginUserDTO.getSystemUserDO();

            String sourceType = authUser.getSource().toLowerCase();
            String uuid = authUser.getUuid();
            AuthSocialUserDO existSocialUser = authSocialUserService.getBySourceAndUuid(sourceType, uuid);
            if (existSocialUser != null) {
                throw new ServiceException(SystemErrorEnum.SOCIAL_USER_BINDEDE);
            }

            AuthSocialUserDO authSocialUserDO = new AuthSocialUserDO();
            authSocialUserDO.setUuid(uuid);
            authSocialUserDO.setUserId(systemUserDO.getId());
            authSocialUserDO.setSource(sourceType);
            authSocialUserDO.setOpenId(authUser.getToken().getOpenId());
            authSocialUserDO.setToken(authUser.getToken().getAccessToken());
            authSocialUserDO.setRowTokenInfo(JSON.toJSONString(authUser.getToken()));
            authSocialUserDO.setNickname(authUser.getNickname());
            authSocialUserDO.setAvatar(authUser.getAvatar());
            authSocialUserDO.setRowUserInfo(authUser.getRawUserInfo().toString());
            authSocialUserDO.setCode(socialUserBindVO.getState());
            authSocialUserDO.setState(socialUserBindVO.getState());
            authSocialUserDO.setBindTime(new Date());
            authSocialUserService.save(authSocialUserDO);

            redisCache.removeCacheObject(getAuthStateKey(socialUserBindVO.getState()));

            return tokenService.createToken(loginUserDTO);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }
}
