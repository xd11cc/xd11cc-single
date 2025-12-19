package com.xd11cc.single.service.impl;

import com.xd11cc.single.config.RedisCache;
import com.xd11cc.single.constants.CacheConstants;
import com.xd11cc.single.constants.TokenConstants;
import com.xd11cc.single.entity.dto.LoginUserDTO;
import com.xd11cc.single.enums.SingleErrorEnum;
import com.xd11cc.single.exception.ServiceException;
import com.xd11cc.single.service.TokenService;
import com.xd11cc.single.utils.IdUtils;
import com.xd11cc.single.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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

    private String getLoginTokenKey(String uuidToken) {
        return CacheConstants.LOGIN_TOKEN_KEY + uuidToken;
    }

    @Override
    public LoginUserDTO getLoginUser(HttpServletRequest request) {
        String token = request.getHeader(TokenConstants.AUTHORIZATION);
        if (StringUtils.isNotEmpty(token)) {
            token = token.replace(TokenConstants.TOKEN_PREFIX, "");
            try {
                Claims claims = JwtUtils.parseToken(token);
                String uuidToken = (String) claims.get(TokenConstants.LOGIN_USER_KEY);
                String loginTokenKey = getLoginTokenKey(uuidToken);
                return redisCache.getCacheObject(loginTokenKey);
            } catch (Exception e) {
                log.error("获取用户信息异常{}", e.getMessage());
                throw new ServiceException(SingleErrorEnum.UNAUTHORIZED);
            }
        }
        return null;
    }

    @Override
    public void verifyToken(LoginUserDTO loginUserDTO) {
        Long expireTime = loginUserDTO.getExpireTime();
        long currentTime = System.currentTimeMillis();
        if ((expireTime - currentTime) <= TokenConstants.EXPIRE_REFRESH_TOKEN_TIME) {
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
        claims.put(TokenConstants.LOGIN_USER_KEY, token);
        claims.put(Claims.SUBJECT, loginUserDTO.getUsername());
        return JwtUtils.createToken(claims);
    }

    private void refreshToken(LoginUserDTO loginUserDTO) {
        loginUserDTO.setExpireTime(System.currentTimeMillis() + TokenConstants.EXPIRE_TIME);
        String loginTokenKey = getLoginTokenKey(loginUserDTO.getToken());
        redisCache.setCacheObject(loginTokenKey, loginUserDTO, TokenConstants.EXPIRE_TIME, TimeUnit.MILLISECONDS);
    }
}
