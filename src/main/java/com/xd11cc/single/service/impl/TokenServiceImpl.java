package com.xd11cc.single.service.impl;

import com.xd11cc.single.config.RedisCache;
import com.xd11cc.single.constants.CacheConstants;
import com.xd11cc.single.constants.SecurityConstants;
import com.xd11cc.single.entity.domain.SystemUserDO;
import com.xd11cc.single.entity.dto.LoginUserDTO;
import com.xd11cc.single.enums.SystemErrorEnum;
import com.xd11cc.single.config.exception.ServiceException;
import com.xd11cc.single.service.TokenService;
import com.xd11cc.single.utils.IdUtils;
import com.xd11cc.single.utils.JwtUtils;
import com.xd11cc.single.utils.TenantUtils;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import com.xd11cc.single.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @Author: xd11cc
 * @Date: 2025/6/16 09:22
 **/
@Slf4j
@Service
public class TokenServiceImpl implements TokenService {

    @Autowired
    private RedisCache redisCache;

    public static String getLoginTokenKey(String uuidToken) {
        return CacheConstants.LOGIN_TOKEN_KEY + uuidToken;
    }

    @Override
    public LoginUserDTO getLoginUser(HttpServletRequest request) {
        String token = request.getHeader(SecurityConstants.AUTHORIZATION);
        if (StringUtils.isNotEmpty(token)) {
            token = token.replace(SecurityConstants.TOKEN_PREFIX, "");
            try {
                Claims claims = JwtUtils.parseToken(token);
                String uuidToken = (String) claims.get(SecurityConstants.LOGIN_USER_KEY);
                String loginTokenKey = getLoginTokenKey(uuidToken);
                return redisCache.getCacheObject(loginTokenKey);
            } catch (Exception e) {
                log.error("获取用户信息异常{}", e.getMessage());
                throw new ServiceException(SystemErrorEnum.UNAUTHORIZED);
            }
        }
        return null;
    }

    @Override
    public LoginUserDTO getLoginUser(String token) {
        if (StringUtils.isNotEmpty(token)) {
            token = token.replace(SecurityConstants.TOKEN_PREFIX, "");
            try {
                Claims claims = JwtUtils.parseToken(token);
                String uuidToken = (String) claims.get(SecurityConstants.LOGIN_USER_KEY);
                String loginTokenKey = getLoginTokenKey(uuidToken);
                Integer tenantId = (Integer) claims.get(SecurityConstants.TENANT_ID);
                return TenantUtils.execute(Long.valueOf(tenantId), ()->{
                    return redisCache.getCacheObject(loginTokenKey);
                });
            } catch (Exception e) {
                log.error("获取用户信息异常{}", e.getMessage(), e);
                throw new ServiceException(SystemErrorEnum.UNAUTHORIZED);
            }
        }
        return null;
    }

    @Override
    public void verifyToken(LoginUserDTO loginUserDTO) {
        Long expireTime = loginUserDTO.getExpireTime();
        long currentTime = System.currentTimeMillis();
        if ((expireTime - currentTime) <= SecurityConstants.EXPIRE_REFRESH_TOKEN_TIME) {
            refreshToken(loginUserDTO);
        }

    }

    @Override
    public void removeLoginUser(String token) {
        if (StringUtils.isNotEmpty(token)) {
            String loginTokenKey = getLoginTokenKey(token);
            redisCache.removeCacheObject(loginTokenKey);
        }
    }

    @Override
    public String createToken(LoginUserDTO loginUserDTO) {
        String token = IdUtils.fastUUID();
        loginUserDTO.setToken(token);
        loginUserDTO.setLoginTime(System.currentTimeMillis());
        refreshToken(loginUserDTO);

        Map<String, Object> claims = new HashMap<>();
        claims.put(SecurityConstants.LOGIN_USER_KEY, token);
        claims.put(SecurityConstants.TENANT_ID, loginUserDTO.getSystemUserDO().getTenantId());
        claims.put(Claims.SUBJECT, loginUserDTO.getUsername());
        return JwtUtils.createToken(claims);
    }

    private void refreshToken(LoginUserDTO loginUserDTO) {
        loginUserDTO.setExpireTime(System.currentTimeMillis() + SecurityConstants.EXPIRE_TIME);
        String loginTokenKey = getLoginTokenKey(loginUserDTO.getToken());
        redisCache.setCacheObject(loginTokenKey, loginUserDTO, SecurityConstants.EXPIRE_TIME, TimeUnit.MILLISECONDS);
    }
}
