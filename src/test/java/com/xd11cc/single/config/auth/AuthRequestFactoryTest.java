package com.xd11cc.single.config.auth;

import com.xd11cc.single.config.exception.ServiceException;
import com.xd11cc.single.entity.domain.AuthClientConfigDO;
import com.xd11cc.single.enums.SystemErrorEnum;
import com.xd11cc.single.enums.SystemStatusEnum;
import com.xd11cc.single.service.IAuthClientConfigService;
import me.zhyd.oauth.exception.AuthException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class AuthRequestFactoryTest {

    @Mock
    private IAuthClientConfigService authClientConfigService;

    @Mock
    private RedisAuthStateCache authStateCache;

    @InjectMocks
    private AuthRequestFactory authRequestFactory;

    // ==================== get 空白source ====================

    @Test
    void get_空白source_抛AuthException() {
        assertThatThrownBy(() -> authRequestFactory.get(""))
                .isInstanceOf(AuthException.class);
    }

    @Test
    void get_空白source_空格_抛AuthException() {
        assertThatThrownBy(() -> authRequestFactory.get("   "))
                .isInstanceOf(AuthException.class);
    }

    @Test
    void get_nullSource_抛AuthException() {
        assertThatThrownBy(() -> authRequestFactory.get(null))
                .isInstanceOf(AuthException.class);
    }

    // ==================== get 不支持的source ====================

    @Test
    void get_不支持的source_抛SOCIAL_AUTH_NOT_SUPPORT() {
        assertThatThrownBy(() -> authRequestFactory.get("TWITTER"))
                .isInstanceOf(ServiceException.class)
                .satisfies(ex -> {
                    ServiceException se = (ServiceException) ex;
                    assertThat(se.getErrorCode()).isEqualTo(SystemErrorEnum.SOCIAL_AUTH_NOT_SUPPORT);
                });
    }

    // ==================== get 已停用的source ====================

    @Test
    void get_停用配置_抛AUTH_SOURCE_FORBIDDEN() {
        AuthClientConfigDO config = new AuthClientConfigDO();
        config.setSource("GITHUB");
        config.setClientId("cid");
        config.setClientSecret("secret");
        config.setRedirectUri("https://example.com/callback");
        config.setStatus(SystemStatusEnum.FORBIDDEN.getCode());

        given(authClientConfigService.getBySource("GITHUB")).willReturn(config);

        assertThatThrownBy(() -> authRequestFactory.get("GITHUB"))
                .isInstanceOf(ServiceException.class)
                .satisfies(ex -> {
                    ServiceException se = (ServiceException) ex;
                    assertThat(se.getErrorCode().getErrorCode())
                            .isEqualTo(SystemErrorEnum.AUTH_SOURCE_FORBIDDEN.getErrorCode());
                });
    }

    // ==================== get 不存在配置 ====================

    @Test
    void get_配置不存在_抛SOCIAL_AUTH_NOT_SUPPORT() {
        given(authClientConfigService.getBySource("GITHUB")).willReturn(null);

        assertThatThrownBy(() -> authRequestFactory.get("GITHUB"))
                .isInstanceOf(ServiceException.class)
                .satisfies(ex -> {
                    ServiceException se = (ServiceException) ex;
                    assertThat(se.getErrorCode()).isEqualTo(SystemErrorEnum.SOCIAL_AUTH_NOT_SUPPORT);
                });
    }
}
