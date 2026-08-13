package com.xd11cc.single.controller;

import com.xd11cc.single.config.RedisCache;
import com.xd11cc.single.constants.CacheConstants;
import com.xd11cc.single.entity.base.ResponseVO;
import com.xd11cc.single.entity.vo.CaptchaVO;
import com.xd11cc.single.entity.vo.LoginPasswordVO;
import com.xd11cc.single.entity.vo.RouteVO;
import com.xd11cc.single.entity.vo.SocialUserBindVO;
import com.xd11cc.single.entity.vo.UserLoginInfoVO;
import com.xd11cc.single.service.ISystemMenuService;
import com.xd11cc.single.service.LoginService;
import com.xd11cc.single.service.SocialLoginService;
import com.xd11cc.single.utils.SecurityUtils;
import me.zhyd.oauth.model.AuthUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

@ExtendWith(MockitoExtension.class)
class LoginControllerTest {

    @Mock
    private LoginService loginService;

    @Mock
    private SocialLoginService socialLoginService;

    @Mock
    private ISystemMenuService systemMenuService;

    @Mock
    private RedisCache redisCache;

    @InjectMocks
    private LoginController loginController;

    // ==================== loginByPassword ====================

    @Test
    void loginByPassword_成功_返回token() {
        LoginPasswordVO vo = buildLoginPasswordVO("admin", "pass123", "captchaId", "captchaCode");
        given(loginService.loginByPassword(vo)).willReturn("token-12345");

        ResponseVO<String> result = loginController.loginByPassword(vo);

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData()).isEqualTo("token-12345");
    }

    // ==================== captcha ====================

    @Test
    void captcha_获取验证码_返回验证码信息() {
        CaptchaVO captcha = new CaptchaVO();
        captcha.setCaptchaId("id-123");
        captcha.setImage("base64-image-data");
        given(loginService.getCaptcha()).willReturn(captcha);

        ResponseVO<CaptchaVO> result = loginController.captcha();

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData()).isNotNull();
        assertThat(result.getData().getCaptchaId()).isEqualTo("id-123");
    }

    // ==================== getUserLoginInfo ====================

    @Test
    void getUserLoginInfo_查询当前用户登录信息() {
        try (MockedStatic<SecurityUtils> mock = mockStatic(SecurityUtils.class)) {
            mock.when(SecurityUtils::getUserId).thenReturn(1L);
            UserLoginInfoVO info = new UserLoginInfoVO();
            info.setId(1L);
            info.setUsername("admin");
            given(loginService.getUserLoginInfo(1L)).willReturn(info);

            ResponseVO<UserLoginInfoVO> result = loginController.getUserLoginInfo();

            assertThat(result.getCode()).isEqualTo(200);
            assertThat(result.getData()).isNotNull();
            assertThat(result.getData().getUsername()).isEqualTo("admin");
        }
    }

    // ==================== getRoutes ====================

    @Test
    void getRoutes_查询当前用户路由信息() {
        try (MockedStatic<SecurityUtils> mock = mockStatic(SecurityUtils.class)) {
            mock.when(SecurityUtils::getUserId).thenReturn(1L);
            List<RouteVO> routes = Arrays.asList(buildRoute(1L, "首页", "/home"));
            given(systemMenuService.getRoutes(1L)).willReturn(routes);

            ResponseVO<List<RouteVO>> result = loginController.getRoutes();

            assertThat(result.getCode()).isEqualTo(200);
            assertThat(result.getData()).hasSize(1);
            assertThat(result.getData().get(0).getName()).isEqualTo("首页");
        }
    }

    // ==================== render ====================

    @Test
    void render_社交授权认证_返回跳转地址() throws Exception {
        given(socialLoginService.getRedirectUri("gitee")).willReturn("https://gitee.com/oauth/authorize");

        ResponseVO<String> result = loginController.render(
                mock(HttpServletRequest.class), mock(HttpServletResponse.class), "gitee");

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData()).isEqualTo("https://gitee.com/oauth/authorize");
    }

    // ==================== socialUserInfo ====================

    @Test
    void socialUserInfo_查询社交用户信息() {
        AuthUser authUser = new AuthUser();
        authUser.setUuid("uuid-123");
        given(redisCache.getCacheObject(CacheConstants.AUTH_STATE_KEY + "state-123")).willReturn(authUser);

        ResponseVO<AuthUser> result = loginController.socialUserInfo("state-123");

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData()).isNotNull();
    }

    // ==================== socialUserBind ====================

    @Test
    void socialUserBind_社交绑定_返回token() {
        SocialUserBindVO vo = buildSocialUserBindVO(0, 0, 0, "admin", "pass123", "state-123", "gitee");
        given(socialLoginService.socialUserBind(vo)).willReturn("bind-token-123");

        ResponseVO<String> result = loginController.socialUserBind(vo);

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData()).isEqualTo("bind-token-123");
    }

    // ==================== 辅助方法 ====================

    private static LoginPasswordVO buildLoginPasswordVO(String username, String password,
                                                        String captchaId, String captcha) {
        LoginPasswordVO vo = new LoginPasswordVO();
        vo.setUsername(username);
        vo.setPassword(password);
        vo.setCaptchaId(captchaId);
        vo.setCaptcha(captcha);
        return vo;
    }

    private static SocialUserBindVO buildSocialUserBindVO(Integer way, Integer device, Integer app,
                                                          String username, String password,
                                                          String state, String source) {
        SocialUserBindVO vo = new SocialUserBindVO();
        vo.setWay(way);
        vo.setDevice(device);
        vo.setApp(app);
        vo.setUsername(username);
        vo.setPassword(password);
        vo.setState(state);
        vo.setSource(source);
        return vo;
    }

    private static RouteVO buildRoute(Long id, String name, String path) {
        RouteVO route = new RouteVO();
        route.setId(id);
        route.setName(name);
        route.setPath(path);
        return route;
    }
}
