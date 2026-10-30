package com.xd11cc.single.common.auth;


import cn.hutool.core.util.EnumUtil;
import cn.hutool.core.util.StrUtil;
import com.xd11cc.single.common.exception.ServiceException;
import com.xd11cc.single.entity.domain.AuthClientConfigDO;
import com.xd11cc.single.enums.SystemErrorEnum;
import com.xd11cc.single.enums.SystemStatusEnum;
import com.xd11cc.single.service.IAuthClientConfigService;
import me.zhyd.oauth.cache.AuthStateCache;
import me.zhyd.oauth.config.AuthConfig;
import me.zhyd.oauth.enums.AuthResponseStatus;
import me.zhyd.oauth.exception.AuthException;
import me.zhyd.oauth.request.*;
import org.springframework.stereotype.Component;


/**
 * @author xd11cc
 * @date 2026-04-29 13:39:09
 * @description JustAuth动态工厂
 */
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
        // 构造方式由枚举常量自带（函数引用），不再反射
        return source.create(config, this.authStateCache);
    }

    /**
     * 获取认证配置
     * @param source
     * @return
     */
    private AuthConfig getAuthConfig(String source) {
        AuthClientConfigDO authClientConfigDO = authClientConfigService.getBySource(source);
        if (authClientConfigDO == null) {
            return null;
        }

        // 校验该配置是否被禁用
        if (SystemStatusEnum.FORBIDDEN.getCode().equals(authClientConfigDO.getStatus())) {
            throw new ServiceException(SystemErrorEnum.AUTH_SOURCE_FORBIDDEN, new Object[]{source});
        }

        return AuthConfig.builder()
                .clientId(authClientConfigDO.getClientId())
                .clientSecret(authClientConfigDO.getClientSecret())
                .redirectUri(authClientConfigDO.getRedirectUri())
                .build();
    }

}
