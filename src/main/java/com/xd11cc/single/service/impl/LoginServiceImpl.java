package com.xd11cc.single.service.impl;

import cn.hutool.captcha.*;
import com.alibaba.fastjson2.JSON;
import com.xd11cc.single.config.RedisCache;
import com.xd11cc.single.config.auth.AuthRequestFactory;
import com.xd11cc.single.constants.CacheConstants;
import com.xd11cc.single.constants.UserConstants;
import com.xd11cc.single.convert.SystemUserConvert;
import com.xd11cc.single.entity.domain.AuthSocialUserDO;
import com.xd11cc.single.entity.domain.SystemUserDO;
import com.xd11cc.single.entity.dto.LoginUserDTO;
import com.xd11cc.single.entity.vo.CaptchaVO;
import com.xd11cc.single.entity.vo.LoginPasswordVO;
import com.xd11cc.single.entity.vo.SocialUserBindVO;
import com.xd11cc.single.entity.vo.UserLoginInfoVO;
import com.xd11cc.single.enums.OperateStatusEnum;
import com.xd11cc.single.enums.LoginTypeEnum;
import com.xd11cc.single.enums.LoginWayEnum;
import com.xd11cc.single.enums.SystemErrorEnum;
import com.xd11cc.single.config.exception.ServiceException;
import com.xd11cc.single.service.*;
import com.xd11cc.single.utils.IdUtils;
import com.xd11cc.single.utils.StringUtils;
import com.xd11cc.single.entity.domain.SystemUserRoleDO;
import com.xd11cc.single.entity.domain.SystemRoleDO;
import lombok.SneakyThrows;
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
    private AuthRequestFactory authRequestFactory;
    @Autowired
    private IAuthSocialUserService authSocialUserService;
    @Autowired
    private ISystemConfigService systemConfigService;
    @Autowired
    private ISystemUserRoleService systemUserRoleService;
    @Autowired
    private ISystemRoleService systemRoleService;
    @Autowired
    private ISystemLoginLogService systemLoginLogService;

    private String getCaptchaKey(String uuid){
        return CacheConstants.CAPTCHA_KEY + uuid;
    }


    private String getAuthStateKey(String key) {
        return CacheConstants.AUTH_STATE_KEY + key;
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
        Authentication authentication;
        try {
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(loginPasswordVO.getUsername(), loginPasswordVO.getPassword());
            // 设置用户上下文
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            // 该方法会去调用UserDetailsServiceImpl.loadUserByUsername
            authentication = authenticationManager.authenticate(authenticationToken);
        } catch (Exception e) {
            systemLoginLogService.recordLoginLog(loginPasswordVO.getUsername(), LoginTypeEnum.PASSWORD, OperateStatusEnum.FAIL, e.getMessage());
            throw e;
        } finally {
            SecurityContextHolder.clearContext();
        }
        // 4、创建token信息
        LoginUserDTO loginUserDTO = (LoginUserDTO) authentication.getPrincipal();
        String token = tokenService.createToken(loginUserDTO);
        systemLoginLogService.recordLoginLog(loginPasswordVO.getUsername(), LoginTypeEnum.PASSWORD, OperateStatusEnum.SUCCESS, "登录成功");
        return token;
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
        // 查询角色信息
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
        String uuid = IdUtils.fastUUID();
        redisCache.setCacheObject(getCaptchaKey(uuid), captcha.getCode(), 1, TimeUnit.MINUTES);
        captchaVO.setCaptchaId(uuid);
        captchaVO.setImage(captcha.getImageBase64());
        return captchaVO;
    }

    @Override
    public String getRedirectUri(String source) {
        AuthRequest authRequest = authRequestFactory.get(source);
        return authRequest.authorize(AuthStateUtils.createState());
    }

    @SneakyThrows
    @Override
    public void callback(String source, String code, String state, HttpServletResponse response) {
        AuthCallback authCallback = AuthCallback.builder()
                .state(state)
                .code(code).build();
        AuthRequest authRequest = authRequestFactory.get(source);
        AuthResponse<AuthUser> authResponse = authRequest.login(authCallback);
        if (!authResponse.ok()){
            log.error("{}授权登录失败，原因:{}", source, authResponse.getMsg());
            return;
        }

        AuthUser authUser = authResponse.getData();

        log.info("authUser:{}", JSON.toJSONString(authUser));

        String sourceType = authUser.getSource().toLowerCase();
        String uuid = authUser.getUuid();

        SystemUserDO systemUserDO;

        String successUrl = systemConfigService.getConfig("auth-redirect-successUrl");
        // 1、判断是否社交绑定
        AuthSocialUserDO authSocialUserDO = authSocialUserService.getBySourceAndUuid(sourceType, uuid);
        if (null != authSocialUserDO) {
            // 已绑定
            authSocialUserDO.setToken(authUser.getToken().getAccessToken());
            authSocialUserDO.setOpenId(authUser.getToken().getOpenId());
            authSocialUserDO.setRowTokenInfo(JSON.toJSONString(authUser.getToken()));
            authSocialUserService.updateById(authSocialUserDO);
            systemUserDO = systemUserService.getById(authSocialUserDO.getUserId());
            String token = tokenService.createToken(new LoginUserDTO(systemMenuService.getPermission(systemUserDO.getId()), systemUserDO));
            systemLoginLogService.recordLoginLog(systemUserDO.getUsername(), LoginTypeEnum.SOCIAL, OperateStatusEnum.SUCCESS, "登录成功");
            response.sendRedirect(String.format(successUrl, token));
            return;
        }

        // 2、未绑定，判断邮箱匹配系统用户
        systemUserDO = systemUserService.getByEmail(authUser.getEmail());
        if (null != systemUserDO) {
            // 匹配，绑定系统用户
            authSocialUserDO = new AuthSocialUserDO();
            authSocialUserDO.setUuid(uuid);
            authSocialUserDO.setUserId(systemUserDO.getId());
            authSocialUserDO.setSource(sourceType);
            authSocialUserDO.setOpenId(authUser.getToken().getOpenId());
            authSocialUserDO.setToken(authUser.getToken().getAccessToken());
            authSocialUserDO.setRowTokenInfo(JSON.toJSONString(authUser.getToken()));
            authSocialUserDO.setNickname(authUser.getNickname());
            authSocialUserDO.setAvatar(authUser.getAvatar());
            authSocialUserDO.setRowUserInfo(authUser.getRawUserInfo().toString());
            authSocialUserDO.setCode(code);
            authSocialUserDO.setState(state);
            authSocialUserService.save(authSocialUserDO);
            String token = tokenService.createToken(new LoginUserDTO(systemMenuService.getPermission(systemUserDO.getId()), systemUserDO));
            systemLoginLogService.recordLoginLog(systemUserDO.getUsername(), LoginTypeEnum.SOCIAL, OperateStatusEnum.SUCCESS, "登录成功");
            response.sendRedirect(String.format(successUrl, token));
            return;
        }
        // 3、不匹配，前端跳转到绑定页面（注册系统用户）
        redisCache.setCacheObject(getAuthStateKey(state), authUser, 500, TimeUnit.MINUTES);
        String bindUserUrl = systemConfigService.getConfig("auth-redirect-bindUserUrl");
        response.sendRedirect(String.format(bindUserUrl, source, state));
    }

    @Override
    @Transactional
    public String socialUserBind(SocialUserBindVO socialUserBindVO) {
        // 1、从 Redis 获取授权用户信息
        AuthUser authUser = redisCache.getCacheObject(getAuthStateKey(socialUserBindVO.getState()));
        if (authUser == null) {
            throw new ServiceException(SystemErrorEnum.SOCIAL_USER_NOT_FOUND);
        }

        // 2、验证用户名密码
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

        // 认证成功，将用户信息设入上下文（供 DefaultDBFieldHandler 自动填充 createUserId）
        SecurityContextHolder.getContext().setAuthentication(authentication);

        LoginUserDTO loginUserDTO = (LoginUserDTO) authentication.getPrincipal();
        SystemUserDO systemUserDO = loginUserDTO.getSystemUserDO();

        // 3、校验该社交账号是否已绑定其他用户
        String sourceType = authUser.getSource().toLowerCase();
        String uuid = authUser.getUuid();
        AuthSocialUserDO existSocialUser = authSocialUserService.getBySourceAndUuid(sourceType, uuid);
        if (existSocialUser != null) {
            throw new ServiceException(SystemErrorEnum.SOCIAL_USER_BINDEDE);
        }

        // 4、创建绑定记录
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

        // 5、删除 Redis 缓存（一次性消费）
        redisCache.removeCacheObject(getAuthStateKey(socialUserBindVO.getState()));

        // 6、生成 Token
        return tokenService.createToken(loginUserDTO);
    }
}
