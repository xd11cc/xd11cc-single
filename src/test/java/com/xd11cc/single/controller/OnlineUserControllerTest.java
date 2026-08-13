package com.xd11cc.single.controller;

import com.xd11cc.single.config.RedisCache;
import com.xd11cc.single.config.context.TenantContextHolder;
import com.xd11cc.single.constants.CacheConstants;
import com.xd11cc.single.entity.base.ResponseVO;
import com.xd11cc.single.entity.domain.SystemUserDO;
import com.xd11cc.single.entity.dto.LoginUserDTO;
import com.xd11cc.single.entity.vo.OnlineUserVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mockStatic;

@ExtendWith(MockitoExtension.class)
class OnlineUserControllerTest {

    @Mock
    private RedisCache redisCache;

    @InjectMocks
    private OnlineUserController onlineUserController;

    // ==================== list ====================

    @Test
    void list_无登录用户_返回空列表() {
        given(redisCache.keys(CacheConstants.LOGIN_TOKEN_KEY)).willReturn(Collections.emptySet());

        ResponseVO<List<OnlineUserVO>> result = onlineUserController.list(null);

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData()).isEmpty();
    }

    @Test
    void list_查询所有在线用户_返回在线用户列表() {
        try (MockedStatic<TenantContextHolder> tenantMock = mockStatic(TenantContextHolder.class)) {
            tenantMock.when(TenantContextHolder::getTenantId).thenReturn(1L);
            tenantMock.when(() -> TenantContextHolder.<LoginUserDTO>runWithoutTenantAwareness(any(Supplier.class)))
                    .thenAnswer(invocation -> ((Supplier<?>) invocation.getArgument(0)).get());

            given(redisCache.keys(CacheConstants.LOGIN_TOKEN_KEY)).willReturn(new HashSet<>(Arrays.asList("token-1", "token-2")));

            // user2 登录时间更晚，排序后应排在前面
            LoginUserDTO loginUser1 = buildLoginUser(1L, "user1", "token-1", 1L, 1000000L);
            LoginUserDTO loginUser2 = buildLoginUser(2L, "user2", "token-2", 1L, 2000000L);
            given(redisCache.getCacheObject("token-1")).willReturn(loginUser1);
            given(redisCache.getCacheObject("token-2")).willReturn(loginUser2);

            ResponseVO<List<OnlineUserVO>> result = onlineUserController.list(null);

            assertThat(result.getCode()).isEqualTo(200);
            assertThat(result.getData()).hasSize(2);
            assertThat(result.getData()).extracting(OnlineUserVO::getUsername)
                    .containsExactly("user2", "user1");
        }
    }

    @Test
    void list_按用户名过滤_返回匹配用户() {
        try (MockedStatic<TenantContextHolder> tenantMock = mockStatic(TenantContextHolder.class)) {
            tenantMock.when(TenantContextHolder::getTenantId).thenReturn(1L);
            tenantMock.when(() -> TenantContextHolder.<LoginUserDTO>runWithoutTenantAwareness(any(Supplier.class)))
                    .thenAnswer(invocation -> ((Supplier<?>) invocation.getArgument(0)).get());

            given(redisCache.keys(CacheConstants.LOGIN_TOKEN_KEY)).willReturn(new HashSet<>(Collections.singletonList("token-1")));

            LoginUserDTO loginUser = buildLoginUser(1L, "admin", "token-1", 1L);
            given(redisCache.getCacheObject("token-1")).willReturn(loginUser);

            ResponseVO<List<OnlineUserVO>> result = onlineUserController.list("ad");

            assertThat(result.getCode()).isEqualTo(200);
            assertThat(result.getData()).hasSize(1);
            assertThat(result.getData().get(0).getUsername()).isEqualTo("admin");
        }
    }

    @Test
    void list_跨租户过滤_排除其他租户用户() {
        try (MockedStatic<TenantContextHolder> tenantMock = mockStatic(TenantContextHolder.class)) {
            tenantMock.when(TenantContextHolder::getTenantId).thenReturn(1L);
            tenantMock.when(() -> TenantContextHolder.<LoginUserDTO>runWithoutTenantAwareness(any(Supplier.class)))
                    .thenAnswer(invocation -> ((Supplier<?>) invocation.getArgument(0)).get());

            given(redisCache.keys(CacheConstants.LOGIN_TOKEN_KEY)).willReturn(new HashSet<>(Collections.singletonList("token-1")));

            // 用户属于租户2，但当前租户是1
            LoginUserDTO loginUser = buildLoginUser(1L, "other_tenant_user", "token-1", 2L);
            given(redisCache.getCacheObject("token-1")).willReturn(loginUser);

            ResponseVO<List<OnlineUserVO>> result = onlineUserController.list(null);

            assertThat(result.getCode()).isEqualTo(200);
            assertThat(result.getData()).isEmpty();
        }
    }

    // ==================== forceLogout ====================

    @Test
    void forceLogout_用户不存在_返回成功() {
        given(redisCache.getCacheObject(CacheConstants.LOGIN_TOKEN_KEY + "token-1")).willReturn(null);

        ResponseVO<Void> result = onlineUserController.forceLogout("token-1");

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData()).isNull();
    }

    @Test
    void forceLogout_同租户_强制退出成功() {
        try (MockedStatic<TenantContextHolder> tenantMock = mockStatic(TenantContextHolder.class)) {
            tenantMock.when(TenantContextHolder::getTenantId).thenReturn(1L);

            LoginUserDTO loginUser = buildLoginUser(1L, "admin", "token-1", 1L);
            SystemUserDO userDO = loginUser.getSystemUserDO();
            given(redisCache.getCacheObject(CacheConstants.LOGIN_TOKEN_KEY + "token-1")).willReturn(loginUser);

            ResponseVO<Void> result = onlineUserController.forceLogout("token-1");

            assertThat(result.getCode()).isEqualTo(200);
            assertThat(result.getData()).isNull();
        }
    }

    // ==================== 辅助方法 ====================

    private static LoginUserDTO buildLoginUser(Long userId, String username, String token, Long tenantId) {
        return buildLoginUser(userId, username, token, tenantId, System.currentTimeMillis());
    }

    private static LoginUserDTO buildLoginUser(Long userId, String username, String token, Long tenantId, long loginTime) {
        LoginUserDTO dto = new LoginUserDTO();
        dto.setUserId(userId);
        dto.setToken(token);
        dto.setLoginTime(loginTime);
        dto.setIpAddr("127.0.0.1");
        dto.setBrowser("Chrome");
        dto.setOs("MacOS");

        SystemUserDO userDO = new SystemUserDO();
        userDO.setId(userId);
        userDO.setUsername(username);
        userDO.setTenantId(tenantId);
        dto.setSystemUserDO(userDO);

        return dto;
    }
}
