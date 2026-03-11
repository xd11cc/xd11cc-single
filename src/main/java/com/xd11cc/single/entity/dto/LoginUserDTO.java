package com.xd11cc.single.entity.dto;

import com.xd11cc.single.entity.domain.SystemUserDO;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

/**
 * @Author: xd11cc
 * @Date: 2025/6/18 22:20
 *
 * 登录用户信息
 **/
@Data
public class LoginUserDTO implements UserDetails {

    private static final long serialVersionUID = 1L;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 岗位id
     */
    private Long deptId;

    /**
     * 用户唯一标识
     */
    private String token;

    /**
     * 登录时间
     */
    private Long loginTime;

    /**
     * 过期时间
     */
    private Long expireTime;

    /**
     * 登录ip地址
     */
    private String ipAddr;

    /**
     * 登录地址
     */
    private String loginLocation;

    /**
     * 浏览器
     */
    private String browser;

    /**
     * 操作系统
     */
    private String os;

    /**
     * 角色id
     */
    private Set<Long> roleIds;

    /**
     * 权限列表
     */
    private Set<String> permissions;

    /**
     * 用户信息
     */
    private SystemUserDO systemUserDO;

    public LoginUserDTO() {
    }

    public LoginUserDTO(Set<String> permissions, SystemUserDO systemUserDO) {
        this.userId = systemUserDO.getId();
        this.deptId = systemUserDO.getDeptId();
        this.permissions = permissions;
        this.systemUserDO = systemUserDO;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList();
    }

    @Override
    public String getPassword() {
        return systemUserDO.getPassword();
    }

    @Override
    public String getUsername() {
        return systemUserDO.getUsername();
    }

    /**
     * 账户是否未过期，过期无法验证
     * @return
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * 用户是否锁定，锁定无法登陆
     * @return
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * 是否已过期的凭证，过期的无法认证
     * @return
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * 账户是否停用，停用无法使用
     * @return
     */
    @Override
    public boolean isEnabled() {
        return true;
    }
}
