package com.xd11cc.single.config.auth;

import me.zhyd.oauth.request.AuthGiteeRequest;
import me.zhyd.oauth.request.AuthGithubRequest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AuthExtendSourceTest {

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
}
