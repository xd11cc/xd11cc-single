package com.xd11cc.single.service.impl;

import com.xd11cc.single.common.exception.ServiceException;
import com.xd11cc.single.entity.domain.SystemUserDO;
import com.xd11cc.single.entity.dto.LoginUserDTO;
import com.xd11cc.single.enums.SystemErrorEnum;
import com.xd11cc.single.enums.SystemStatusEnum;
import com.xd11cc.single.service.DataScopeService;
import com.xd11cc.single.service.ISystemMenuService;
import com.xd11cc.single.service.ISystemUserService;
import com.xd11cc.single.util.BaseUnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

/**
 * {@link UserDetailServiceImpl} 的测试。
 * <p>
 * 本类只负责「加载用户」：查用户、校验状态、装配权限与数据域。
 * 密码比对与错误次数锁定在 {@code PasswordAuthenticationProvider}，由那张测试覆盖。
 * </p>
 *
 * @author xd11cc
 */
@ExtendWith(MockitoExtension.class)
class UserDetailServiceImplTest extends BaseUnitTest {

    @Mock
    private ISystemUserService systemUserService;
    @Mock
    private ISystemMenuService systemMenuService;
    @Mock
    private DataScopeService dataScopeService;

    @InjectMocks
    private UserDetailServiceImpl userDetailService;

    @Test
    @DisplayName("用户不存在：抛统一的 LOGIN_FAILED，不暴露用户名是否存在")
    void loadUserByUsername_用户不存在() {
        given(systemUserService.getByUsername("nobody")).willReturn(null);

        assertThatThrownBy(() -> userDetailService.loadUserByUsername("nobody"))
                .isInstanceOf(ServiceException.class)
                .satisfies(e -> assertThat(((ServiceException) e).getErrorCode())
                        .isEqualTo(SystemErrorEnum.LOGIN_FAILED));
    }

    @Test
    @DisplayName("禁用用户也照常返回用户详情，状态校验延后到密码通过之后")
    void loadUserByUsername_禁用用户_不在本层拦截() {
        SystemUserDO user = buildUser(1L, "admin", SystemStatusEnum.FORBIDDEN.getCode());
        given(systemUserService.getByUsername("admin")).willReturn(user);

        LoginUserDTO dto = (LoginUserDTO) userDetailService.loadUserByUsername("admin");

        assertThat(dto.getSystemUserDO().getStatus()).isEqualTo(SystemStatusEnum.FORBIDDEN.getCode());
    }

    @Test
    @DisplayName("正常用户：装配权限并解析数据权限")
    void loadUserByUsername_成功构建用户详情() {
        SystemUserDO user = buildUser(1L, "admin", SystemStatusEnum.NORMAL.getCode());
        user.setDeptId(20L);
        given(systemUserService.getByUsername("admin")).willReturn(user);
        given(systemMenuService.getPermission(1L)).willReturn(
                new HashSet<>(Arrays.asList("system:user:list", "system:role:list")));

        LoginUserDTO dto = (LoginUserDTO) userDetailService.loadUserByUsername("admin");

        assertThat(dto.getUserId()).isEqualTo(1L);
        assertThat(dto.getUsername()).isEqualTo("admin");
        assertThat(dto.getDeptId()).isEqualTo(20L);
        assertThat(dto.getSystemUserDO()).isSameAs(user);
        assertThat(dto.getPermissions()).containsExactlyInAnyOrder(
                "system:user:list", "system:role:list");
        then(dataScopeService).should().resolveDataScope(dto);
        then(systemMenuService).should().getPermission(1L);
    }

    private static SystemUserDO buildUser(Long id, String username, String status) {
        SystemUserDO user = new SystemUserDO();
        user.setId(id);
        user.setUsername(username);
        user.setStatus(status);
        user.setTenantId(1L);
        user.setDeptId(10L);
        return user;
    }
}
