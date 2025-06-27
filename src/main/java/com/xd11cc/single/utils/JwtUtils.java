package com.xd11cc.single.utils;

import com.xd11cc.single.constants.TokenConstants;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Map;

/**
 * @Author: xd11cc
 * @Date: 2025/6/16 15:48
 *
 * JWT工具类
 **/
public class JwtUtils {

    public static String createToken(Map<String, Object> claims){
        return Jwts.builder()
                .setClaims(claims)
                .signWith(SignatureAlgorithm.HS256, TokenConstants.TOKEN_SECRET)
                .compact();
    }

    public static Claims parseToken(String token){
        return Jwts.parser()
                .setSigningKey(TokenConstants.TOKEN_SECRET)
                .parseClaimsJws(token)
                .getBody();
    }
}
