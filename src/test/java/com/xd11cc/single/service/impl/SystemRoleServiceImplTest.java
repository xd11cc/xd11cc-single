package com.xd11cc.single.service.impl;

import com.xd11cc.single.config.exception.ServiceException;
import com.xd11cc.single.entity.domain.SystemRoleDO;
import com.xd11cc.single.entity.domain.SystemRoleMenuDO;
import com.xd11cc.single.entity.domain.SystemUserRoleDO;
import com.xd11cc.single.entity.vo.SystemRoleAddVO;
import com.xd11cc.single.entity.vo.SystemRoleQueryVO;
import com.xd11cc.single.entity.vo.SystemRoleUpdateVO;
import com.xd11cc.single.enums.DataScopeEnum;
import com.xd11cc.single.enums.SystemErrorEnum;
import com.xd11cc.single.mapper.SystemRoleMapper;
import com.xd11cc.single.service.ISystemRoleDeptService;
import com.xd11cc.single.service.ISystemRoleMenuService;
import com.xd11cc.single.service.ISystemRoleService;
import com.xd11cc.single.service.ISystemUserRoleService;
import com.xd11cc.single.util.BaseUnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;

@ExtendWith(MockitoExtension.class)
class SystemRoleServiceImplTest extends BaseUnitTest {

    @Mock
    private SystemRoleMapper baseMapper;
    @Mock
    private ISystemRoleMenuService systemRoleMenuService;
    @Mock
    private ISystemUserRoleService systemUserRoleService;
    @Mock
    private ISystemRoleDeptService systemRoleDeptService;

    @InjectMocks
    private SystemRoleServiceImpl roleService;

    // ==================== add ====================

    @Test
    void add_成功_写入角色并保存菜单和部门关联() {
        SystemRoleAddVO vo = buildAddVO("admin", "管理员", "0",
                DataScopeEnum.CUSTOM.getCode(), Arrays.asList(1L, 2L), Arrays.asList(10L, 20L));
        given(baseMapper.insert(any(SystemRoleDO.class))).willReturn(1);

        int row = roleService.add(vo);

        assertThat(row).isEqualTo(1);
        then(baseMapper).should().insert(any(SystemRoleDO.class));
        then(systemRoleMenuService).should().saveBatch(anyList());
        then(systemRoleDeptService).should().removeByRoleId(any());
        then(systemRoleDeptService).should().saveRoleDepts(any(), any());
    }

    @Test
    void add_重复键_抛角色编码已存在() {
        SystemRoleAddVO vo = buildAddVO("dup", "重复", "0",
                DataScopeEnum.ALL.getCode(), null, null);
        willThrow(new DuplicateKeyException("dup")).given(baseMapper).insert(any(SystemRoleDO.class));

        ServiceException ex = assertThrows(ServiceException.class, () -> roleService.add(vo));
        assertThat(ex.getErrorCode()).isEqualTo(SystemErrorEnum.ROLE_CODE_EXISTS);
        then(systemRoleMenuService).should(org.mockito.BDDMockito.never()).saveBatch(anyList());
        then(systemRoleDeptService).should(org.mockito.BDDMockito.never()).saveRoleDepts(any(), any());
    }

    @Test
    void add_空菜单列表_仅写入角色不调用saveBatch() {
        SystemRoleAddVO vo = buildAddVO("simple", "简单", "0",
                DataScopeEnum.ALL.getCode(), Collections.emptyList(), null);
        given(baseMapper.insert(any(SystemRoleDO.class))).willReturn(1);

        int row = roleService.add(vo);

        assertThat(row).isEqualTo(1);
        then(systemRoleMenuService).should(org.mockito.BDDMockito.never()).saveBatch(anyList());
        then(systemRoleDeptService).should().removeByRoleId(any());
    }

    @Test
    void add_非自定义数据范围_不保存部门关联() {
        SystemRoleAddVO vo = buildAddVO("scope", "范围", "0",
                DataScopeEnum.DEPT_AND_SUB.getCode(), Arrays.asList(1L), null);
        given(baseMapper.insert(any(SystemRoleDO.class))).willReturn(1);

        int row = roleService.add(vo);

        assertThat(row).isEqualTo(1);
        then(systemRoleDeptService).should().removeByRoleId(any());
        then(systemRoleDeptService).should(org.mockito.BDDMockito.never()).saveRoleDepts(any(), any());
    }

    // ==================== deleteByIds ====================

