package com.xd11cc.single.service.impl;

import com.xd11cc.single.config.exception.ServiceException;
import com.xd11cc.single.entity.domain.SystemDeptDO;
import com.xd11cc.single.entity.vo.SystemDeptAddVO;
import com.xd11cc.single.entity.vo.SystemDeptQueryVO;
import com.xd11cc.single.entity.vo.SystemDeptTreeVO;
import com.xd11cc.single.entity.vo.SystemDeptUpdateVO;
import com.xd11cc.single.enums.SystemErrorEnum;
import com.xd11cc.single.mapper.SystemDeptMapper;
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
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;

@ExtendWith(MockitoExtension.class)
class SystemDeptServiceImplTest extends BaseUnitTest {

    @Mock
    private SystemDeptMapper baseMapper;

    @InjectMocks
    private SystemDeptServiceImpl deptService;

    // ==================== add ====================

    @Test
    void add_成功_返回插入行数() {
        SystemDeptAddVO vo = buildAddVO(null, "dept001", "技术部", 1L, 1, "0", "备注");
        given(baseMapper.insert(any(SystemDeptDO.class))).willReturn(1);

        int row = deptService.add(vo);

        assertThat(row).isEqualTo(1);
        then(baseMapper).should().insert(any(SystemDeptDO.class));
    }

    @Test
    void add_重复键_抛部门编码已存在() {
        SystemDeptAddVO vo = buildAddVO(null, "dept001", "技术部", 1L, 1, "0", null);
        willThrow(new DuplicateKeyException("dup")).given(baseMapper).insert(any(SystemDeptDO.class));

        ServiceException ex = assertThrows(ServiceException.class, () -> deptService.add(vo));
        assertThat(ex.getErrorCode()).isEqualTo(SystemErrorEnum.DEPT_CODE_EXISTS);
    }

    // ==================== deleteById ====================

    @Test
    void deleteById_有子部门_抛部门存在子部门异常() {
        // count() 由 ServiceImpl 提供，返回子部门数量 > 0
        given(deptService.count(any())).willReturn(2L);

        ServiceException ex = assertThrows(ServiceException.class, () -> deptService.deleteById(1L));
        assertThat(ex.getErrorCode()).isEqualTo(SystemErrorEnum.DEPT_HAVE_CHILDREN);
        then(baseMapper).should(org.mockito.BDDMockito.never()).deleteById(any());
    }

    @Test
    void deleteById_无子部门_正常删除() {
        given(deptService.count(any())).willReturn(0L);
        given(baseMapper.deleteById(1L)).willReturn(1);

        int row = deptService.deleteById(1L);

        assertThat(row).isEqualTo(1);
        then(baseMapper).should().deleteById(1L);
    }

    // ==================== modifyById ====================

    @Test
    void modifyById_自身设为父部门_抛不能设置为自身父部门() {
        SystemDeptUpdateVO vo = buildUpdateVO(1L, 1L, "dept001", "技术部", 1L, 1, "0");

        ServiceException ex = assertThrows(ServiceException.class, () -> deptService.modifyById(vo));
        assertThat(ex.getErrorCode()).isEqualTo(SystemErrorEnum.DEPT_PARENT_CANNOT_SELF);
        then(baseMapper).should(org.mockito.BDDMockito.never()).updateById(any());
    }

    @Test
    void modifyById_成功_返回更新行数() {
        SystemDeptUpdateVO vo = buildUpdateVO(1L, 0L, "dept001", "技术部", 1L, 1, "0");
        given(baseMapper.updateById(any(SystemDeptDO.class))).willReturn(1);

        int row = deptService.modifyById(vo);

        assertThat(row).isEqualTo(1);
        then(baseMapper).should().updateById(any(SystemDeptDO.class));
    }

    @Test
    void modifyById_重复键_抛部门编码已存在() {
        SystemDeptUpdateVO vo = buildUpdateVO(1L, 0L, "dup", "重复", 1L, 1, "0");
        willThrow(new DuplicateKeyException("dup")).given(baseMapper).updateById(any(SystemDeptDO.class));

        ServiceException ex = assertThrows(ServiceException.class, () -> deptService.modifyById(vo));
        assertThat(ex.getErrorCode()).isEqualTo(SystemErrorEnum.DEPT_CODE_EXISTS);
    }

    // ==================== getList ====================

