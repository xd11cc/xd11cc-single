package com.xd11cc.single.service.impl;

import cn.hutool.http.useragent.UserAgent;
import cn.hutool.http.useragent.UserAgentUtil;
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

import com.xd11cc.single.utils.IpUtils;
import com.xd11cc.single.utils.ServletUtils;

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

    public static String getLoginUserKey(Long userId) {
        return CacheConstants.LOGIN_USER_KEY + userId;
    }

    @Override
    public LoginUserDTO getLoginUser(HttpServletRequest request) {
        String token = request.getHeader(SecurityConstants.AUTHORIZATION);
        if (StringUtils.isNotEmpty(token)) {
            token = token.replace(SecurityConstants.TOKEN_PREFIX, "");
            try {
                Claims claims = JwtUtils.parseToken(token);
                String uuidToken = (String) claims.get(SecurityConstants.LOGIN_USER_KEY);
                return redisCache.getCacheObject(getLoginTokenKey(uuidToken));
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
                Integer tenantId = (Integer) claims.get(SecurityConstants.TENANT_ID);
                return TenantUtils.execute(Long.valueOf(tenantId), ()->{
                    return redisCache.getCacheObject(getLoginTokenKey(uuidToken));
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
    public void removeLoginUser(LoginUserDTO loginUserDTO) {
        if (StringUtils.isNotEmpty(loginUserDTO.getToken())) {
            redisCache.removeCacheObject(getLoginTokenKey(loginUserDTO.getToken()));
            redisCache.removeCacheObject(getLoginUserKey(loginUserDTO.getUserId()));
        }
    }

    @Override
    public String createToken(LoginUserDTO loginUserDTO) {
        String token = IdUtils.fastUUID();
        loginUserDTO.setToken(token);
        loginUserDTO.setLoginTime(System.currentTimeMillis());
        setUserAgent(loginUserDTO);
        refreshToken(loginUserDTO);

        Map<String, Object> claims = new HashMap<>();
        claims.put(SecurityConstants.LOGIN_USER_KEY, token);
        claims.put(SecurityConstants.TENANT_ID, loginUserDTO.getSystemUserDO().getTenantId());
        claims.put(Claims.SUBJECT, loginUserDTO.getUsername());
        return JwtUtils.createToken(claims);
    }

    private void setUserAgent(LoginUserDTO loginUserDTO) {
        try {
            HttpServletRequest request = ServletUtils.getRequest();
            loginUserDTO.setIpAddr(IpUtils.getIpAddr(request));
            String userAgentStr = request.getHeader("User-Agent");
            UserAgent userAgent = UserAgentUtil.parse(userAgentStr);
            if (userAgent != null) {
                loginUserDTO.setBrowser(userAgent.getBrowser().getName());
                loginUserDTO.setOs(userAgent.getOs().getName());
            }
        } catch (Exception ignored) {
        }
    }

    private void refreshToken(LoginUserDTO loginUserDTO) {
        loginUserDTO.setExpireTime(System.currentTimeMillis() + SecurityConstants.EXPIRE_TIME);
        String token = redisCache.getCacheObject(getLoginUserKey(loginUserDTO.getUserId()));
        // 如果是异地登录，将上一个踢下线
        if (StringUtils.isNotEmpty(token) && !token.equals(loginUserDTO.getToken())) {
            redisCache.removeCacheObject(getLoginUserKey(loginUserDTO.getUserId()));
            redisCache.removeCacheObject(getLoginTokenKey(token));
        }
        redisCache.setCacheObject(getLoginTokenKey(loginUserDTO.getToken()), loginUserDTO, SecurityConstants.EXPIRE_TIME, TimeUnit.MILLISECONDS);
        redisCache.setCacheObject(getLoginUserKey(loginUserDTO.getUserId()), loginUserDTO.getToken(), SecurityConstants.EXPIRE_TIME, TimeUnit.MILLISECONDS);
    }
}
