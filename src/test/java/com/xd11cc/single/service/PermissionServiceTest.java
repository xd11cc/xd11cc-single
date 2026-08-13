package com.xd11cc.single.service;

import com.xd11cc.single.config.context.PermissionContextHolder;
import com.xd11cc.single.entity.domain.SystemUserDO;
import com.xd11cc.single.entity.dto.LoginUserDTO;
import com.xd11cc.single.util.BaseUnitTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.mock;
import static org.mockito.BDDMockito.never;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mockStatic;

@ExtendWith(MockitoExtension.class)
class PermissionServiceTest extends BaseUnitTest {

    @Mock
    private ISystemUserService systemUserService;

    @InjectMocks
    private PermissionService permissionService;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // ==================== hasPermission ====================

    @Test
    void hasPermission_权限字符串为空_返回false() {
        assertThat(permissionService.hasPermission("")).isFalse();
        assertThat(permissionService.hasPermission(null)).isFalse();
        assertThat(permissionService.hasPermission("   ")).isFalse();
    }

    @Test
    void hasPermission_无登录用户_返回false() {
        // SecurityContextHolder 已清空，无认证信息
        assertThat(permissionService.hasPermission("system:user:list")).isFalse();
    }

    @Test
    void hasPermission_权限集合为空_返回false() {
        LoginUserDTO loginUser = buildLoginUser(1L, Collections.emptySet());
        setAuth(loginUser);

        assertThat(permissionService.hasPermission("system:user:list")).isFalse();
    }

    @Test
    void hasPermission_具备指定权限_返回true() {
        LoginUserDTO loginUser = buildLoginUser(1L,
                new HashSet<>(Arrays.asList("system:user:list", "system:role:list")));
        setAuth(loginUser);

        try (MockedStatic<PermissionContextHolder> pc = mockStatic(PermissionContextHolder.class)) {
            assertThat(permissionService.hasPermission("system:user:list")).isTrue();
            pc.verify(() -> PermissionContextHolder.setContext("system:user:list"));
        }
    }

    @Test
    void hasPermission_具备通配符权限_返回true() {
        LoginUserDTO loginUser = buildLoginUser(1L,
                new HashSet<>(Collections.singleton("*:*:*")));
        setAuth(loginUser);

        try (MockedStatic<PermissionContextHolder> pc = mockStatic(PermissionContextHolder.class)) {
            assertThat(permissionService.hasPermission("system:user:list")).isTrue();
            pc.verify(() -> PermissionContextHolder.setContext("system:user:list"));
        }
    }

    @Test
    void hasPermission_不具备权限_返回false() {
        LoginUserDTO loginUser = buildLoginUser(1L,
                new HashSet<>(Collections.singleton("system:role:list")));
        setAuth(loginUser);

        try (MockedStatic<PermissionContextHolder> pc = mockStatic(PermissionContextHolder.class)) {
            assertThat(permissionService.hasPermission("system:user:list")).isFalse();
            pc.verify(() -> PermissionContextHolder.setContext("system:user:list"));
        }
    }

    // ==================== lacksPermission ====================

    @Test
    void lacksPermission_具备权限_返回false() {
        LoginUserDTO loginUser = buildLoginUser(1L,
                new HashSet<>(Collections.singleton("system:user:list")));
        setAuth(loginUser);

        try (MockedStatic<PermissionContextHolder> pc = mockStatic(PermissionContextHolder.class)) {
            assertThat(permissionService.lacksPermission("system:user:list")).isFalse();
            pc.verify(() -> PermissionContextHolder.setContext("system:user:list"));
        }
    }

    @Test
    void lacksPermission_不具备权限_返回true() {
        LoginUserDTO loginUser = buildLoginUser(1L,
                new HashSet<>(Collections.singleton("system:role:list")));
        setAuth(loginUser);

        try (MockedStatic<PermissionContextHolder> pc = mockStatic(PermissionContextHolder.class)) {
            assertThat(permissionService.lacksPermission("system:user:list")).isTrue();
            pc.verify(() -> PermissionContextHolder.setContext("system:user:list"));
        }
    }

    // ==================== hasAnyPermission ====================

    @Test
    void hasAnyPermission_字符串为空_返回false() {
        assertThat(permissionService.hasAnyPermission("")).isFalse();
        assertThat(permissionService.hasAnyPermission(null)).isFalse();
    }

    @Test
    void hasAnyPermission_无登录用户_返回false() {
        assertThat(permissionService.hasAnyPermission("system:user:list,system:role:list")).isFalse();
    }

    @Test
    void hasAnyPermission_多个权限有一个匹配_返回true() {
        LoginUserDTO loginUser = buildLoginUser(1L,
                new HashSet<>(Collections.singleton("system:user:list")));
        setAuth(loginUser);

        try (MockedStatic<PermissionContextHolder> pc = mockStatic(PermissionContextHolder.class)) {
            assertThat(permissionService.hasAnyPermission("system:user:list,system:role:list")).isTrue();
            pc.verify(() -> PermissionContextHolder.setContext(
                    "system:user:list,system:role:list"));
        }
    }

    @Test
    void hasAnyPermission_多个权限都不匹配_返回false() {
        LoginUserDTO loginUser = buildLoginUser(1L,
                new HashSet<>(Collections.singleton("system:dict:list")));
        setAuth(loginUser);

        try (MockedStatic<PermissionContextHolder> pc = mockStatic(PermissionContextHolder.class)) {
            assertThat(permissionService.hasAnyPermission("system:user:list,system:role:list")).isFalse();
            pc.verify(() -> PermissionContextHolder.setContext(
                    "system:user:list,system:role:list"));
        }
    }

    // ==================== 辅助方法 ====================

    private static LoginUserDTO buildLoginUser(Long userId, Set<String> permissions) {
        SystemUserDO userDO = new SystemUserDO();
        userDO.setId(userId);
        userDO.setUsername("user" + userId);
        userDO.setTenantId(1L);
        userDO.setDeptId(10L);
        return new LoginUserDTO(permissions, userDO);
    }

    private static void setAuth(LoginUserDTO loginUser) {
        Authentication auth = new UsernamePasswordAuthenticationToken(loginUser, null);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}
