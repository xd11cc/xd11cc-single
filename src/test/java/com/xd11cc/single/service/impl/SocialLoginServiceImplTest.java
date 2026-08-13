package com.xd11cc.single.service.impl;

import com.xd11cc.single.config.RedisCache;
import com.xd11cc.single.config.auth.AuthRequestFactory;
import com.xd11cc.single.config.exception.ServiceException;
import com.xd11cc.single.constants.CacheConstants;
import com.xd11cc.single.entity.domain.AuthSocialUserDO;
import com.xd11cc.single.entity.domain.SystemUserDO;
import com.xd11cc.single.entity.dto.LoginUserDTO;
import com.xd11cc.single.entity.vo.SocialUserBindVO;
import com.xd11cc.single.enums.LoginTypeEnum;
import com.xd11cc.single.enums.OperateStatusEnum;
import com.xd11cc.single.enums.SystemErrorEnum;
import com.xd11cc.single.service.DataScopeService;
import com.xd11cc.single.service.IAuthSocialUserService;
import com.xd11cc.single.service.ISystemConfigService;
import com.xd11cc.single.service.ISystemLoginLogService;
import com.xd11cc.single.service.ISystemMenuService;
import com.xd11cc.single.service.ISystemUserService;
import com.xd11cc.single.service.TokenService;
import me.zhyd.oauth.model.AuthCallback;
import me.zhyd.oauth.model.AuthResponse;
import me.zhyd.oauth.model.AuthToken;
import me.zhyd.oauth.model.AuthUser;
import me.zhyd.oauth.request.AuthRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;