    @Test
    void getList_带名称筛选_拼接like条件() {
        SystemDeptQueryVO vo = new SystemDeptQueryVO();
        vo.setDeptName("技术部");
        vo.setStatus("0");
        SystemDeptDO expected = buildDept(1L, 0L, "dept_tech", "技术部", 1L, 1, "0");
        given(baseMapper.selectList(any())).willReturn(Collections.singletonList(expected));

        List<SystemDeptDO> result = deptService.getList(vo);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDeptName()).isEqualTo("技术部");
    }

    @Test
    void getList_带状态筛选_拼接eq条件() {
        SystemDeptQueryVO vo = new SystemDeptQueryVO();
        vo.setDeptName(null);
        vo.setStatus("1");
        given(baseMapper.selectList(any())).willReturn(Collections.emptyList());

        List<SystemDeptDO> result = deptService.getList(vo);

        assertThat(result).isEmpty();
    }

    @Test
    void getList_空筛选_返回全部() {
        SystemDeptQueryVO vo = new SystemDeptQueryVO();
        SystemDeptDO d1 = buildDept(1L, 0L, "root", "根部门", 1L, 1, "0");
        SystemDeptDO d2 = buildDept(2L, 1L, "sub", "子部门", 1L, 2, "0");
        given(baseMapper.selectList(any())).willReturn(Arrays.asList(d1, d2));

        List<SystemDeptDO> result = deptService.getList(vo);

        assertThat(result).hasSize(2);
    }

    // ==================== getTreeList ====================

    @Test
    void getTreeList_返回树形结构() {
        SystemDeptQueryVO vo = new SystemDeptQueryVO();
        // 根部门 parentId = null（与 SQL 中 parent_id = NULL 约定一致）
        SystemDeptDO root = buildDept(1L, null, "root", "根", 1L, 1, "0");
        SystemDeptDO child = buildDept(2L, 1L, "sub", "子", 1L, 2, "0");
        given(baseMapper.selectList(any())).willReturn(Arrays.asList(root, child));

        List<SystemDeptTreeVO> treeList = deptService.getTreeList(vo);

        assertThat(treeList).hasSize(1);
        assertThat(treeList.get(0).getId()).isEqualTo(1L);
        assertThat(treeList.get(0).getDeptName()).isEqualTo("根");
        assertThat(treeList.get(0).getChildren()).hasSize(1);
        assertThat(treeList.get(0).getChildren().get(0).getId()).isEqualTo(2L);
    }

    // ==================== getSubDeptIds ====================

    @Test
    void getSubDeptIds_含子部门_递归收集所有后代id() {
        // 部门层级：1(root) -> 2(child) -> 3(grandchild)
        SystemDeptDO root = buildDept(1L, null, "root", "根", 1L, 1, "0");
        SystemDeptDO child = buildDept(2L, 1L, "sub1", "子1", 1L, 2, "0");
        SystemDeptDO grandchild = buildDept(3L, 2L, "sub2", "子2", 1L, 3, "0");
        // 返回全表列表，BFS 遍历
        given(baseMapper.selectList(any())).willReturn(Arrays.asList(root, child, grandchild));

        Set<Long> result = deptService.getSubDeptIds(1L);

        assertThat(result).containsExactlyInAnyOrder(1L, 2L, 3L);
    }

    @Test
    void getSubDeptIds_无子部门_仅返回自身() {
        SystemDeptDO standalone = buildDept(5L, 1L, "leaf", "叶子", 1L, 5, "0");
        given(baseMapper.selectList(any())).willReturn(Collections.singletonList(standalone));

        Set<Long> result = deptService.getSubDeptIds(5L);

        assertThat(result).containsExactly(5L);
    }

    // ==================== 辅助方法 ====================

    private static SystemDeptDO buildDept(Long id, Long parentId, String deptCode, String deptName,
                                          Long leaderId, Integer sort, String status) {
        SystemDeptDO dept = new SystemDeptDO();
        dept.setId(id);
        dept.setParentId(parentId);
        dept.setDeptCode(deptCode);
        dept.setDeptName(deptName);
        dept.setLeaderId(leaderId);
        dept.setSort(sort);
        dept.setStatus(status);
        dept.setRemark("备注");
        return dept;
    }

    private static SystemDeptAddVO buildAddVO(Long parentId, String deptCode, String deptName,
                                               Long leaderId, Integer sort, String status, String remark) {
        SystemDeptAddVO vo = new SystemDeptAddVO();
        vo.setParentId(parentId);
        vo.setDeptCode(deptCode);
        vo.setDeptName(deptName);
        vo.setLeaderId(leaderId);
        vo.setSort(sort);
        vo.setStatus(status);
        vo.setRemark(remark);
        return vo;
    }

    private static SystemDeptUpdateVO buildUpdateVO(Long id, Long parentId, String deptCode,
                                                     String deptName, Long leaderId,
                                                     Integer sort, String status) {
        SystemDeptUpdateVO vo = new SystemDeptUpdateVO();
        vo.setId(id);
        vo.setParentId(parentId);
        vo.setDeptCode(deptCode);
        vo.setDeptName(deptName);
        vo.setLeaderId(leaderId);
        vo.setSort(sort);
        vo.setStatus(status);
        vo.setRemark("备注");
        return vo;
    }
}