    @Test
    void deleteByIds_已绑定用户_抛角色已绑定用户() {
        given(systemUserRoleService.count(any())).willReturn(3L);

        ServiceException ex = assertThrows(ServiceException.class,
                () -> roleService.deleteByIds(Arrays.asList(1L, 2L)));
        assertThat(ex.getErrorCode()).isEqualTo(SystemErrorEnum.ROLE_BINDEDE_USER);
        then(baseMapper).should(org.mockito.BDDMockito.never()).deleteBatchIds(any());
        then(systemRoleMenuService).should(org.mockito.BDDMockito.never()).remove(any());
        then(systemRoleDeptService).should(org.mockito.BDDMockito.never()).removeByRoleIds(any());
    }

    @Test
    void deleteByIds_无用户绑定_删除并清理菜单和部门关联() {
        given(systemUserRoleService.count(any())).willReturn(0L);
        given(baseMapper.deleteBatchIds(Arrays.asList(2L, 3L))).willReturn(2);

        int row = roleService.deleteByIds(Arrays.asList(2L, 3L));

        assertThat(row).isEqualTo(2);
        then(baseMapper).should().deleteBatchIds(Arrays.asList(2L, 3L));
        then(systemRoleMenuService).should().remove(any());
        then(systemRoleDeptService).should().removeByRoleIds(Arrays.asList(2L, 3L));
    }

    // ==================== modifyById ====================

    @Test
    void modifyById_成功_更新角色并更新菜单和部门关联() {
        SystemRoleUpdateVO vo = buildUpdateVO(1L, "new_code", "新角色", "0",
                DataScopeEnum.CUSTOM.getCode(), Arrays.asList(10L, 20L));
        given(baseMapper.updateById(any(SystemRoleDO.class))).willReturn(1);

        int row = roleService.modifyById(vo);

        assertThat(row).isEqualTo(1);
        then(baseMapper).should().updateById(any(SystemRoleDO.class));
        then(systemRoleMenuService).should().remove(any());
        then(systemRoleMenuService).should().saveBatch(anyList());
        then(systemRoleDeptService).should().removeByRoleId(1L);
        then(systemRoleDeptService).should().saveRoleDepts(1L, Arrays.asList(10L, 20L));
    }

    @Test
    void modifyById_重复键_抛角色编码已存在() {
        SystemRoleUpdateVO vo = buildUpdateVO(1L, "dup", "重复", "0",
                DataScopeEnum.ALL.getCode(), Collections.emptyList());
        willThrow(new DuplicateKeyException("dup")).given(baseMapper).updateById(any(SystemRoleDO.class));

        ServiceException ex = assertThrows(ServiceException.class, () -> roleService.modifyById(vo));
        assertThat(ex.getErrorCode()).isEqualTo(SystemErrorEnum.ROLE_CODE_EXISTS);
        then(systemRoleMenuService).should(org.mockito.BDDMockito.never()).remove(any());
        then(systemRoleDeptService).should(org.mockito.BDDMockito.never()).removeByRoleId(any());
    }

    @Test
    void modifyById_空菜单列表_删除旧菜单关联() {
        SystemRoleUpdateVO vo = buildUpdateVO(1L, "code", "角色", "0",
                DataScopeEnum.ALL.getCode(), Collections.emptyList());
        given(baseMapper.updateById(any(SystemRoleDO.class))).willReturn(1);

        int row = roleService.modifyById(vo);

        assertThat(row).isEqualTo(1);
        then(systemRoleMenuService).should().remove(any());
        then(systemRoleMenuService).should(org.mockito.BDDMockito.never()).saveBatch(anyList());
    }

    @Test
    void modifyById_非自定义_清理部门关联但不保存新部门() {
        SystemRoleUpdateVO vo = buildUpdateVO(1L, "code", "角色", "0",
                DataScopeEnum.DEPT.getCode(), Collections.emptyList());
        given(baseMapper.updateById(any(SystemRoleDO.class))).willReturn(1);

        int row = roleService.modifyById(vo);

        assertThat(row).isEqualTo(1);
        then(systemRoleDeptService).should().removeByRoleId(1L);
        then(systemRoleDeptService).should(org.mockito.BDDMockito.never()).saveRoleDepts(any(), any());
    }

    // ==================== getList ====================

