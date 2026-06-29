package com.xd11cc.single.utils;

import com.xd11cc.single.config.exception.ServiceException;
import com.xd11cc.single.entity.dto.LoginUserDTO;
import com.xd11cc.single.enums.SystemErrorEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * @author xd11cc
 * @date 2025/6/24 14:20
 */
@Slf4j
public class SecurityUtils {

    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder();

    public static boolean matchPassword(String enterPassword, String encodedPassword) {
        return ENCODER.matches(enterPassword, encodedPassword);
    }

    public static String encodePassword(String rawPassword) {
        return ENCODER.encode(rawPassword);
    }

    public static LoginUserDTO getLoginUser() {
        try {
            return (LoginUserDTO) getAuthentication().getPrincipal();
        } catch (Exception e) {
            log.warn("获取当前登录用户失败: {}", e.getMessage());
            return null;
        }
    }

    public static Long getUserId() {
        LoginUserDTO loginUser = getLoginUser();
        return loginUser != null ? loginUser.getUserId() : null;
    }

    public static Long getRequireUserId() {
        LoginUserDTO loginUser = getLoginUser();
        if (loginUser == null || loginUser.getUserId() == null) {
            throw new ServiceException(SystemErrorEnum.UNAUTHORIZED);
        }
        return loginUser.getUserId();
    }

    public static String getUsername() {
        LoginUserDTO loginUser = getLoginUser();
        return loginUser != null ? loginUser.getUsername() : null;
    }

    public static Long getTenantId() {
        LoginUserDTO loginUser = getLoginUser();
        if (loginUser == null || loginUser.getSystemUserDO() == null) {
            return null;
        }
        return loginUser.getSystemUserDO().getTenantId();
    }

    public static Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }
}
