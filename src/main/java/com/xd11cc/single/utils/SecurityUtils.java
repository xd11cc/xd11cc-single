package com.xd11cc.single.utils;

import com.xd11cc.single.entity.dto.LoginUserDTO;
import com.xd11cc.single.enums.SystemErrorEnum;
import com.xd11cc.single.config.exception.ServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * @Author: xd11cc
 * @Date: 2025/6/24 14:20
 *
 * security工具类
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

    /**
     * 获取登录用户信息
     * @return
     */
    public static LoginUserDTO getLoginUser() {
        try {
            return (LoginUserDTO) getAuthentication().getPrincipal();
        } catch (Exception e) {
            throw new ServiceException(SystemErrorEnum.UNAUTHORIZED);
        }
    }

    /**
     * 获取登录用户id
     * @return
     */
    public static Long getUserId() {
        return getLoginUser().getUserId();
    }

    /**
     * 获取Authentication
     * @return
     */
    public static Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }
}
