package com.xd11cc.single.utils;

import com.xd11cc.single.config.context.TenantContextHolder;
import com.xd11cc.single.entity.domain.SystemUserDO;
import com.xd11cc.single.entity.dto.LoginUserDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SecurityUtilsTest {

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        TenantContextHolder.clear();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        TenantContextHolder.clear();
    }

    private void mockLoginUser(Long userId, String username, Long tenantId) {
        SystemUserDO user = new SystemUserDO();
        user.setId(userId);
        user.setUsername(username);
        user.setTenantId(tenantId);

        LoginUserDTO loginUser = new LoginUserDTO();
        loginUser.setUserId(userId);
        loginUser.setSystemUserDO(user);

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(loginUser, null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void encodePassword_明文密码_返回BCrypt编码() {
        String raw = "mySecret123";
        String encoded = SecurityUtils.encodePassword(raw);
        assertThat(encoded).isNotEqualTo(raw);
        assertThat(encoded).startsWith("$2a$");
    }

    @Test
    void encodePassword_相同明文_每次编码不同() {
        String raw = "mySecret123";
        String e1 = SecurityUtils.encodePassword(raw);
        String e2 = SecurityUtils.encodePassword(raw);
        assertThat(e1).isNotEqualTo(e2);
    }

    @Test
    void matchPassword_正确密码_匹配成功() {
        String raw = "mySecret123";
        String encoded = SecurityUtils.encodePassword(raw);
        assertThat(SecurityUtils.matchPassword(raw, encoded)).isTrue();
    }

    @Test
    void matchPassword_错误密码_匹配失败() {
        String raw = "mySecret123";
        String encoded = SecurityUtils.encodePassword(raw);
        assertThat(SecurityUtils.matchPassword("wrong", encoded)).isFalse();
    }

    @Test
    void getLoginUser_已登录_返回LoginUserDTO() {
        mockLoginUser(1L, "admin", 10L);
        assertThat(SecurityUtils.getLoginUser()).isNotNull();
        assertThat(SecurityUtils.getLoginUser().getUsername()).isEqualTo("admin");
    }

    @Test
    void getLoginUser_未登录_返回null() {
        assertThat(SecurityUtils.getLoginUser()).isNull();
    }

    @Test
    void getUserId_已登录_返回用户ID() {
        mockLoginUser(42L, "tester", 20L);
        assertThat(SecurityUtils.getUserId()).isEqualTo(42L);
    }

    @Test
    void getUserId_未登录_返回null() {
        assertThat(SecurityUtils.getUserId()).isNull();
    }

    @Test
    void getUsername_已登录_返回用户名() {
        mockLoginUser(1L, "alice", 10L);
        assertThat(SecurityUtils.getUsername()).isEqualTo("alice");
    }

    @Test
    void getUsername_未登录_返回null() {
        assertThat(SecurityUtils.getUsername()).isNull();
    }

    @Test
    void getRequireUserId_已登录_返回用户ID() {
        mockLoginUser(7L, "required", 10L);
        assertThat(SecurityUtils.getRequireUserId()).isEqualTo(7L);
    }

    @Test
    void getRequireUserId_未登录_抛ServiceException() {
        com.xd11cc.single.config.exception.ServiceException ex = assertThrows(
                com.xd11cc.single.config.exception.ServiceException.class,
                SecurityUtils::getRequireUserId
        );
        assertThat(ex.getErrorCode().getErrorCode()).isEqualTo(401);
    }

    @Test
    void getTenantId_已登录_返回租户ID() {
        mockLoginUser(1L, "admin", 99L);
        assertThat(SecurityUtils.getTenantId()).isEqualTo(99L);
    }

    @Test
    void getTenantId_未登录_返回null() {
        assertThat(SecurityUtils.getTenantId()).isNull();
    }
}
