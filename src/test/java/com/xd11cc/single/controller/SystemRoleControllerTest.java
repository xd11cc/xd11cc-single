package com.xd11cc.single.controller;

import com.xd11cc.single.entity.base.PageResult;
import com.xd11cc.single.entity.base.ResponseVO;
import com.xd11cc.single.entity.domain.SystemRoleDO;
import com.xd11cc.single.entity.vo.SystemRoleAddVO;
import com.xd11cc.single.entity.vo.SystemRoleQueryVO;
import com.xd11cc.single.entity.vo.SystemRoleUpdateVO;
import com.xd11cc.single.service.ISystemRoleDeptService;
import com.xd11cc.single.service.ISystemRoleService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class SystemRoleControllerTest {

    @Mock
    private ISystemRoleService systemRoleService;

    @Mock
    private ISystemRoleDeptService systemRoleDeptService;

    @InjectMocks
    private SystemRoleController roleController;

    // ==================== add ====================

    @Test
    void add_成功_返回行数() {
        SystemRoleAddVO vo = buildAddVO("管理员", "admin", "0", "备注");
        given(systemRoleService.add(vo)).willReturn(1);

        ResponseVO<Integer> result = roleController.add(vo);

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData()).isEqualTo(1);
        assertThat(result.getMsg()).isEqualTo("新增成功");
    }

    @Test
    void add_失败_返回失败消息() {
        SystemRoleAddVO vo = buildAddVO("管理员", "admin", "0", "备注");
        given(systemRoleService.add(vo)).willReturn(0);

        ResponseVO<Integer> result = roleController.add(vo);

        assertThat(result.getCode()).isEqualTo(500);
        assertThat(result.getData()).isNull();
        assertThat(result.getMsg()).isEqualTo("新增失败");
    }

    // ==================== removeByIds ====================

    @Test
    void removeByIds_成功_返回删除行数() {
        given(systemRoleService.deleteByIds(Arrays.asList(1L, 2L))).willReturn(2);

        ResponseVO<Integer> result = roleController.removeByIds(Arrays.asList(1L, 2L));

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData()).isEqualTo(2);
        assertThat(result.getMsg()).isEqualTo("删除成功");
    }

    // ==================== modifyById ====================

    @Test
    void modifyById_成功_返回行数() {
        SystemRoleUpdateVO vo = buildUpdateVO(1L, "新管理员", "admin", "0", "备注");
        given(systemRoleService.modifyById(vo)).willReturn(1);

        ResponseVO<Integer> result = roleController.modifyById(vo);

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData()).isEqualTo(1);
        assertThat(result.getMsg()).isEqualTo("修改成功");
    }

    // ==================== page ====================

    @Test
    void page_查询_返回分页结果() {
        SystemRoleQueryVO vo = new SystemRoleQueryVO();
        vo.setCurrentPage(1);
        vo.setPageSize(10);
        List<SystemRoleDO> roles = Collections.singletonList(buildRole(1L, "admin", "管理员", "0"));
        given(systemRoleService.getList(vo)).willReturn(roles);

        ResponseVO<PageResult<SystemRoleDO>> result = roleController.page(vo);

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData()).isNotNull();
        assertThat(result.getData().getRows()).hasSize(1);
    }

    // ==================== list ====================

    @Test
    void list_查询所有角色() {
        SystemRoleQueryVO vo = new SystemRoleQueryVO();
        List<SystemRoleDO> roles = Arrays.asList(buildRole(1L, "admin", "管理员", "0"), buildRole(2L, "user", "用户", "0"));
        given(systemRoleService.getList(vo)).willReturn(roles);

        ResponseVO<List<SystemRoleDO>> result = roleController.list(vo);

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData()).hasSize(2);
        assertThat(result.getData().get(0).getRoleCode()).isEqualTo("admin");
    }

    // ==================== getMenuIds ====================

    @Test
    void getMenuIds_查询角色菜单权限() {
        given(systemRoleService.getMenuIdsByRoleId(1L)).willReturn(Arrays.asList(1L, 2L, 3L));

        ResponseVO<List<Long>> result = roleController.getMenuIds(1L);

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData()).containsExactlyInAnyOrder(1L, 2L, 3L);
    }

    // ==================== getDeptIds ====================

    @Test
    void getDeptIds_查询角色数据权限部门() {
        Set<Long> deptIds = new java.util.HashSet<>(Arrays.asList(10L, 20L));
        given(systemRoleDeptService.getDeptIdsByRoleId(1L)).willReturn(deptIds);

        ResponseVO<Set<Long>> result = roleController.getDeptIds(1L);

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData()).containsExactlyInAnyOrder(10L, 20L);
    }

    // ==================== 辅助方法 ====================

    private static SystemRoleAddVO buildAddVO(String roleName, String roleCode, String status, String remark) {
        SystemRoleAddVO vo = new SystemRoleAddVO();
        vo.setRoleName(roleName);
        vo.setRoleCode(roleCode);
        vo.setStatus(status);
        vo.setRemark(remark);
        return vo;
    }

    private static SystemRoleUpdateVO buildUpdateVO(Long id, String roleName, String roleCode, String status, String remark) {
        SystemRoleUpdateVO vo = new SystemRoleUpdateVO();
        vo.setId(id);
        vo.setRoleName(roleName);
        vo.setRoleCode(roleCode);
        vo.setStatus(status);
        vo.setRemark(remark);
        return vo;
    }

    private static SystemRoleDO buildRole(Long id, String roleCode, String roleName, String status) {
        SystemRoleDO role = new SystemRoleDO();
        role.setId(id);
        role.setRoleCode(roleCode);
        role.setRoleName(roleName);
        role.setStatus(status);
        return role;
    }
}
