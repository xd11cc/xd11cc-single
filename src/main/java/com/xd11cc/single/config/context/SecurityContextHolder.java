package com.xd11cc.single.config.context;

import org.springframework.security.core.Authentication;

/**
 * @Author: xd11cc
 * @Date: 2025/6/23 21:33
 *
 * security身份验证信息
 **/
public class SecurityContextHolder {

    private static final ThreadLocal<Authentication> THREADLOCAL = new ThreadLocal<>();

    public static Authentication getContext() {
        return THREADLOCAL.get();
    }

    public static void setContext(Authentication authentication) {
        THREADLOCAL.set(authentication);
    }

    public static void clearContext() {
        THREADLOCAL.remove();
    }

}
