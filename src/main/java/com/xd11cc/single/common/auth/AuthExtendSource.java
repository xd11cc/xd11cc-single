package com.xd11cc.single.common.auth;

import me.zhyd.oauth.cache.AuthStateCache;
import me.zhyd.oauth.config.AuthConfig;
import me.zhyd.oauth.config.AuthDefaultSource;
import me.zhyd.oauth.config.AuthSource;
import me.zhyd.oauth.request.AuthDefaultRequest;
import me.zhyd.oauth.request.AuthGiteeRequest;
import me.zhyd.oauth.request.AuthGithubRequest;
import me.zhyd.oauth.request.AuthRequest;

import java.util.function.BiFunction;

/**
 * 已接入的第三方登录平台。
 * <p>
 * 每个常量同时携带「委托给 JustAuth 的平台配置」和「如何构造请求对象」。
 * 早先只保留配置、靠反射 {@code getConstructor(AuthConfig.class, AuthStateCache.class)}
 * 构造请求，隐患是 JustAuth 升级改动构造签名时编译期无感，只在用户点登录时才抛
 * {@code NoSuchMethodException}。改成函数引用后，签名不匹配会直接编译失败。
 * </p>
 * <p>
 * <b>新增平台</b>：加一个常量（引用其 {@code AuthXxxRequest::new}），
 * 再在 {@code auth_client_config} 表补一条对应租户的配置数据即可，无需改工厂。
 * </p>
 *
 * @author xd11cc
 * @date 2026-05-09 09:25:13
 */
public enum AuthExtendSource implements AuthSource {

    GITHUB(AuthDefaultSource.GITHUB, AuthGithubRequest::new),
    GITEE(AuthDefaultSource.GITEE, AuthGiteeRequest::new),

    ;

    private final AuthSource delegate;

    /**
     * 请求对象构造器：入参是平台配置与 state 缓存
     */
    private final BiFunction<AuthConfig, AuthStateCache, AuthRequest> requestCreator;

    AuthExtendSource(AuthSource delegate, BiFunction<AuthConfig, AuthStateCache, AuthRequest> requestCreator) {
        this.delegate = delegate;
        this.requestCreator = requestCreator;
    }

    /**
     * 按给定配置构造该平台的授权请求。
     *
     * @param config     平台配置
     * @param stateCache state 缓存（OAuth 防 CSRF）
     */
    public AuthRequest create(AuthConfig config, AuthStateCache stateCache) {
        return requestCreator.apply(config, stateCache);
    }

    @Override
    public String authorize() {
        return delegate.authorize();
    }

    @Override
    public String accessToken() {
        return delegate.accessToken();
    }

    @Override
    public String userInfo() {
        return delegate.userInfo();
    }

    @Override
    public Class<? extends AuthDefaultRequest> getTargetClass() {
        return delegate.getTargetClass();
    }
}
