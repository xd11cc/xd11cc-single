package com.xd11cc.single.config.auth;


import cn.hutool.core.util.EnumUtil;
import cn.hutool.core.util.StrUtil;
import com.xd11cc.single.config.exception.ServiceException;
import com.xd11cc.single.entity.domain.AuthClientConfigDO;
import com.xd11cc.single.enums.SystemErrorEnum;
import com.xd11cc.single.enums.SystemStatusEnum;
import com.xd11cc.single.service.IAuthClientConfigService;
import lombok.extern.slf4j.Slf4j;
import me.zhyd.oauth.cache.AuthStateCache;
import me.zhyd.oauth.config.AuthConfig;
import me.zhyd.oauth.enums.AuthResponseStatus;
import me.zhyd.oauth.exception.AuthException;
import me.zhyd.oauth.request.*;
import org.springframework.stereotype.Component;

import java.lang.reflect.Constructor;

/**
 * @author xd11cc
 * @date 2026-04-29 13:39:09
 * @description JustAuth动态工厂
 */
@Slf4j
@Component
public class AuthRequestFactory {

    private final RedisAuthStateCache authStateCache;
    private final IAuthClientConfigService authClientConfigService;

    public AuthRequestFactory(RedisAuthStateCache authStateCache,
                              IAuthClientConfigService authClientConfigService) {
        this.authStateCache = authStateCache;
        this.authClientConfigService = authClientConfigService;
    }

    public AuthRequest get(String source) {
        if (StrUtil.isBlank(source)) {
            throw new AuthException(AuthResponseStatus.NO_AUTH_SOURCE);
        }

        AuthExtendSource authSource;
        try {
            authSource = EnumUtil.fromString(AuthExtendSource.class, source.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ServiceException(SystemErrorEnum.SOCIAL_AUTH_NOT_SUPPORT);
        }

        AuthConfig config = getAuthConfig(authSource.name());
        if (config == null) {
            throw new ServiceException(SystemErrorEnum.SOCIAL_AUTH_NOT_SUPPORT);
        }

        return buildRequest(authSource, config);
    }

    /**
     * 构建授权请求
     * @param source
     * @param config
     * @return
     */
    private AuthRequest buildRequest(AuthExtendSource source, AuthConfig config) {
        Class<? extends AuthDefaultRequest> targetClass = source.getTargetClass();
        try {
            Constructor<? extends AuthDefaultRequest> constructor = targetClass.getConstructor(AuthConfig.class, AuthStateCache.class);
            return constructor.newInstance(config, this.authStateCache);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new AuthException("创建认证请求失败: " + source.name());
        }
    }

    /**
     * 获取认证配置
     * @param source
     * @return
     */
    private AuthConfig getAuthConfig(String source) {
        AuthClientConfigDO authClientConfigDO = authClientConfigService.getBySource(source);

        // 校验该配置是否被禁用
        if (null != authClientConfigDO &&
                SystemStatusEnum.FORBIDDEN.getCode().equals(authClientConfigDO.getStatus())) {
            throw new ServiceException(SystemErrorEnum.AUTH_SOURCE_FORBIDDEN, new Object[]{source});
        }

        return AuthConfig.builder()
                .clientId(authClientConfigDO.getClientId())
                .clientSecret(authClientConfigDO.getClientSecret())
                .redirectUri(String.format(authClientConfigDO.getRedirectUri(), authClientConfigDO.getTenantId()))
                .build();
    }

}
