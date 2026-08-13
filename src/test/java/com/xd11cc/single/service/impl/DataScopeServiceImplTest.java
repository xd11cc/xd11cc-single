package com.xd11cc.single.service.impl;

import com.xd11cc.single.config.context.TenantContextHolder;
import com.xd11cc.single.entity.domain.SystemRoleDO;
import com.xd11cc.single.entity.domain.SystemRoleDeptDO;
import com.xd11cc.single.entity.domain.SystemUserRoleDO;
import com.xd11cc.single.entity.dto.LoginUserDTO;
import com.xd11cc.single.enums.DataScopeEnum;
import com.xd11cc.single.service.ISystemDeptService;
import com.xd11cc.single.service.ISystemRoleDeptService;
import com.xd11cc.single.service.ISystemRoleService;
import com.xd11cc.single.service.ISystemUserRoleService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

class DataScopeServiceImplTest {

    @Mock
    private ISystemUserRoleService systemUserRoleService;
    @Mock
    private ISystemRoleService systemRoleService;
    @Mock
    private ISystemDeptService systemDeptService;
    @Mock
    private ISystemRoleDeptService systemRoleDeptService;

    @InjectMocks
    private DataScopeServiceImpl dataScopeService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() {
        TenantContextHolder.clear();
    }

    // ==================== resolveDataScope 无角色 ====================

    @Test
    void resolveDataScope_无角色_设置为SELF() {
        LoginUserDTO dto = buildLoginUserDTO(1L, 10L);

        given(systemUserRoleService.list(org.mockito.ArgumentMatchers.any()))
                .willReturn(Collections.emptyList());

        dataScopeService.resolveDataScope(dto);

        assertThat(dto.getDataScope()).isEqualTo(DataScopeEnum.SELF.getCode());
        assertThat(dto.getRoleIds()).isNull();
        assertThat(dto.getDataScopeDeptIds()).isNull();
    }

    // ==================== resolveDataScope 全量权限 ====================

    @Test
    void resolveDataScope_有ALL角色_设置为ALL() {
        LoginUserDTO dto = buildLoginUserDTO(1L, 10L);
        SystemUserRoleDO userRole = buildUserRole(1L, 1L, 101L);
        SystemRoleDO allRole = buildRole(101L, "admin", DataScopeEnum.ALL.getCode());

        given(systemUserRoleService.list(org.mockito.ArgumentMatchers.any()))
                .willReturn(Collections.singletonList(userRole));
        given(systemRoleService.listByIds(org.mockito.ArgumentMatchers.any()))
                .willReturn(Collections.singletonList(allRole));

        dataScopeService.resolveDataScope(dto);

        assertThat(dto.getDataScope()).isEqualTo(DataScopeEnum.ALL.getCode());
        assertThat(dto.getRoleIds()).containsExactly(101L);
        assertThat(dto.getDataScopeDeptIds()).isNull();
    }

    // ==================== resolveDataScope 全SELF ====================

    @Test
    void resolveDataScope_全部SELF角色_设置为SELF() {
        LoginUserDTO dto = buildLoginUserDTO(1L, 10L);
        SystemUserRoleDO userRole1 = buildUserRole(1L, 1L, 101L);
        SystemUserRoleDO userRole2 = buildUserRole(2L, 1L, 102L);
        SystemRoleDO selfRole1 = buildRole(101L, "user1", DataScopeEnum.SELF.getCode());
        SystemRoleDO selfRole2 = buildRole(102L, "user2", DataScopeEnum.SELF.getCode());

        given(systemUserRoleService.list(org.mockito.ArgumentMatchers.any()))
                .willReturn(Arrays.asList(userRole1, userRole2));
        given(systemRoleService.listByIds(org.mockito.ArgumentMatchers.any()))
                .willReturn(Arrays.asList(selfRole1, selfRole2));

        dataScopeService.resolveDataScope(dto);

        assertThat(dto.getDataScope()).isEqualTo(DataScopeEnum.SELF.getCode());
        assertThat(dto.getRoleIds()).containsExactlyInAnyOrder(101L, 102L);
        assertThat(dto.getDataScopeDeptIds()).isNull();
    }

    // ==================== resolveDataScope 部门及下级 ====================

    @Test
    void resolveDataScope_DEPT_AND_SUB角色_包含下级部门id() {
        LoginUserDTO dto = buildLoginUserDTO(1L, 10L);
        SystemUserRoleDO userRole = buildUserRole(1L, 1L, 101L);
        SystemRoleDO deptAndSubRole = buildRole(101L, "dept_mgr", DataScopeEnum.DEPT_AND_SUB.getCode());

        given(systemUserRoleService.list(org.mockito.ArgumentMatchers.any()))
                .willReturn(Collections.singletonList(userRole));
        given(systemRoleService.listByIds(org.mockito.ArgumentMatchers.any()))
                .willReturn(Collections.singletonList(deptAndSubRole));
        given(systemDeptService.getSubDeptIds(10L))
                .willReturn(new HashSet<>(Arrays.asList(10L, 20L, 30L)));

        dataScopeService.resolveDataScope(dto);

        assertThat(dto.getDataScope()).isEqualTo(DataScopeEnum.CUSTOM.getCode());
        assertThat(dto.getDataScopeDeptIds()).containsExactlyInAnyOrder(10L, 20L, 30L);
    }

    // ==================== resolveDataScope 仅本部门 ====================

