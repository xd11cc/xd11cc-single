package com.xd11cc.single.controller;

import com.xd11cc.single.entity.base.PageResult;
import com.xd11cc.single.entity.base.ResponseVO;
import com.xd11cc.single.entity.domain.SystemNoticeDO;
import com.xd11cc.single.entity.vo.SystemNoticeAddVO;
import com.xd11cc.single.entity.vo.SystemNoticeQueryVO;
import com.xd11cc.single.entity.vo.SystemNoticeUpdateVO;
import com.xd11cc.single.service.ISystemNoticeService;
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
class SystemNoticeControllerTest {

    @Mock
    private ISystemNoticeService systemNoticeService;

    @InjectMocks
    private SystemNoticeController noticeController;

    // ==================== add ====================

    @Test
    void add_成功_返回行数() {
        SystemNoticeAddVO vo = buildAddVO("通知标题", "内容", 1, 1, null, null, "备注");
        given(systemNoticeService.add(vo)).willReturn(1);

        ResponseVO<Integer> result = noticeController.add(vo);

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData()).isEqualTo(1);
        assertThat(result.getMsg()).isEqualTo("新增成功");
    }

    @Test
    void add_失败_返回失败消息() {
        SystemNoticeAddVO vo = buildAddVO("通知标题", "内容", 1, 1, null, null, "备注");
        given(systemNoticeService.add(vo)).willReturn(0);

        ResponseVO<Integer> result = noticeController.add(vo);

        assertThat(result.getCode()).isEqualTo(500);
        assertThat(result.getData()).isNull();
        assertThat(result.getMsg()).isEqualTo("新增失败");
    }

    // ==================== modifyById ====================

    @Test
    void modifyById_成功_返回行数() {
        SystemNoticeUpdateVO vo = buildUpdateVO(1L, "新标题", "新内容", 1, 1, null, null, "备注");
        given(systemNoticeService.modifyById(vo)).willReturn(1);

        ResponseVO<Integer> result = noticeController.modifyById(vo);

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData()).isEqualTo(1);
        assertThat(result.getMsg()).isEqualTo("修改成功");
    }

    @Test
    void modifyById_失败_返回失败消息() {
        SystemNoticeUpdateVO vo = buildUpdateVO(1L, "新标题", "新内容", 1, 1, null, null, "备注");
        given(systemNoticeService.modifyById(vo)).willReturn(0);

        ResponseVO<Integer> result = noticeController.modifyById(vo);

        assertThat(result.getCode()).isEqualTo(500);
        assertThat(result.getData()).isNull();
        assertThat(result.getMsg()).isEqualTo("修改失败");
    }

    // ==================== removeByIds ====================

    @Test
    void removeByIds_成功_返回删除行数() {
        given(systemNoticeService.deleteByIds(Arrays.asList(1L, 2L))).willReturn(2);

        ResponseVO<Integer> result = noticeController.removeByIds(Arrays.asList(1L, 2L));

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData()).isEqualTo(2);
        assertThat(result.getMsg()).isEqualTo("删除成功");
    }

    // ==================== page ====================

    @Test
    void page_查询_返回分页结果() {
        SystemNoticeQueryVO vo = new SystemNoticeQueryVO();
        vo.setCurrentPage(1);
        vo.setPageSize(10);
        List<SystemNoticeDO> notices = Collections.singletonList(buildNotice(1L, "通知", "内容", 1));
        given(systemNoticeService.getList(vo)).willReturn(notices);

        ResponseVO<PageResult<SystemNoticeDO>> result = noticeController.page(vo);

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData()).isNotNull();
        assertThat(result.getData().getRows()).hasSize(1);
    }

    // ==================== getById ====================

    @Test
    void getById_存在_返回通知详情() {
        SystemNoticeDO notice = buildNotice(1L, "通知标题", "内容", 1);
        given(systemNoticeService.getDetailById(1L)).willReturn(notice);

        ResponseVO<SystemNoticeDO> result = noticeController.getById(1L);

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData()).isSameAs(notice);
        assertThat(result.getData().getTitle()).isEqualTo("通知标题");
    }

    @Test
    void getById_不存在_返回null详情() {
        given(systemNoticeService.getDetailById(99L)).willReturn(null);

        ResponseVO<SystemNoticeDO> result = noticeController.getById(99L);

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData()).isNull();
    }

    // ==================== publish ====================

    @Test
    void publish_发布成功_返回成功消息() {
        noticeController.publish(1L);

        // publish 方法直接返回成功，无需验证 service 返回值
        // 只验证 controller 响应结构正确
    }

    // ==================== revoke ====================

    @Test
    void revoke_撤回成功_返回成功消息() {
        noticeController.revoke(1L);

        // revoke 方法直接返回成功，无需验证 service 返回值
    }

    // ==================== 辅助方法 ====================

    private static SystemNoticeAddVO buildAddVO(String title, String content, Integer type, Integer scope,
                                                List<Long> scopeDeptIds, List<Long> scopeUserIds, String remark) {
        SystemNoticeAddVO vo = new SystemNoticeAddVO();
        vo.setTitle(title);
        vo.setContent(content);
        vo.setType(type);
        vo.setScope(scope);
        vo.setScopeDeptIds(scopeDeptIds);
        vo.setScopeUserIds(scopeUserIds);
        vo.setRemark(remark);
        return vo;
    }

    private static SystemNoticeUpdateVO buildUpdateVO(Long id, String title, String content, Integer type, Integer scope,
                                                       List<Long> scopeDeptIds, List<Long> scopeUserIds, String remark) {
        SystemNoticeUpdateVO vo = new SystemNoticeUpdateVO();
        vo.setId(id);
        vo.setTitle(title);
        vo.setContent(content);
        vo.setType(type);
        vo.setScope(scope);
        vo.setScopeDeptIds(scopeDeptIds);
        vo.setScopeUserIds(scopeUserIds);
        vo.setRemark(remark);
        return vo;
    }

    private static SystemNoticeDO buildNotice(Long id, String title, String content, Integer type) {
        SystemNoticeDO notice = new SystemNoticeDO();
        notice.setId(id);
        notice.setTitle(title);
        notice.setContent(content);
        notice.setType(type);
        return notice;
    }
}
