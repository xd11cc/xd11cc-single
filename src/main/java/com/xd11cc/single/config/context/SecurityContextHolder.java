package com.xd11cc.single.config.context;

import com.xd11cc.single.entity.dto.LoginUserDTO;

/**
 * @Author: xd11cc
 * @Date: 2025/6/23 21:33
 *
 * security身份验证信息
 **/
public class SecurityContextHolder {

    private static final ThreadLocal<LoginUserDTO> THREADLOCAL = new ThreadLocal<>();

    public static LoginUserDTO getContext() {
        return THREADLOCAL.get();
    }

    public static void setContext(LoginUserDTO loginUserDTO) {
        THREADLOCAL.set(loginUserDTO);
    }

    public static void clearContext() {
        THREADLOCAL.remove();
    }
}
