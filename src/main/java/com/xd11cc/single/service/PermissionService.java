package com.xd11cc.single.service;

import com.xd11cc.single.config.context.PermissionContextHolder;
import com.xd11cc.single.constants.PermissionConstants;
import com.xd11cc.single.entity.dto.LoginUserDTO;
import com.xd11cc.single.utils.SecurityUtils;
import com.xd11cc.single.utils.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * @author xd11cc
 * @date 2026-03-16 09:54:52
 * @description ruoyi 自定义权限实现
 */
@Service("ss")
public class PermissionService {

    /**
     * 验证用户是否具备某权限
     * @param permission 权限字符串
     * @return 用户是否具备某权限
     */
    public boolean hasPermission(String permission) {
        if (StringUtils.isEmpty(permission)) {
            return false;
        }
        LoginUserDTO loginUser = SecurityUtils.getLoginUser();
        if (loginUser == null || StringUtils.isEmpty(loginUser.getPermissions())){
            return false;
        }
        PermissionContextHolder.setContext(permission);
        return hasPermissions(loginUser.getPermissions(), permission);
    }

    /**
     * 验证用户是否不具备某权限，与hasPermission逻辑相反
     * @param permission 权限字符串
     * @return 用户是否不具备某权限
     */
    public boolean lacksPermission(String permission) {
        return !hasPermission(permission);
    }

    public boolean hasAnyPermission(String permissions) {
        if (StringUtils.isEmpty(permissions)) {
            return false;
        }
        LoginUserDTO loginUser = SecurityUtils.getLoginUser();
        if (loginUser == null || StringUtils.isEmpty(loginUser.getPermissions())){
            return false;
        }
        PermissionContextHolder.setContext(permissions);
        Set<String> authorities = loginUser.getPermissions();
        for (String permission : permissions.split(PermissionConstants.PERMISSION_DELIMITER)) {
            if (hasPermissions(authorities, permission)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断是否包含权限
     * @param permissions 权限列表
     * @param permission 权限字符串
     * @return 用户是否具备某权限
     */
    private boolean hasPermissions(Set<String> permissions, String permission) {
        return permissions.contains(PermissionConstants.ALL_PERMISSION) || permissions.contains(permission);
    }
}
