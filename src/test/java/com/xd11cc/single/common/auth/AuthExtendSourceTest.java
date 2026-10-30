package com.xd11cc.single.common.auth;

import me.zhyd.oauth.cache.AuthStateCache;
import me.zhyd.oauth.config.AuthConfig;
import me.zhyd.oauth.request.AuthGiteeRequest;
import me.zhyd.oauth.request.AuthGithubRequest;
import me.zhyd.oauth.request.AuthRequest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AuthExtendSourceTest {

    private final AuthStateCache authStateCache = new AuthStateCache() {
        @Override
        public void cache(String key, String value) {
        }

        @Override
        public void cache(String key, String value, long timeout) {
        }

        @Override
        public String get(String key) {
            return null;
        }

        @Override
        public boolean containsKey(String key) {
            return false;
        }
    };

    // ==================== GITHUB ====================

    @Test
    void github_委托给AuthDefaultSource_GITHUB() {
        assertThat(AuthExtendSource.GITHUB.getTargetClass()).isEqualTo(AuthGithubRequest.class);
    }

    @Test
    void github_authorize_委托() {
        assertThat(AuthExtendSource.GITHUB.authorize()).isEqualTo(
                me.zhyd.oauth.config.AuthDefaultSource.GITHUB.authorize());
    }

    @Test
    void github_accessToken_委托() {
        assertThat(AuthExtendSource.GITHUB.accessToken()).isEqualTo(
                me.zhyd.oauth.config.AuthDefaultSource.GITHUB.accessToken());
    }

    @Test
    void github_userInfo_委托() {
        assertThat(AuthExtendSource.GITHUB.userInfo()).isEqualTo(
                me.zhyd.oauth.config.AuthDefaultSource.GITHUB.userInfo());
    }

    // ==================== GITEE ====================

    @Test
    void gitee_委托给AuthDefaultSource_GITEE() {
        assertThat(AuthExtendSource.GITEE.getTargetClass()).isEqualTo(AuthGiteeRequest.class);
    }

    @Test
    void gitee_authorize_委托() {
        assertThat(AuthExtendSource.GITEE.authorize()).isEqualTo(
                me.zhyd.oauth.config.AuthDefaultSource.GITEE.authorize());
    }

    @Test
    void gitee_accessToken_委托() {
        assertThat(AuthExtendSource.GITEE.accessToken()).isEqualTo(
                me.zhyd.oauth.config.AuthDefaultSource.GITEE.accessToken());
    }

    @Test
    void gitee_userInfo_委托() {
        assertThat(AuthExtendSource.GITEE.userInfo()).isEqualTo(
                me.zhyd.oauth.config.AuthDefaultSource.GITEE.userInfo());
    }
    // ==================== create：函数引用构造 ====================

    @Test
    void create_返回对应平台的请求对象() {
        AuthConfig config = AuthConfig.builder()
                .clientId("id").clientSecret("secret").redirectUri("http://localhost/cb").build();

        AuthRequest request = AuthExtendSource.GITHUB.create(config, authStateCache);

        assertThat(request).isInstanceOf(AuthGithubRequest.class);
    }

    @Test
    void create_所有已接入平台都能构造() {
        AuthConfig config = AuthConfig.builder()
                .clientId("id").clientSecret("secret").redirectUri("http://localhost/cb").build();

        for (AuthExtendSource source : AuthExtendSource.values()) {
            AuthRequest request = source.create(config, authStateCache);
            assertThat(request)
                    .as("平台 %s 应能构造请求对象（JustAuth 升级改签名时这里会先失败）", source.name())
                    .isNotNull();
        }
    }
}
