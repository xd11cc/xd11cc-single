package com.xd11cc.single.controller;

import com.xd11cc.single.entity.base.ResponseVO;
import com.xd11cc.single.entity.domain.SystemDeptDO;
import com.xd11cc.single.entity.vo.SystemDeptAddVO;
import com.xd11cc.single.entity.vo.SystemDeptQueryVO;
import com.xd11cc.single.entity.vo.SystemDeptTreeVO;
import com.xd11cc.single.entity.vo.SystemDeptUpdateVO;
import com.xd11cc.single.service.ISystemDeptService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class SystemDeptControllerTest {

    @Mock
    private ISystemDeptService systemDeptService;

    @InjectMocks
    private SystemDeptController deptController;

    // ==================== add ====================

    @Test
    void add_成功_返回行数() {
        SystemDeptAddVO vo = buildAddVO(null, "D001", "技术部", 1L, 1, "0", "备注");
        given(systemDeptService.add(vo)).willReturn(1);

        ResponseVO<Integer> result = deptController.add(vo);

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData()).isEqualTo(1);
        assertThat(result.getMsg()).isEqualTo("新增成功");
    }

    @Test
    void add_失败_返回失败消息() {
        SystemDeptAddVO vo = buildAddVO(null, "D001", "技术部", 1L, 1, "0", "备注");
        given(systemDeptService.add(vo)).willReturn(0);

        ResponseVO<Integer> result = deptController.add(vo);

        assertThat(result.getCode()).isEqualTo(500);
        assertThat(result.getData()).isNull();
        assertThat(result.getMsg()).isEqualTo("新增失败");
    }

    // ==================== removeById ====================

    @Test
    void removeById_成功_返回删除行数() {
        given(systemDeptService.deleteById(10L)).willReturn(1);

        ResponseVO<Integer> result = deptController.removeById(10L);

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData()).isEqualTo(1);
        assertThat(result.getMsg()).isEqualTo("删除成功");
    }

    @Test
    void removeById_失败_返回失败消息() {
        given(systemDeptService.deleteById(10L)).willReturn(0);

        ResponseVO<Integer> result = deptController.removeById(10L);

        assertThat(result.getCode()).isEqualTo(500);
        assertThat(result.getData()).isNull();
        assertThat(result.getMsg()).isEqualTo("删除失败");
    }

    // ==================== modifyById ====================

    @Test
    void modifyById_成功_返回行数() {
        SystemDeptUpdateVO vo = buildUpdateVO(1L, "D001", "技术部", 1L, 1, "0", "备注");
        given(systemDeptService.modifyById(vo)).willReturn(1);

        ResponseVO<Integer> result = deptController.modifyById(vo);

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData()).isEqualTo(1);
        assertThat(result.getMsg()).isEqualTo("修改成功");
    }

    @Test
    void modifyById_失败_返回失败消息() {
        SystemDeptUpdateVO vo = buildUpdateVO(1L, "D001", "技术部", 1L, 1, "0", "备注");
        given(systemDeptService.modifyById(vo)).willReturn(0);

        ResponseVO<Integer> result = deptController.modifyById(vo);

        assertThat(result.getCode()).isEqualTo(500);
        assertThat(result.getData()).isNull();
        assertThat(result.getMsg()).isEqualTo("修改失败");
    }

    // ==================== treeList ====================

    @Test
    void treeList_查询_返回树形列表() {
        SystemDeptQueryVO vo = new SystemDeptQueryVO();
        List<SystemDeptTreeVO> treeList = Collections.singletonList(buildTreeVO(1L, "技术部", null, 1));
        given(systemDeptService.getTreeList(vo)).willReturn(treeList);

        ResponseVO<List<SystemDeptTreeVO>> result = deptController.treeList(vo);

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData()).hasSize(1);
        assertThat(result.getData().get(0).getDeptName()).isEqualTo("技术部");
    }

    // ==================== 辅助方法 ====================

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

    private static SystemDeptUpdateVO buildUpdateVO(Long id, String deptCode, String deptName,
                                                     Long leaderId, Integer sort, String status, String remark) {
        SystemDeptUpdateVO vo = new SystemDeptUpdateVO();
        vo.setId(id);
        vo.setDeptCode(deptCode);
        vo.setDeptName(deptName);
        vo.setLeaderId(leaderId);
        vo.setSort(sort);
        vo.setStatus(status);
        vo.setRemark(remark);
        return vo;
    }

    private static SystemDeptTreeVO buildTreeVO(Long id, String deptName, Long parentId, Integer sort) {
        SystemDeptTreeVO vo = new SystemDeptTreeVO();
        vo.setId(id);
        vo.setDeptName(deptName);
        vo.setParentId(parentId);
        vo.setSort(sort);
        return vo;
    }
}
