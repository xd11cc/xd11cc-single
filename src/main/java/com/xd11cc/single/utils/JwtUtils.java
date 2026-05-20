package com.xd11cc.single.utils;

import com.xd11cc.single.constants.SecurityConstants;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * @Author: xd11cc
 * @Date: 2025/6/16 15:48
 *
 * JWT工具类
 **/
public class JwtUtils {

    private static final SecretKey KEY = Keys.hmacShaKeyFor(
            SecurityConstants.TOKEN_SECRET.getBytes(StandardCharsets.UTF_8)
    );

    public static String createToken(Map<String, Object> claims){
        return Jwts.builder()
                .setClaims(claims)
                .signWith(KEY)
                .compact();
    }

    public static Claims parseToken(String token){
        return Jwts.parserBuilder()
                .setSigningKey(KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
