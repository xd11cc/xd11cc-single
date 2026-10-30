package com.xd11cc.single.common.security;

import com.xd11cc.single.common.exception.ServiceException;
import com.xd11cc.single.constants.UserConstants;
import com.xd11cc.single.entity.domain.SystemUserDO;
import com.xd11cc.single.entity.dto.LoginUserDTO;
import com.xd11cc.single.enums.SystemErrorEnum;
import com.xd11cc.single.infrastructure.redis.RedisCache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

/**
 * {@link PasswordAuthenticationProvider} 的测试。
 * <p>
 * 覆盖密码校验与错误计数锁定。这些行为原本长在 {@code UserDetailServiceImpl} 里，
 * 现已归位到认证提供者；错误码与锁定阈值保持与改造前一致。
 * </p>
 *
 * @author xd11cc
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PasswordAuthenticationProviderTest {

    private static final Long USER_ID = 1L;
    private static final String COUNT_KEY = "password_error_count:1";
    private static final String RAW_PASSWORD = "TestPwd123!";

    private static final PasswordEncoder ENCODER = new BCryptPasswordEncoder();

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private RedisCache redisCache;

    private PasswordAuthenticationProvider provider;

    @BeforeEach
    void setUp() {
        provider = new PasswordAuthenticationProvider(userDetailsService, ENCODER, redisCache);
    }

    @Test
    @DisplayName("已锁定：直接拒绝，不再比对密码")
    void additionalAuthenticationChecks_locked() {
        given(redisCache.getCacheObject(COUNT_KEY)).willReturn(UserConstants.PASSWORD_MAX_RETRY_COUNT);

        assertThatThrownBy(() -> provider.additionalAuthenticationChecks(loginUser(RAW_PASSWORD), token("wrong")))
                .isInstanceOf(ServiceException.class)
                .satisfies(e -> {
                    ServiceException se = (ServiceException) e;
                    assertThat(se.getErrorCode()).isEqualTo(SystemErrorEnum.USER_LOCKED);
                    assertThat(se.getArgs()).containsExactly(UserConstants.PASSWORD_MAX_RETRY_COUNT);
                });
    }

    @Test
    @DisplayName("密码错误且无历史计数：计数置 1 并抛 LOGIN_FAILED")
    void additionalAuthenticationChecks_wrongPassword_firstFailure() {
        given(redisCache.getCacheObject(COUNT_KEY)).willReturn(null);

        assertThatThrownBy(() -> provider.additionalAuthenticationChecks(loginUser(RAW_PASSWORD), token("wrong")))
                .isInstanceOf(ServiceException.class)
                .satisfies(e -> assertThat(((ServiceException) e).getErrorCode())
                        .isEqualTo(SystemErrorEnum.LOGIN_FAILED));

        then(redisCache).should().setCacheObject(eq(COUNT_KEY), eq(1), eq(30L), eq(TimeUnit.MINUTES));
    }

    @Test
    @DisplayName("密码错误且已有计数：递增")
    void additionalAuthenticationChecks_wrongPassword_increments() {
        given(redisCache.getCacheObject(COUNT_KEY)).willReturn(3);

        assertThatThrownBy(() -> provider.additionalAuthenticationChecks(loginUser(RAW_PASSWORD), token("wrong")))
                .isInstanceOf(ServiceException.class);

        then(redisCache).should().setCacheObject(eq(COUNT_KEY), eq(4), eq(30L), eq(TimeUnit.MINUTES));
    }

    @Test
    @DisplayName("密码正确但账户已禁用：抛 USER_FORBIDDEN")
    void additionalAuthenticationChecks_correctPassword_butDisabled() {
        given(redisCache.getCacheObject(COUNT_KEY)).willReturn(null);
        LoginUserDTO disabled = loginUser(RAW_PASSWORD);
        disabled.getSystemUserDO().setStatus("1");

        assertThatThrownBy(() -> provider.additionalAuthenticationChecks(disabled, token(RAW_PASSWORD)))
                .isInstanceOf(ServiceException.class)
                .satisfies(e -> assertThat(((ServiceException) e).getErrorCode())
                        .isEqualTo(SystemErrorEnum.USER_FORBIDDEN));
    }

    @Test
    @DisplayName("密码正确：清除历史计数且不抛异常")
    void additionalAuthenticationChecks_correctPassword_clearsCount() {
        given(redisCache.getCacheObject(COUNT_KEY)).willReturn(2);
        given(redisCache.hasKey(COUNT_KEY)).willReturn(true);

        provider.additionalAuthenticationChecks(loginUser(RAW_PASSWORD), token(RAW_PASSWORD));

        then(redisCache).should().removeCacheObject(COUNT_KEY);
        then(redisCache).should(never()).setCacheObject(anyString(), anyInt(), anyLong(), eq(TimeUnit.MINUTES));
    }

    @Test
    @DisplayName("凭证为空：抛 UNAUTHORIZED，且不计入错误次数")
    void additionalAuthenticationChecks_nullCredentials() {
        given(redisCache.getCacheObject(COUNT_KEY)).willReturn(null);

        assertThatThrownBy(() -> provider.additionalAuthenticationChecks(loginUser(RAW_PASSWORD), token(null)))
                .isInstanceOf(ServiceException.class)
                .satisfies(e -> assertThat(((ServiceException) e).getErrorCode())
                        .isEqualTo(SystemErrorEnum.UNAUTHORIZED));

        then(redisCache).should(never()).setCacheObject(anyString(), anyInt(), anyLong(), eq(TimeUnit.MINUTES));
    }

    // ==================== 辅助 ====================

    private static LoginUserDTO loginUser(String rawPassword) {
        SystemUserDO user = new SystemUserDO();
        user.setId(USER_ID);
        user.setUsername("admin");
        user.setPassword(ENCODER.encode(rawPassword));
        user.setTenantId(1L);
        return new LoginUserDTO(java.util.Collections.emptySet(), user);
    }

    private static UsernamePasswordAuthenticationToken token(String credentials) {
        return new UsernamePasswordAuthenticationToken("admin", credentials);
    }
}
