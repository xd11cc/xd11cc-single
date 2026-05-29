package com.xd11cc.single.utils;

import com.xd11cc.single.config.exception.ServiceException;
import com.xd11cc.single.entity.dto.LoginUserDTO;
import com.xd11cc.single.enums.SystemErrorEnum;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * @author xd11cc
 * @date 2025/6/24 14:20
 */
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
            throw new ServiceException(SystemErrorEnum.UNAUTHORIZED);
        }
    }

    public static Long getUserId() {
        return getLoginUser().getUserId();
    }

    public static String getUsername() {
        return getLoginUser().getUsername();
    }

    public static Long getTenantId() {
        return getLoginUser().getSystemUserDO().getTenantId();
    }

    public static Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }
}
