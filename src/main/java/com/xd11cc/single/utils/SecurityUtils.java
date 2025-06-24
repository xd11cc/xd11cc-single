package com.xd11cc.single.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * @Author: xd11cc
 * @Date: 2025/6/24 14:20
 **/
public class SecurityUtils {

    /**
     * 校验密码是否正确
     * @param enterPassword
     * @param encodedPassword
     * @return
     */
    public static boolean matchPassword(String enterPassword, String encodedPassword) {
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        return bCryptPasswordEncoder.matches(enterPassword, encodedPassword);
    }
}
