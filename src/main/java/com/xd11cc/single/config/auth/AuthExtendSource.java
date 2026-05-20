package com.xd11cc.single.config.auth;

import me.zhyd.oauth.config.AuthDefaultSource;
import me.zhyd.oauth.config.AuthSource;
import me.zhyd.oauth.request.AuthDefaultRequest;

/**
 * @author xd11cc
 * @date 2026-05-09 09:25:13
 * @description
 */
public enum AuthExtendSource implements AuthSource {

    GITHUB(AuthDefaultSource.GITHUB),
    GITEE(AuthDefaultSource.GITEE),

    ;

    private final AuthSource delegate;

    /**
     * 自定义auth配置构造器
     */
    AuthExtendSource() {
        this.delegate = null;
    }

    /**
     * 套用just auth中的配置构造器
     * @param delegate
     */
    AuthExtendSource(AuthSource delegate) {
        this.delegate = delegate;
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
