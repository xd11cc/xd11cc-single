package com.xd11cc.single.service.impl;

import com.xd11cc.single.config.exception.ServiceException;
import com.xd11cc.single.config.RedisCache;
import com.xd11cc.single.entity.domain.SystemRoleDO;
import com.xd11cc.single.entity.domain.SystemUserDO;
import com.xd11cc.single.entity.domain.SystemUserRoleDO;
import com.xd11cc.single.entity.dto.LoginUserDTO;
import com.xd11cc.single.entity.vo.CaptchaVO;
import com.xd11cc.single.entity.vo.LoginPasswordVO;
import com.xd11cc.single.entity.vo.UserLoginInfoVO;
import com.xd11cc.single.enums.LoginTypeEnum;
import com.xd11cc.single.enums.LoginWayEnum;
import com.xd11cc.single.enums.OperateStatusEnum;
import com.xd11cc.single.enums.SystemErrorEnum;
import com.xd11cc.single.service.ISystemLoginLogService;
import com.xd11cc.single.service.ISystemMenuService;
import com.xd11cc.single.service.ISystemRoleService;
import com.xd11cc.single.service.ISystemUserRoleService;
import com.xd11cc.single.service.ISystemUserService;
import com.xd11cc.single.service.TokenService;
import com.xd11cc.single.util.BaseUnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.Collections;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.mock;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class LoginServiceImplTest extends BaseUnitTest {

    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private TokenService tokenService;
    @Mock
    private ISystemUserService systemUserService;
    @Mock
    private ISystemMenuService systemMenuService;
    @Mock
    private RedisCache redisCache;
    @Mock
    private ISystemLoginLogService systemLoginLogService;
    @Mock
    private ISystemUserRoleService systemUserRoleService;
    @Mock
    private ISystemRoleService systemRoleService;

    @InjectMocks
    private LoginServiceImpl loginService;

    @Test
    void loginByPassword_验证码已过期_抛异常() {
        given(redisCache.getCacheObject(anyString())).willReturn(null);

        ServiceException ex = assertThrows(ServiceException.class,
                () -> loginService.loginByPassword(buildLoginVO("user", "pwd", "cid", "any")));
        assertThat(ex.getErrorCode()).isEqualTo(SystemErrorEnum.CAPTCHA_EXPIRE);
    }

    @Test
    void loginByPassword_验证码不匹配_抛异常() {
        given(redisCache.getCacheObject(anyString())).willReturn("stored_captcha");

        ServiceException ex = assertThrows(ServiceException.class,
                () -> loginService.loginByPassword(buildLoginVO("user", "pwd", "cid", "wrong")));
        assertThat(ex.getErrorCode()).isEqualTo(SystemErrorEnum.CAPTCHA_ERROR);
    }

    @Test
    void loginByPassword_非PC端登录_抛非法访问() {
        given(redisCache.getCacheObject(anyString())).willReturn("correct");

        LoginPasswordVO vo = buildLoginVO("user", "pwd", "cid", "correct");
        vo.setWay(LoginWayEnum.APP.getCode());

        ServiceException ex = assertThrows(ServiceException.class,
                () -> loginService.loginByPassword(vo));
        assertThat(ex.getErrorCode()).isEqualTo(SystemErrorEnum.ILLEGAL_VISIT);
    }

    @Test
    void loginByPassword_密码短于最小长度_抛异常() {
        given(redisCache.getCacheObject(anyString())).willReturn("correct");

        ServiceException ex = assertThrows(ServiceException.class,
                () -> loginService.loginByPassword(buildLoginVO("user", "12345", "cid", "correct")));
        assertThat(ex.getErrorCode()).isEqualTo(SystemErrorEnum.PASSWORD_ERROR);
    }

    @Test
    void loginByPassword_密码长于最大长度_抛异常() {
        given(redisCache.getCacheObject(anyString())).willReturn("correct");

        String longPwd = "123456789012345678901"; // 21 chars, max is 20
        ServiceException ex = assertThrows(ServiceException.class,
                () -> loginService.loginByPassword(buildLoginVO("user", longPwd, "cid", "correct")));
        assertThat(ex.getErrorCode()).isEqualTo(SystemErrorEnum.PASSWORD_ERROR);
    }

    @Test
    void loginByPassword_认证失败_记录失败日志_抛出异常() {
        given(redisCache.getCacheObject(anyString())).willReturn("correct");
        org.mockito.BDDMockito.doThrow(new RuntimeException("Bad credentials"))
                .when(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));

        assertThrows(RuntimeException.class,
                () -> loginService.loginByPassword(buildLoginVO("admin", "123456", "cid", "correct")));

        then(systemLoginLogService).should().recordLoginLog(
                eq("admin"), eq(LoginTypeEnum.PASSWORD), eq(OperateStatusEnum.FAIL), anyString());
        then(tokenService).should(org.mockito.BDDMockito.never()).createToken(any());
    }

    @Test
    void loginByPassword_认证成功_返回token并记录成功日志() {
        given(redisCache.getCacheObject(anyString())).willReturn("correct");
        Authentication auth = mock(Authentication.class);
        SystemUserDO userDO = new SystemUserDO();
        userDO.setId(1L);
        userDO.setUsername("admin");
        userDO.setTenantId(1L);
        LoginUserDTO loginUser = new LoginUserDTO(Collections.emptySet(), userDO);
        loginUser.setUserId(1L);
        given(auth.getPrincipal()).willReturn(loginUser);
        given(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .willReturn(auth);
        given(tokenService.createToken(loginUser)).willReturn("mock-jwt-token");

        String token = loginService.loginByPassword(buildLoginVO("admin", "123456", "cid", "correct"));

        assertThat(token).isEqualTo("mock-jwt-token");
        then(systemLoginLogService).should().recordLoginLog(
                eq("admin"), eq(LoginTypeEnum.PASSWORD), eq(OperateStatusEnum.SUCCESS), eq("登录成功"));
    }

    @Test
    void getUserLoginInfo_无角色关联_返回不含角色的用户信息() {
        SystemUserDO user = buildSystemUser(1L, "admin");
        given(systemUserService.getById(1L)).willReturn(user);
        given(systemMenuService.getPermission(1L)).willReturn(Collections.singleton("system:user:list"));

        UserLoginInfoVO result = loginService.getUserLoginInfo(1L);

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("admin");
        assertThat(result.getDeptId()).isEqualTo(10L);
        // 无角色关联时，roleIds/roles/roleNames 保持 null（converter 未映射，set 块未执行）
        assertThat(result.getRoleIds()).isNull();
        assertThat(result.getRoles()).isNull();
        assertThat(result.getRoleNames()).isNull();
        assertThat(result.getPermissions()).containsExactly("system:user:list");
    }

    @Test
    void getUserLoginInfo_有角色关联_填充角色和权限信息() {
        SystemUserDO user = buildSystemUser(1L, "admin");
        given(systemUserService.getById(1L)).willReturn(user);
        given(systemMenuService.getPermission(1L)).willReturn(Collections.singleton("system:user:list"));

        SystemUserRoleDO userRole = new SystemUserRoleDO();
        userRole.setUserId(1L);
        userRole.setRoleId(100L);
        given(systemUserRoleService.list(any())).willReturn(Collections.singletonList(userRole));

        SystemRoleDO role = new SystemRoleDO();
        role.setRoleCode("admin");
        role.setRoleName("管理员");
        given(systemRoleService.listByIds(Collections.singleton(100L))).willReturn(Collections.singletonList(role));

        UserLoginInfoVO result = loginService.getUserLoginInfo(1L);

        assertThat(result.getRoleIds()).containsExactly(100L);
        assertThat(result.getRoles()).containsExactly("admin");
        assertThat(result.getRoleNames()).containsExactly("管理员");
    }

    @Test
    void getCaptcha_生成验证码并缓存() {
        CaptchaVO result = loginService.getCaptcha();

        assertThat(result).isNotNull();
        assertThat(result.getCaptchaId()).isNotBlank();
        assertThat(result.getImage()).isNotBlank();
        then(redisCache).should().setCacheObject(
                anyString(), anyString(), eq(1L), any());
    }

    private static LoginPasswordVO buildLoginVO(String username, String password, String captchaId, String captcha) {
        LoginPasswordVO vo = new LoginPasswordVO();
        vo.setWay(LoginWayEnum.PC.getCode());
        vo.setUsername(username);
        vo.setPassword(password);
        vo.setCaptchaId(captchaId);
        vo.setCaptcha(captcha);
        return vo;
    }

    private static SystemUserDO buildSystemUser(Long id, String username) {
        SystemUserDO user = new SystemUserDO();
        user.setId(id);
        user.setUsername(username);
        user.setNickname("昵称" + username);
        user.setDeptId(10L);
        user.setStatus("0");
        return user;
    }
}
