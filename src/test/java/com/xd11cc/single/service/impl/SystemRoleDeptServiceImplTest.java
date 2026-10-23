package com.xd11cc.single.service.impl;

import com.xd11cc.single.entity.domain.SystemRoleDeptDO;
import com.xd11cc.single.mapper.SystemRoleDeptMapper;
import com.xd11cc.single.service.ISystemRoleDeptService;
import com.xd11cc.single.util.BaseUnitTest;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class SystemRoleDeptServiceImplTest extends BaseUnitTest {

    @Mock
    private SystemRoleDeptMapper baseMapper;

    @InjectMocks
    private SystemRoleDeptServiceImpl roleDeptService;

    // ==================== getDeptIdsByRoleId ====================

    @Test
    void getDeptIdsByRoleId_有关联_返回部门id集合() {
        SystemRoleDeptDO rd1 = buildRoleDept(1L, 10L, 100L);
        SystemRoleDeptDO rd2 = buildRoleDept(2L, 10L, 200L);
        given(baseMapper.selectList(any())).willReturn(Arrays.asList(rd1, rd2));

        Set<Long> deptIds = roleDeptService.getDeptIdsByRoleId(10L);

        assertThat(deptIds).containsExactlyInAnyOrder(100L, 200L);
    }

    @Test
    void getDeptIdsByRoleId_无关联_返回空集合() {
        given(baseMapper.selectList(any())).willReturn(Collections.emptyList());

        Set<Long> deptIds = roleDeptService.getDeptIdsByRoleId(99L);

        assertThat(deptIds).isEmpty();
    }

    // ==================== getDeptIdsByRoleIds ====================

    @Test
    void getDeptIdsByRoleIds_多个角色_返回并集() {
        SystemRoleDeptDO rd1 = buildRoleDept(1L, 10L, 100L);
        SystemRoleDeptDO rd2 = buildRoleDept(2L, 20L, 200L);
        given(baseMapper.selectList(any())).willReturn(Arrays.asList(rd1, rd2));

        Set<Long> deptIds = roleDeptService.getDeptIdsByRoleIds(new java.util.HashSet<>(Arrays.asList(10L, 20L)));

        assertThat(deptIds).containsExactlyInAnyOrder(100L, 200L);
    }

    @Test
    void getDeptIdsByRoleIds_null输入_返回空集合() {
        Set<Long> deptIds = roleDeptService.getDeptIdsByRoleIds(null);

        assertThat(deptIds).isEmpty();
        then(baseMapper).should(org.mockito.BDDMockito.never()).selectList(any());
    }

    @Test
    void getDeptIdsByRoleIds_空集合_返回空集合() {
        Set<Long> deptIds = roleDeptService.getDeptIdsByRoleIds(Collections.emptySet());

        assertThat(deptIds).isEmpty();
        then(baseMapper).should(org.mockito.BDDMockito.never()).selectList(any());
    }

    // ==================== saveRoleDepts ====================

    @Test
    void saveRoleDepts_null_不调用saveBatch() {
        roleDeptService.saveRoleDepts(1L, null);

        then(baseMapper).should(org.mockito.BDDMockito.never()).insert(any());
    }

    @Test
    void saveRoleDepts_空列表_不调用saveBatch() {
        roleDeptService.saveRoleDepts(1L, Collections.emptyList());

        then(baseMapper).should(org.mockito.BDDMockito.never()).insert(any());
    }

    // 注意：saveBatch 由 IService 提供，依赖 MyBatis-Plus TableInfo 缓存；
    // 在 mock baseMapper 场景下直接调用会抛出 TableInfoCache 异常，
    // saveBatch 行为的正确性已通过 SystemRoleServiceImplTest 间接覆盖

    // ==================== removeByRoleId ====================

    @Test
    void removeByRoleId_删除指定角色关联() {
        roleDeptService.removeByRoleId(10L);

        then(baseMapper).should().delete(any());
    }

    // ==================== removeByRoleIds ====================

    @Test
    void removeByRoleIds_批量删除指定角色关联() {
        roleDeptService.removeByRoleIds(Arrays.asList(10L, 20L));

        then(baseMapper).should().delete(any());
    }

    // ==================== 辅助方法 ====================

    private static SystemRoleDeptDO buildRoleDept(Long id, Long roleId, Long deptId) {
        SystemRoleDeptDO rd = new SystemRoleDeptDO();
        rd.setId(id);
        rd.setRoleId(roleId);
        rd.setDeptId(deptId);
        return rd;
    }
}