    @Test
    void resolveDataScope_DEPT角色_仅包含本部门id() {
        LoginUserDTO dto = buildLoginUserDTO(1L, 10L);
        SystemUserRoleDO userRole = buildUserRole(1L, 1L, 101L);
        SystemRoleDO deptRole = buildRole(101L, "dept_viewer", DataScopeEnum.DEPT.getCode());

        given(systemUserRoleService.list(org.mockito.ArgumentMatchers.any()))
                .willReturn(Collections.singletonList(userRole));
        given(systemRoleService.listByIds(org.mockito.ArgumentMatchers.any()))
                .willReturn(Collections.singletonList(deptRole));

        dataScopeService.resolveDataScope(dto);

        assertThat(dto.getDataScope()).isEqualTo(DataScopeEnum.CUSTOM.getCode());
        assertThat(dto.getDataScopeDeptIds()).containsExactly(10L);
    }

    // ==================== resolveDataScope 自定义角色 ====================

    @Test
    void resolveDataScope_CUSTOM角色_查询关联部门id() {
        LoginUserDTO dto = buildLoginUserDTO(1L, 10L);
        SystemUserRoleDO userRole = buildUserRole(1L, 1L, 101L);
        SystemRoleDO customRole = buildRole(101L, "custom_viewer", DataScopeEnum.CUSTOM.getCode());

        given(systemUserRoleService.list(org.mockito.ArgumentMatchers.any()))
                .willReturn(Collections.singletonList(userRole));
        given(systemRoleService.listByIds(org.mockito.ArgumentMatchers.any()))
                .willReturn(Collections.singletonList(customRole));
        given(systemRoleDeptService.getDeptIdsByRoleIds(org.mockito.ArgumentMatchers.any()))
                .willReturn(new HashSet<>(Arrays.asList(5L, 6L)));

        dataScopeService.resolveDataScope(dto);

        assertThat(dto.getDataScope()).isEqualTo(DataScopeEnum.CUSTOM.getCode());
        assertThat(dto.getDataScopeDeptIds()).containsExactlyInAnyOrder(5L, 6L);
    }

    // ==================== resolveDataScope 混合DEPT_AND_SUB和CUSTOM ====================

    @Test
    void resolveDataScope_混合范围_合并部门id集合() {
        LoginUserDTO dto = buildLoginUserDTO(1L, 10L);
        SystemUserRoleDO userRole1 = buildUserRole(1L, 1L, 101L);
        SystemUserRoleDO userRole2 = buildUserRole(2L, 1L, 102L);
        SystemRoleDO deptAndSubRole = buildRole(101L, "dept_mgr", DataScopeEnum.DEPT_AND_SUB.getCode());
        SystemRoleDO customRole = buildRole(102L, "custom_viewer", DataScopeEnum.CUSTOM.getCode());

        given(systemUserRoleService.list(org.mockito.ArgumentMatchers.any()))
                .willReturn(Arrays.asList(userRole1, userRole2));
        given(systemRoleService.listByIds(org.mockito.ArgumentMatchers.any()))
                .willReturn(Arrays.asList(deptAndSubRole, customRole));
        given(systemDeptService.getSubDeptIds(10L))
                .willReturn(new HashSet<>(Arrays.asList(10L, 20L)));
        given(systemRoleDeptService.getDeptIdsByRoleIds(org.mockito.ArgumentMatchers.any()))
                .willReturn(new HashSet<>(Collections.singletonList(30L)));

        dataScopeService.resolveDataScope(dto);

        assertThat(dto.getDataScope()).isEqualTo(DataScopeEnum.CUSTOM.getCode());
        assertThat(dto.getDataScopeDeptIds()).containsExactlyInAnyOrder(10L, 20L, 30L);
    }

    // ==================== resolveDataScope 混合无可见部门 ====================

    @Test
    void resolveDataScope_全部SELF混合无自定义_降级SELF() {
        LoginUserDTO dto = buildLoginUserDTO(1L, 10L);
        SystemUserRoleDO userRole1 = buildUserRole(1L, 1L, 101L);
        SystemUserRoleDO userRole2 = buildUserRole(2L, 1L, 102L);
        SystemRoleDO selfRole = buildRole(101L, "self1", DataScopeEnum.SELF.getCode());
        SystemRoleDO deptAndSubRole = buildRole(102L, "dept_empty", DataScopeEnum.DEPT_AND_SUB.getCode());

        given(systemUserRoleService.list(org.mockito.ArgumentMatchers.any()))
                .willReturn(Arrays.asList(userRole1, userRole2));
        given(systemRoleService.listByIds(org.mockito.ArgumentMatchers.any()))
                .willReturn(Arrays.asList(selfRole, deptAndSubRole));
        // 模拟 getSubDeptIds 返回空集合
        given(systemDeptService.getSubDeptIds(10L))
                .willReturn(Collections.emptySet());

        dataScopeService.resolveDataScope(dto);

        assertThat(dto.getDataScope()).isEqualTo(DataScopeEnum.SELF.getCode());
        assertThat(dto.getDataScopeDeptIds()).isNull();
    }

    // ==================== 辅助方法 ====================

    private LoginUserDTO buildLoginUserDTO(Long userId, Long deptId) {
        LoginUserDTO dto = new LoginUserDTO();
        dto.setUserId(userId);
        dto.setDeptId(deptId);
        return dto;
    }

    private SystemUserRoleDO buildUserRole(Long id, Long userId, Long roleId) {
        SystemUserRoleDO ur = new SystemUserRoleDO();
        ur.setId(id);
        ur.setUserId(userId);
        ur.setRoleId(roleId);
        return ur;
    }

    private SystemRoleDO buildRole(Long id, String roleName, String dataScope) {
        SystemRoleDO role = new SystemRoleDO();
        role.setId(id);
        role.setRoleName(roleName);
        role.setDataScope(dataScope);
        return role;
    }
}