    @Test
    void getList_全匹配查询_返回结果() {
        SystemRoleQueryVO vo = buildQueryVO("admin", "管理员", "0", null, null);
        SystemRoleDO expected = buildRole(1L, "admin", "管理员", "0", null);
        given(baseMapper.selectList(any())).willReturn(Collections.singletonList(expected));

        List<SystemRoleDO> result = roleService.getList(vo);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRoleCode()).isEqualTo("admin");
        assertThat(result.get(0).getRoleName()).isEqualTo("管理员");
    }

    @Test
    void getList_空筛选_返回全部() {
        SystemRoleQueryVO vo = new SystemRoleQueryVO();
        SystemRoleDO r1 = buildRole(1L, "r1", "角色1", "0", null);
        SystemRoleDO r2 = buildRole(2L, "r2", "角色2", "0", null);
        given(baseMapper.selectList(any())).willReturn(Arrays.asList(r1, r2));

        List<SystemRoleDO> result = roleService.getList(vo);

        assertThat(result).hasSize(2);
    }

    @Test
    void getList_带时间范围_拼接开始和结束时间() {
        SystemRoleQueryVO vo = buildQueryVO(null, null, null,
                new java.util.Date(946684800000L), new java.util.Date());
        given(baseMapper.selectList(any())).willReturn(Collections.emptyList());

        List<SystemRoleDO> result = roleService.getList(vo);

        assertThat(result).isEmpty();
        then(baseMapper).should().selectList(any());
    }

    // ==================== getMenuIdsByRoleId ====================

    @Test
    void getMenuIdsByRoleId_有菜单_返回id列表() {
        SystemRoleMenuDO rm1 = buildRoleMenu(1L, 10L, 100L);
        SystemRoleMenuDO rm2 = buildRoleMenu(2L, 10L, 200L);
        given(systemRoleMenuService.list(any())).willReturn(Arrays.asList(rm1, rm2));

        List<Long> menuIds = roleService.getMenuIdsByRoleId(10L);

        assertThat(menuIds).containsExactly(100L, 200L);
        then(systemRoleMenuService).should().list(any());
    }

    @Test
    void getMenuIdsByRoleId_无菜单_返回空列表() {
        given(systemRoleMenuService.list(any())).willReturn(Collections.emptyList());

        List<Long> menuIds = roleService.getMenuIdsByRoleId(99L);

        assertThat(menuIds).isEmpty();
    }

    // ==================== 辅助方法 ====================

    private static SystemRoleDO buildRole(Long id, String roleCode, String roleName,
                                          String status, String dataScope) {
        SystemRoleDO role = new SystemRoleDO();
        role.setId(id);
        role.setRoleCode(roleCode);
        role.setRoleName(roleName);
        role.setStatus(status);
        role.setDataScope(dataScope);
        role.setRemark("备注");
        return role;
    }

    private static SystemRoleAddVO buildAddVO(String roleCode, String roleName, String status,
                                               String dataScope, List<Long> menuIds, List<Long> deptIds) {
        SystemRoleAddVO vo = new SystemRoleAddVO();
        vo.setRoleCode(roleCode);
        vo.setRoleName(roleName);
        vo.setStatus(status);
        vo.setMenuIds(menuIds);
        vo.setDataScope(dataScope);
        vo.setDeptIds(deptIds);
        vo.setRemark("备注");
        return vo;
    }

    private static SystemRoleUpdateVO buildUpdateVO(Long id, String roleCode, String roleName,
                                                     String status, String dataScope, List<Long> menuIds) {
        SystemRoleUpdateVO vo = new SystemRoleUpdateVO();
        vo.setId(id);
        vo.setRoleCode(roleCode);
        vo.setRoleName(roleName);
        vo.setStatus(status);
        vo.setMenuIds(menuIds);
        vo.setDataScope(dataScope);
        vo.setDeptIds(menuIds);
        vo.setRemark("备注");
        return vo;
    }

    private static SystemRoleQueryVO buildQueryVO(String roleCode, String roleName,
                                                   String status, java.util.Date beginTime,
                                                   java.util.Date endTime) {
        SystemRoleQueryVO vo = new SystemRoleQueryVO();
        vo.setRoleCode(roleCode);
        vo.setRoleName(roleName);
        vo.setStatus(status);
        vo.setBeginTime(beginTime);
        vo.setEndTime(endTime);
        return vo;
    }

    private static SystemRoleMenuDO buildRoleMenu(Long id, Long roleId, Long menuId) {
        SystemRoleMenuDO rm = new SystemRoleMenuDO();
        rm.setId(id);
        rm.setRoleId(roleId);
        rm.setMenuId(menuId);
        rm.setRemark("关联");
        return rm;
    }
}
