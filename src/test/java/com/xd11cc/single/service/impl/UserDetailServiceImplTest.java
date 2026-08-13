package com.xd11cc.single.service.impl;

import com.xd11cc.single.config.RedisCache;
import com.xd11cc.single.entity.domain.SystemUserDO;
import com.xd11cc.single.entity.dto.LoginUserDTO;
import com.xd11cc.single.enums.SystemErrorEnum;
import com.xd11cc.single.enums.SystemStatusEnum;
import com.xd11cc.single.config.exception.ServiceException;
import com.xd11cc.single.service.ISystemMenuService;
import com.xd11cc.single.service.ISystemUserService;
import com.xd11cc.single.service.DataScopeService;
import com.xd11cc.single.util.BaseUnitTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.mock;
import static org.mockito.BDDMockito.never;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class UserDetailServiceImplTest extends BaseUnitTest {

    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder();
    private static final String RAW_PASSWORD = "TestPwd123!";
    private static final String WRONG_PASSWORD = "WrongPwd456!";

    @Mock
    private ISystemUserService systemUserService;
    @Mock
    private ISystemMenuService systemMenuService;
    @Mock
    private RedisCache redisCache;
    @Mock
    private DataScopeService dataScopeService;

    @InjectMocks
    private UserDetailServiceImpl userDetailService;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // ==================== 前置校验分支 ====================

    @Test
    void loadUserByUsername_用户不存在_抛用户不存在() {
        given(systemUserService.getByUsername("nobody")).willReturn(null);

        ServiceException ex = assertThrows(ServiceException.class,
                () -> userDetailService.loadUserByUsername("nobody"));
        assertThat(ex.getErrorCode()).isEqualTo(SystemErrorEnum.USER_NOT_FOUND);
    }

    @Test
    void loadUserByUsername_用户已禁用_抛用户禁用() {
        given(systemUserService.getByUsername("admin")).willReturn(
                buildUser(1L, "admin", SystemStatusEnum.FORBIDDEN.getCode()));

        ServiceException ex = assertThrows(ServiceException.class,
                () -> userDetailService.loadUserByUsername("admin"));
        assertThat(ex.getErrorCode()).isEqualTo(SystemErrorEnum.USER_FORBIDDEN);
    }

    @Test
    void loadUserByUsername_密码错误超限_抛账户锁定并携带重试次数() {
        SystemUserDO user = buildUser(1L, "admin", SystemStatusEnum.NORMAL.getCode());
        given(systemUserService.getByUsername("admin")).willReturn(user);
        given(redisCache.getCacheObject("password_error_count:1")).willReturn(5);

        ServiceException ex = assertThrows(ServiceException.class,
                () -> userDetailService.loadUserByUsername("admin"));
        assertThat(ex.getErrorCode()).isEqualTo(SystemErrorEnum.USER_LOCKED);
        assertThat(ex.getArgs()).containsExactly(5);
    }

    @Test
    void loadUserByUsername_安全上下文为空_抛未授权() {
        SystemUserDO user = buildUser(1L, "admin", SystemStatusEnum.NORMAL.getCode());
        given(systemUserService.getByUsername("admin")).willReturn(user);
        // SecurityContextHolder 已清空，getAuthentication() 返回 null

        ServiceException ex = assertThrows(ServiceException.class,
                () -> userDetailService.loadUserByUsername("admin"));
        assertThat(ex.getErrorCode()).isEqualTo(SystemErrorEnum.UNAUTHORIZED);
    }

    @Test
    void loadUserByUsername_凭证为空_抛未授权() {
        SystemUserDO user = buildUser(1L, "admin", SystemStatusEnum.NORMAL.getCode());
        given(systemUserService.getByUsername("admin")).willReturn(user);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(null, null));

        ServiceException ex = assertThrows(ServiceException.class,
                () -> userDetailService.loadUserByUsername("admin"));
        assertThat(ex.getErrorCode()).isEqualTo(SystemErrorEnum.UNAUTHORIZED);
    }

    // ==================== 密码错误分支 ====================

    @Test
    void loadUserByUsername_密码错误_无历史计数_初始计数为1() {
        SystemUserDO user = buildUser(1L, "admin", SystemStatusEnum.NORMAL.getCode());
        given(systemUserService.getByUsername("admin")).willReturn(user);
        given(redisCache.getCacheObject("password_error_count:1")).willReturn(null);

        withAuth(WRONG_PASSWORD);

        ServiceException ex = assertThrows(ServiceException.class,
                () -> userDetailService.loadUserByUsername("admin"));
        assertThat(ex.getErrorCode()).isEqualTo(SystemErrorEnum.PASSWORD_ERROR);
        then(redisCache).should().setCacheObject(
                eq("password_error_count:1"), eq(1), eq(30L), any());
    }

    @Test
    void loadUserByUsername_密码错误_历史计数递增() {
        SystemUserDO user = buildUser(1L, "admin", SystemStatusEnum.NORMAL.getCode());
        given(systemUserService.getByUsername("admin")).willReturn(user);
        given(redisCache.getCacheObject("password_error_count:1")).willReturn(3);

        withAuth(WRONG_PASSWORD);

        ServiceException ex = assertThrows(ServiceException.class,
                () -> userDetailService.loadUserByUsername("admin"));
        assertThat(ex.getErrorCode()).isEqualTo(SystemErrorEnum.PASSWORD_ERROR);
        then(redisCache).should().setCacheObject(
                eq("password_error_count:1"), eq(4), eq(30L), any());
    }

    // ==================== 密码正确分支 ====================

    @Test
    void loadUserByUsername_密码正确_存在历史计数_清除缓存() {
        SystemUserDO user = buildUser(1L, "admin", SystemStatusEnum.NORMAL.getCode());
        given(systemUserService.getByUsername("admin")).willReturn(user);
        given(redisCache.getCacheObject("password_error_count:1")).willReturn(2);
        given(redisCache.hasKey("password_error_count:1")).willReturn(true);
        given(systemMenuService.getPermission(1L)).willReturn(Collections.singleton("system:user:list"));

        withAuth(RAW_PASSWORD);

        UserDetails result = userDetailService.loadUserByUsername("admin");

        assertThat(result).isInstanceOf(LoginUserDTO.class);
        LoginUserDTO dto = (LoginUserDTO) result;
        assertThat(dto.getUsername()).isEqualTo("admin");
        assertThat(dto.getUserId()).isEqualTo(1L);
        assertThat(dto.getPermissions()).containsExactly("system:user:list");
        then(redisCache).should().removeCacheObject("password_error_count:1");
        then(dataScopeService).should().resolveDataScope(dto);
    }

    @Test
    void loadUserByUsername_密码正确_无历史计数_不触发删除() {
        SystemUserDO user = buildUser(1L, "admin", SystemStatusEnum.NORMAL.getCode());
        given(systemUserService.getByUsername("admin")).willReturn(user);
        given(redisCache.getCacheObject("password_error_count:1")).willReturn(null);
        given(redisCache.hasKey("password_error_count:1")).willReturn(false);
        given(systemMenuService.getPermission(1L)).willReturn(Collections.emptySet());

        withAuth(RAW_PASSWORD);

        LoginUserDTO result = (LoginUserDTO) userDetailService.loadUserByUsername("admin");

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("admin");
        assertThat(result.getPermissions()).isEmpty();
        then(redisCache).should(never()).removeCacheObject(anyString());
    }

    @Test
    void loadUserByUsername_登录成功_构建用户详情并解析数据权限() {
        SystemUserDO user = buildUser(1L, "admin", SystemStatusEnum.NORMAL.getCode());
        user.setDeptId(20L);
        given(systemUserService.getByUsername("admin")).willReturn(user);
        given(redisCache.getCacheObject("password_error_count:1")).willReturn(null);
        given(redisCache.hasKey("password_error_count:1")).willReturn(false);
        given(systemMenuService.getPermission(1L)).willReturn(
                new HashSet<>(Arrays.asList("system:user:list", "system:role:list")));

        withAuth(RAW_PASSWORD);

        LoginUserDTO dto = (LoginUserDTO) userDetailService.loadUserByUsername("admin");

        assertThat(dto).isNotNull();
        assertThat(dto.getUserId()).isEqualTo(1L);
        assertThat(dto.getUsername()).isEqualTo("admin");
        assertThat(dto.getDeptId()).isEqualTo(20L);
        assertThat(dto.getSystemUserDO()).isSameAs(user);
        assertThat(dto.getPermissions()).containsExactlyInAnyOrder(
                "system:user:list", "system:role:list");
        then(dataScopeService).should().resolveDataScope(dto);
        then(systemMenuService).should().getPermission(1L);
    }

    // ==================== 辅助方法 ====================

    private static SystemUserDO buildUser(Long id, String username, String status) {
        SystemUserDO user = new SystemUserDO();
        user.setId(id);
        user.setUsername(username);
        user.setPassword(ENCODER.encode(RAW_PASSWORD));
        user.setStatus(status);
        user.setTenantId(1L);
        user.setDeptId(10L);
        return user;
    }

    private static void withAuth(String password) {
        Authentication auth = new UsernamePasswordAuthenticationToken(null, password);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}
