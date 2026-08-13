package com.xd11cc.single.controller;

import com.xd11cc.single.entity.base.PageResult;
import com.xd11cc.single.entity.base.ResponseVO;
import com.xd11cc.single.entity.domain.SystemPostDO;
import com.xd11cc.single.entity.vo.SystemPostAddVO;
import com.xd11cc.single.entity.vo.SystemPostQueryVO;
import com.xd11cc.single.entity.vo.SystemPostUpdateVO;
import com.xd11cc.single.service.ISystemPostService;
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
class SystemPostControllerTest {

    @Mock
    private ISystemPostService systemPostService;

    @InjectMocks
    private SystemPostController postController;

    // ==================== add ====================

    @Test
    void add_成功_返回行数() {
        SystemPostAddVO vo = buildAddVO("CODE", "岗位名称", "0", "备注");
        given(systemPostService.add(vo)).willReturn(1);

        ResponseVO<Integer> result = postController.add(vo);

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData()).isEqualTo(1);
        assertThat(result.getMsg()).isEqualTo("新增成功");
    }

    @Test
    void add_失败_返回失败消息() {
        SystemPostAddVO vo = buildAddVO("CODE", "岗位名称", "0", "备注");
        given(systemPostService.add(vo)).willReturn(0);

        ResponseVO<Integer> result = postController.add(vo);

        assertThat(result.getCode()).isEqualTo(500);
        assertThat(result.getData()).isNull();
        assertThat(result.getMsg()).isEqualTo("新增失败");
    }

    // ==================== removeByIds ====================

    @Test
    void removeByIds_成功_返回删除行数() {
        given(systemPostService.deleteByIds(Arrays.asList(1L, 2L))).willReturn(2);

        ResponseVO<Integer> result = postController.removeByIds(Arrays.asList(1L, 2L));

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData()).isEqualTo(2);
        assertThat(result.getMsg()).isEqualTo("删除成功");
    }

    @Test
    void removeByIds_失败_返回失败消息() {
        given(systemPostService.deleteByIds(Collections.singletonList(1L))).willReturn(0);

        ResponseVO<Integer> result = postController.removeByIds(Collections.singletonList(1L));

        assertThat(result.getCode()).isEqualTo(500);
        assertThat(result.getData()).isNull();
        assertThat(result.getMsg()).isEqualTo("删除失败");
    }

    // ==================== modifyById ====================

    @Test
    void modifyById_成功_返回行数() {
        SystemPostUpdateVO vo = buildUpdateVO(1L, "NEW_CODE", "新名称", "0", "备注");
        given(systemPostService.modifyById(vo)).willReturn(1);

        ResponseVO<Integer> result = postController.modifyById(vo);

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData()).isEqualTo(1);
        assertThat(result.getMsg()).isEqualTo("修改成功");
    }

    @Test
    void modifyById_失败_返回失败消息() {
        SystemPostUpdateVO vo = buildUpdateVO(1L, "NEW_CODE", "新名称", "0", "备注");
        given(systemPostService.modifyById(vo)).willReturn(0);

        ResponseVO<Integer> result = postController.modifyById(vo);

        assertThat(result.getCode()).isEqualTo(500);
        assertThat(result.getData()).isNull();
        assertThat(result.getMsg()).isEqualTo("修改失败");
    }

    // ==================== page ====================

    @Test
    void page_查询_返回分页结果() {
        SystemPostQueryVO vo = new SystemPostQueryVO();
        vo.setCurrentPage(1);
        vo.setPageSize(10);
        List<SystemPostDO> posts = Collections.singletonList(buildPost(1L, "CODE", "岗位", "0", "备注"));
        given(systemPostService.getList(vo)).willReturn(posts);

        ResponseVO<PageResult<SystemPostDO>> result = postController.page(vo);

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData()).isNotNull();
        assertThat(result.getData().getRows()).hasSize(1);
    }

    // ==================== getDeptIds ====================

    @Test
    void getDeptIds_查询岗位已分配部门id() {
        given(systemPostService.getDeptIdsByPostId(1L)).willReturn(Arrays.asList(10L, 20L));

        ResponseVO<List<Long>> result = postController.getDeptIds(1L);

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData()).containsExactlyInAnyOrder(10L, 20L);
    }

    // ==================== listByDeptId ====================

    @Test
    void listByDeptId_查询部门下岗位() {
        List<SystemPostDO> posts = Arrays.asList(buildPost(1L, "CODE1", "岗位1", "0", "备注1"),
                buildPost(2L, "CODE2", "岗位2", "1", "备注2"));
        given(systemPostService.getListByDeptId(1L)).willReturn(posts);

        ResponseVO<List<SystemPostDO>> result = postController.listByDeptId(1L);

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData()).hasSize(2);
        assertThat(result.getData().get(0).getPostCode()).isEqualTo("CODE1");
    }

    // ==================== 辅助方法 ====================

    private static SystemPostAddVO buildAddVO(String code, String name, String status, String remark) {
        SystemPostAddVO vo = new SystemPostAddVO();
        vo.setPostCode(code);
        vo.setPostName(name);
        vo.setStatus(status);
        vo.setRemark(remark);
        return vo;
    }

    private static SystemPostUpdateVO buildUpdateVO(Long id, String code, String name, String status, String remark) {
        SystemPostUpdateVO vo = new SystemPostUpdateVO();
        vo.setId(id);
        vo.setPostCode(code);
        vo.setPostName(name);
        vo.setStatus(status);
        vo.setRemark(remark);
        return vo;
    }

    private static SystemPostDO buildPost(Long id, String code, String name, String status, String remark) {
        SystemPostDO post = new SystemPostDO();
        post.setId(id);
        post.setPostCode(code);
        post.setPostName(name);
        post.setStatus(status);
        post.setRemark(remark);
        return post;
    }
}
