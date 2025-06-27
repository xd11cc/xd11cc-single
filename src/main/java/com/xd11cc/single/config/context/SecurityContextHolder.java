package com.xd11cc.single.config.context;

import org.springframework.security.core.Authentication;

/**
 * @Author: xd11cc
 * @Date: 2025/6/23 21:33
 *
 * security身份验证信息
 **/
public class SecurityContextHolder {

    private static final ThreadLocal<Authentication> CONTEXT_HOLDER = new ThreadLocal<>();

    public static Authentication getContext() {
        return CONTEXT_HOLDER.get();
    }

    public static void setContext(Authentication authentication) {
        CONTEXT_HOLDER.set(authentication);
    }

    public static void clearContext() {
        CONTEXT_HOLDER.remove();
    }

}
