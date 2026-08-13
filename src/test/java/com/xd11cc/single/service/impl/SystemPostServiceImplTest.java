package com.xd11cc.single.service.impl;

import com.xd11cc.single.config.exception.ServiceException;
import com.xd11cc.single.entity.domain.SystemDeptPostDO;
import com.xd11cc.single.entity.domain.SystemPostDO;
import com.xd11cc.single.entity.vo.SystemPostAddVO;
import com.xd11cc.single.entity.vo.SystemPostQueryVO;
import com.xd11cc.single.entity.vo.SystemPostUpdateVO;
import com.xd11cc.single.enums.SystemErrorEnum;
import com.xd11cc.single.mapper.SystemPostMapper;
import com.xd11cc.single.service.ISystemDeptPostService;
import com.xd11cc.single.service.ISystemPostService;
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
class SystemPostServiceImplTest extends BaseUnitTest {

    @Mock
    private SystemPostMapper baseMapper;
    @Mock
    private ISystemDeptPostService systemDeptPostService;

    @InjectMocks
    private SystemPostServiceImpl postService;

    // ==================== add ====================

    @Test
    void add_成功_写入并保存部门岗位关联() {
        SystemPostAddVO vo = buildAddVO("dev", "开发岗", "0", Arrays.asList(1L, 2L));
        given(baseMapper.insert(any(SystemPostDO.class))).willReturn(1);

        int row = postService.add(vo);

        assertThat(row).isEqualTo(1);
        then(baseMapper).should().insert(any(SystemPostDO.class));
        then(systemDeptPostService).should().saveBatch(anyList());
    }

    @Test
    void add_重复键_抛岗位编码已存在() {
        SystemPostAddVO vo = buildAddVO("dup", "重复", "0", null);
        willThrow(new DuplicateKeyException("dup")).given(baseMapper).insert(any(SystemPostDO.class));

        ServiceException ex = assertThrows(ServiceException.class, () -> postService.add(vo));
        assertThat(ex.getErrorCode()).isEqualTo(SystemErrorEnum.POST_CODE_EXISTS);
        then(systemDeptPostService).should(org.mockito.BDDMockito.never()).saveBatch(anyList());
    }

    @Test
    void add_空部门列表_仅写入岗位() {
        SystemPostAddVO vo = buildAddVO("simple", "简单", "0", Collections.emptyList());
        given(baseMapper.insert(any(SystemPostDO.class))).willReturn(1);

        int row = postService.add(vo);

        assertThat(row).isEqualTo(1);
        then(systemDeptPostService).should(org.mockito.BDDMockito.never()).saveBatch(anyList());
    }

    // ==================== deleteByIds ====================

    @Test
    void deleteByIds_成功_删除并清理部门岗位关联() {
        given(baseMapper.deleteBatchIds(Arrays.asList(2L, 3L))).willReturn(2);

        int row = postService.deleteByIds(Arrays.asList(2L, 3L));

        assertThat(row).isEqualTo(2);
        then(baseMapper).should().deleteBatchIds(Arrays.asList(2L, 3L));
        then(systemDeptPostService).should().remove(any());
    }

    // ==================== modifyById ====================

    @Test
    void modifyById_成功_更新岗位并更新部门关联() {
        SystemPostUpdateVO vo = buildUpdateVO(1L, "new_code", "新岗位", "0", Arrays.asList(10L, 20L));
        given(baseMapper.updateById(any(SystemPostDO.class))).willReturn(1);

        int row = postService.modifyById(vo);

        assertThat(row).isEqualTo(1);
        then(baseMapper).should().updateById(any(SystemPostDO.class));
        then(systemDeptPostService).should().remove(any());
        then(systemDeptPostService).should().saveBatch(anyList());
    }

    @Test
    void modifyById_重复键_抛岗位编码已存在() {
        SystemPostUpdateVO vo = buildUpdateVO(1L, "dup", "重复", "0", Collections.emptyList());
        willThrow(new DuplicateKeyException("dup")).given(baseMapper).updateById(any(SystemPostDO.class));

        ServiceException ex = assertThrows(ServiceException.class, () -> postService.modifyById(vo));
        assertThat(ex.getErrorCode()).isEqualTo(SystemErrorEnum.POST_CODE_EXISTS);
        then(systemDeptPostService).should(org.mockito.BDDMockito.never()).remove(any());
        then(systemDeptPostService).should(org.mockito.BDDMockito.never()).saveBatch(anyList());
    }