import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SocialLoginServiceImplTest {

    @Mock
    private AuthRequestFactory authRequestFactory;

    @Mock
    private TokenService tokenService;

    @Mock
    private ISystemUserService systemUserService;

    @Mock
    private ISystemMenuService systemMenuService;

    @Mock
    private RedisCache redisCache;

    @Mock
    private IAuthSocialUserService authSocialUserService;

    @Mock
    private ISystemConfigService systemConfigService;

    @Mock
    private DataScopeService dataScopeService;

    @Mock
    private ISystemLoginLogService systemLoginLogService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private SocialLoginServiceImpl socialLoginService;

    // ==================== getRedirectUri ====================

    @Test
    void getRedirectUri_返回授权地址() {
        AuthRequest authRequest = mock(AuthRequest.class);
        given(authRequestFactory.get("GITHUB")).willReturn(authRequest);
        given(authRequest.authorize(anyString())).willReturn("https://github.com/login/oauth/authorize?state=abc123");

        String redirectUri = socialLoginService.getRedirectUri("GITHUB");

        assertThat(redirectUri).isEqualTo("https://github.com/login/oauth/authorize?state=abc123");
        verify(authRequestFactory).get("GITHUB");
    }

    // ==================== callback - OAuth 响应失败 ====================

    @Test
    void callback_OAuth响应失败_直接返回() {
        AuthRequest authRequest = mock(AuthRequest.class);
        given(authRequestFactory.get("GITHUB")).willReturn(authRequest);
        AuthResponse<AuthUser> authResponse = mock(AuthResponse.class);
        given(authRequest.login(any(AuthCallback.class))).willReturn(authResponse);
        given(authResponse.ok()).willReturn(false);
        given(authResponse.getMsg()).willReturn("access_denied");

        socialLoginService.callback("GITHUB", "code123", "state123", mock(HttpServletResponse.class));

        verify(authRequestFactory).get("GITHUB");
        verify(authRequest).login(any(AuthCallback.class));
        verifyNoInteractions(authSocialUserService, systemUserService, redisCache);
    }

    // ==================== callback - 已有社交账号，登录并跳转 ====================

    @Test
    void callback_已有社交账号_更新令牌登录并跳转() throws Exception {
        AuthRequest authRequest = mock(AuthRequest.class);
        given(authRequestFactory.get("GITHUB")).willReturn(authRequest);
        AuthResponse<AuthUser> authResponse = mock(AuthResponse.class);
        given(authRequest.login(any(AuthCallback.class))).willReturn(authResponse);
        given(authResponse.ok()).willReturn(true);

        AuthUser authUser = mockAuthUser("github", "uuid-1", "user@example.com");
        given(authResponse.getData()).willReturn(authUser);
        given(authUser.getSource()).willReturn("github");

        AuthSocialUserDO socialUser = new AuthSocialUserDO();
        socialUser.setUserId(100L);
        given(authSocialUserService.getBySourceAndUuid("github", "uuid-1")).willReturn(socialUser);

        SystemUserDO systemUser = new SystemUserDO();
        systemUser.setId(100L);
        systemUser.setUsername("existingUser");
        given(systemUserService.getById(100L)).willReturn(systemUser);

        given(systemMenuService.getPermission(100L)).willReturn(Collections.emptySet());
        doNothing().when(dataScopeService).resolveDataScope(any(LoginUserDTO.class));
        given(tokenService.createToken(any(LoginUserDTO.class))).willReturn("token-xyz");
        given(systemConfigService.getConfig("auth-redirect-successUrl")).willReturn("https://app.com/success?token=%s");

        HttpServletResponse response = mock(HttpServletResponse.class);

        socialLoginService.callback("GITHUB", "code123", "state123", response);

        verify(response).sendRedirect("https://app.com/success?token=token-xyz");
        verify(systemLoginLogService).recordLoginLog("existingUser", LoginTypeEnum.SOCIAL, OperateStatusEnum.SUCCESS, "登录成功");
        verify(authSocialUserService).updateById(any(AuthSocialUserDO.class));

        ArgumentCaptor<LoginUserDTO> captor = ArgumentCaptor.forClass(LoginUserDTO.class);
        verify(tokenService).createToken(captor.capture());
        assertThat(captor.getValue().getSystemUserDO().getUsername()).isEqualTo("existingUser");
    }

    // ==================== callback - 邮箱已存在，自动关联社交账号 ====================

    @Test
    void callback_邮箱已存在_自动关联并登录() throws Exception {
        AuthRequest authRequest = mock(AuthRequest.class);
        given(authRequestFactory.get("GITHUB")).willReturn(authRequest);
        AuthResponse<AuthUser> authResponse = mock(AuthResponse.class);
        given(authRequest.login(any(AuthCallback.class))).willReturn(authResponse);
        given(authResponse.ok()).willReturn(true);

        AuthUser authUser = mockAuthUser("github", "uuid-2", "existing@example.com");
        given(authResponse.getData()).willReturn(authUser);
        given(authUser.getSource()).willReturn("github");

        given(authSocialUserService.getBySourceAndUuid("github", "uuid-2")).willReturn(null);

        SystemUserDO systemUser = new SystemUserDO();
        systemUser.setUsername("emailUser");
        systemUser.setId(200L);
        given(systemUserService.getByEmail("existing@example.com")).willReturn(systemUser);

        given(systemMenuService.getPermission(200L)).willReturn(Collections.emptySet());
        doNothing().when(dataScopeService).resolveDataScope(any(LoginUserDTO.class));
        given(tokenService.createToken(any(LoginUserDTO.class))).willReturn("token-abc");
        given(systemConfigService.getConfig("auth-redirect-successUrl")).willReturn("https://app.com/success?token=%s");

        HttpServletResponse response = mock(HttpServletResponse.class);

        socialLoginService.callback("GITHUB", "code123", "state123", response);

        verify(authSocialUserService).save(any(AuthSocialUserDO.class));
        verify(response).sendRedirect("https://app.com/success?token=token-abc");
        verify(systemLoginLogService).recordLoginLog("emailUser", LoginTypeEnum.SOCIAL, OperateStatusEnum.SUCCESS, "登录成功");

        ArgumentCaptor<AuthSocialUserDO> captor = ArgumentCaptor.forClass(AuthSocialUserDO.class);
        verify(authSocialUserService).save(captor.capture());
        AuthSocialUserDO saved = captor.getValue();
        assertThat(saved.getUuid()).isEqualTo("uuid-2");
        assertThat(saved.getUserId()).isEqualTo(200L);
        assertThat(saved.getSource()).isEqualTo("github");
        assertThat(saved.getOpenId()).isEqualTo("open-uuid-2");
        assertThat(saved.getToken()).isEqualTo("access-uuid-2");
        assertThat(saved.getNickname()).isEqualTo("TestNick");
        assertThat(saved.getCode()).isEqualTo("code123");
        assertThat(saved.getState()).isEqualTo("state123");
    }

    // ==================== callback - 新用户，缓存状态跳转绑定页 ====================

    @Test
    void callback_新用户_缓存授权状态并跳转绑定页() throws Exception {
        AuthRequest authRequest = mock(AuthRequest.class);
        given(authRequestFactory.get("GITHUB")).willReturn(authRequest);
        AuthResponse<AuthUser> authResponse = mock(AuthResponse.class);
        given(authRequest.login(any(AuthCallback.class))).willReturn(authResponse);
        given(authResponse.ok()).willReturn(true);

        AuthUser authUser = mockAuthUser("github", "uuid-3", "new@example.com");
        given(authResponse.getData()).willReturn(authUser);

        given(authSocialUserService.getBySourceAndUuid("github", "uuid-3")).willReturn(null);
        given(systemUserService.getByEmail("new@example.com")).willReturn(null);
        given(systemConfigService.getConfig("auth-redirect-successUrl")).willReturn("https://app.com/success?token=%s");
        given(systemConfigService.getConfig("auth-redirect-bindUserUrl")).willReturn("https://app.com/bind?source=%s&state=%s");

        HttpServletResponse response = mock(HttpServletResponse.class);

        socialLoginService.callback("GITHUB", "code123", "state123", response);

        verify(redisCache).setCacheObject(
                eq(CacheConstants.AUTH_STATE_KEY + "state123"),
                eq(authUser),
                eq(500L),
                any(TimeUnit.class));
        // bindUserUrl 使用原始 source（大写）
        verify(response).sendRedirect("https://app.com/bind?source=GITHUB&state=state123");
    }

    // ==================== callback - 缺少成功URL配置 ====================

    @Test
    void callback_缺少成功URL配置_直接返回() {
        AuthRequest authRequest = mock(AuthRequest.class);
        given(authRequestFactory.get("GITHUB")).willReturn(authRequest);
        AuthResponse<AuthUser> authResponse = mock(AuthResponse.class);
        given(authRequest.login(any(AuthCallback.class))).willReturn(authResponse);
        given(authResponse.ok()).willReturn(true);

        AuthUser authUser = mockAuthUser("github", "uuid-4", "user@example.com");
        given(authResponse.getData()).willReturn(authUser);
        given(systemConfigService.getConfig("auth-redirect-successUrl")).willReturn(null);

        socialLoginService.callback("GITHUB", "code123", "state123", mock(HttpServletResponse.class));

        // successUrl 为 null 时直接返回，不查询用户和社交账号
        verifyNoInteractions(authSocialUserService, systemUserService, systemMenuService, redisCache);
    }

    // ==================== callback - 缺少绑定URL配置 ====================

    @Test
    void callback_新用户且缺少绑定URL_直接返回() throws Exception {
        AuthRequest authRequest = mock(AuthRequest.class);
        given(authRequestFactory.get("GITHUB")).willReturn(authRequest);
        AuthResponse<AuthUser> authResponse = mock(AuthResponse.class);
        given(authRequest.login(any(AuthCallback.class))).willReturn(authResponse);
        given(authResponse.ok()).willReturn(true);

        AuthUser authUser = mockAuthUser("github", "uuid-5", "new@example.com");
        given(authResponse.getData()).willReturn(authUser);

        given(authSocialUserService.getBySourceAndUuid("github", "uuid-5")).willReturn(null);
        given(systemUserService.getByEmail("new@example.com")).willReturn(null);
        given(systemConfigService.getConfig("auth-redirect-successUrl")).willReturn("https://app.com/success?token=%s");
        given(systemConfigService.getConfig("auth-redirect-bindUserUrl")).willReturn(null);

        HttpServletResponse response = mock(HttpServletResponse.class);

        socialLoginService.callback("GITHUB", "code123", "state123", response);

        verify(redisCache).setCacheObject(anyString(), any(), anyLong(), any(TimeUnit.class));
        verify(response, never()).sendRedirect(anyString());
    }

    // ==================== socialUserBind - 状态已过期 ====================

    @Test
    void socialUserBind_状态已过期_抛异常() {
        SocialUserBindVO bindVO = new SocialUserBindVO();
        bindVO.setState("expired-state");
        bindVO.setUsername("bindUser");
        bindVO.setPassword("pass");

        given(redisCache.getCacheObject(CacheConstants.AUTH_STATE_KEY + "expired-state")).willReturn(null);

        assertThatThrownBy(() -> socialLoginService.socialUserBind(bindVO))
                .isInstanceOf(ServiceException.class)
                .satisfies(ex -> {
                    ServiceException se = (ServiceException) ex;
                    assertThat(se.getErrorCode()).isEqualTo(SystemErrorEnum.SOCIAL_USER_NOT_FOUND);
                });
    }

    // ==================== socialUserBind - 已绑定 ====================

    @Test
    void socialUserBind_已绑定_抛已绑定异常() throws Exception {
        SocialUserBindVO bindVO = new SocialUserBindVO();
        bindVO.setState("state-1");
        bindVO.setUsername("bindUser");
        bindVO.setPassword("pass");

        AuthUser cachedAuthUser = mockAuthUser("github", "uuid-1", "user@example.com");
        given(redisCache.getCacheObject(CacheConstants.AUTH_STATE_KEY + "state-1")).willReturn(cachedAuthUser);

        AuthSocialUserDO existSocialUser = new AuthSocialUserDO();
        existSocialUser.setUuid("uuid-1");
        given(authSocialUserService.getBySourceAndUuid("github", "uuid-1")).willReturn(existSocialUser);

        Authentication authentication = mock(Authentication.class);
        LoginUserDTO loginUserDTO = mock(LoginUserDTO.class);
        SystemUserDO systemUser = new SystemUserDO();
        systemUser.setUsername("bindUser");
        systemUser.setId(300L);
        given(loginUserDTO.getSystemUserDO()).willReturn(systemUser);
        given(authentication.getPrincipal()).willReturn(loginUserDTO);
        given(authenticationManager.authenticate(any(org.springframework.security.authentication.UsernamePasswordAuthenticationToken.class)))
                .willReturn(authentication);

        assertThatThrownBy(() -> socialLoginService.socialUserBind(bindVO))
                .isInstanceOf(ServiceException.class)
                .satisfies(ex -> {
                    ServiceException se = (ServiceException) ex;
                    assertThat(se.getErrorCode()).isEqualTo(SystemErrorEnum.SOCIAL_USER_BINDEDE);
                });
    }

    // ==================== socialUserBind - 新绑定成功 ====================

    @Test
    void socialUserBind_新绑定_创建关联并返回token() throws Exception {
        SocialUserBindVO bindVO = new SocialUserBindVO();
        bindVO.setState("state-1");
        bindVO.setUsername("bindUser");
        bindVO.setPassword("pass");

        AuthUser cachedAuthUser = mockAuthUser("github", "uuid-1", "user@example.com");
        given(redisCache.getCacheObject(CacheConstants.AUTH_STATE_KEY + "state-1")).willReturn(cachedAuthUser);

        Authentication authentication = mock(Authentication.class);
        LoginUserDTO loginUserDTO = mock(LoginUserDTO.class);
        SystemUserDO systemUser = new SystemUserDO();
        systemUser.setUsername("bindUser");
        systemUser.setId(300L);
        given(loginUserDTO.getSystemUserDO()).willReturn(systemUser);
        given(authentication.getPrincipal()).willReturn(loginUserDTO);
        given(authenticationManager.authenticate(any(org.springframework.security.authentication.UsernamePasswordAuthenticationToken.class)))
                .willReturn(authentication);

        given(authSocialUserService.getBySourceAndUuid("github", "uuid-1")).willReturn(null);
        given(tokenService.createToken(any(LoginUserDTO.class))).willReturn("token-new-bind");

        String result = socialLoginService.socialUserBind(bindVO);

        assertThat(result).isEqualTo("token-new-bind");

        ArgumentCaptor<AuthSocialUserDO> captor = ArgumentCaptor.forClass(AuthSocialUserDO.class);
        verify(authSocialUserService).save(captor.capture());
        AuthSocialUserDO saved = captor.getValue();
        assertThat(saved.getUuid()).isEqualTo("uuid-1");
        assertThat(saved.getUserId()).isEqualTo(300L);
        assertThat(saved.getSource()).isEqualTo("github");
        assertThat(saved.getOpenId()).isEqualTo("open-uuid-1");
        assertThat(saved.getToken()).isEqualTo("access-uuid-1");
        assertThat(saved.getNickname()).isEqualTo("TestNick");
        assertThat(saved.getAvatar()).isEqualTo("https://example.com/avatar.png");
        assertThat(saved.getCode()).isEqualTo("state-1");
        assertThat(saved.getState()).isEqualTo("state-1");

        verify(redisCache).removeCacheObject(CacheConstants.AUTH_STATE_KEY + "state-1");
    }

    // ==================== 辅助方法 ====================

    private AuthUser mockAuthUser(String source, String uuid, String email) {
        AuthUser authUser = mock(AuthUser.class);
        given(authUser.getSource()).willReturn(source);
        given(authUser.getUuid()).willReturn(uuid);
        given(authUser.getEmail()).willReturn(email);
        given(authUser.getNickname()).willReturn("TestNick");
        given(authUser.getAvatar()).willReturn("https://example.com/avatar.png");

        AuthToken token = mock(AuthToken.class);
        given(token.getOpenId()).willReturn("open-" + uuid);
        given(token.getAccessToken()).willReturn("access-" + uuid);
        given(authUser.getToken()).willReturn(token);

        com.alibaba.fastjson.JSONObject rawInfo = mock(com.alibaba.fastjson.JSONObject.class);
        given(rawInfo.toString()).willReturn("{\"source\":\"" + source + "\"}");
        given(authUser.getRawUserInfo()).willReturn(rawInfo);

        return authUser;
    }
}