    @Test
    void modifyById_空部门列表_删除旧关联() {
        SystemPostUpdateVO vo = buildUpdateVO(1L, "code", "岗位", "0", Collections.emptyList());
        given(baseMapper.updateById(any(SystemPostDO.class))).willReturn(1);

        int row = postService.modifyById(vo);

        assertThat(row).isEqualTo(1);
        then(systemDeptPostService).should().remove(any());
        then(systemDeptPostService).should(org.mockito.BDDMockito.never()).saveBatch(anyList());
    }

    // ==================== getList ====================

    @Test
    void getList_带筛选_返回结果() {
        SystemPostQueryVO vo = new SystemPostQueryVO();
        vo.setPostCode("dev");
        vo.setPostName("开发");
        vo.setStatus("0");
        SystemPostDO expected = buildPost(1L, "dev", "开发岗", "0");
        given(baseMapper.selectList(any())).willReturn(Collections.singletonList(expected));

        List<SystemPostDO> result = postService.getList(vo);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPostCode()).isEqualTo("dev");
    }

    @Test
    void getList_空筛选_返回全部() {
        SystemPostQueryVO vo = new SystemPostQueryVO();
        SystemPostDO p1 = buildPost(1L, "dev", "开发", "0");
        SystemPostDO p2 = buildPost(2L, "test", "测试", "0");
        given(baseMapper.selectList(any())).willReturn(Arrays.asList(p1, p2));

        List<SystemPostDO> result = postService.getList(vo);

        assertThat(result).hasSize(2);
    }

    // ==================== getDeptIdsByPostId ====================

    @Test
    void getDeptIdsByPostId_有关联_返回部门id列表() {
        SystemDeptPostDO dp1 = buildDeptPost(1L, 1L, 10L);
        SystemDeptPostDO dp2 = buildDeptPost(2L, 1L, 20L);
        given(systemDeptPostService.list(any())).willReturn(Arrays.asList(dp1, dp2));

        List<Long> deptIds = postService.getDeptIdsByPostId(1L);

        assertThat(deptIds).containsExactly(10L, 20L);
    }

    @Test
    void getDeptIdsByPostId_无关联_返回空列表() {
        given(systemDeptPostService.list(any())).willReturn(Collections.emptyList());

        List<Long> deptIds = postService.getDeptIdsByPostId(99L);

        assertThat(deptIds).isEmpty();
    }

    // ==================== getListByDeptId ====================

    @Test
    void getListByDeptId_有关联_返回岗位列表() {
        SystemDeptPostDO dp = buildDeptPost(1L, 10L, 1L); // postId=10L, deptId=1L
        given(systemDeptPostService.list(any())).willReturn(Collections.singletonList(dp));
        SystemPostDO post = buildPost(10L, "dev", "开发", "0");
        given(baseMapper.selectBatchIds(Collections.singletonList(10L))).willReturn(Collections.singletonList(post));

        List<SystemPostDO> result = postService.getListByDeptId(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPostCode()).isEqualTo("dev");
        then(baseMapper).should().selectBatchIds(Collections.singletonList(10L));
    }

    @Test
    void getListByDeptId_无关联_返回空列表() {
        given(systemDeptPostService.list(any())).willReturn(Collections.emptyList());

        List<SystemPostDO> result = postService.getListByDeptId(99L);

        assertThat(result).isEmpty();
        then(baseMapper).should(org.mockito.BDDMockito.never()).selectBatchIds(any());
    }

    // ==================== 辅助方法 ====================

    private static SystemPostDO buildPost(Long id, String postCode, String postName, String status) {
        SystemPostDO post = new SystemPostDO();
        post.setId(id);
        post.setPostCode(postCode);
        post.setPostName(postName);
        post.setStatus(status);
        post.setRemark("备注");
        return post;
    }

    private static SystemPostAddVO buildAddVO(String postCode, String postName, String status, List<Long> deptIds) {
        SystemPostAddVO vo = new SystemPostAddVO();
        vo.setPostCode(postCode);
        vo.setPostName(postName);
        vo.setStatus(status);
        vo.setDeptIds(deptIds);
        vo.setRemark("备注");
        return vo;
    }

    private static SystemPostUpdateVO buildUpdateVO(Long id, String postCode, String postName, String status, List<Long> deptIds) {
        SystemPostUpdateVO vo = new SystemPostUpdateVO();
        vo.setId(id);
        vo.setPostCode(postCode);
        vo.setPostName(postName);
        vo.setStatus(status);
        vo.setDeptIds(deptIds);
        vo.setRemark("备注");
        return vo;
    }

    private static SystemDeptPostDO buildDeptPost(Long id, Long postId, Long deptId) {
        SystemDeptPostDO dp = new SystemDeptPostDO();
        dp.setId(id);
        dp.setPostId(postId);
        dp.setDeptId(deptId);
        dp.setRemark("关联");
        return dp;
    }
}
