package com.xd11cc.single.controller;

import com.xd11cc.single.entity.base.PageResult;
import com.xd11cc.single.entity.base.ResponseVO;
import com.xd11cc.single.entity.dto.SystemNoticeUserDTO;
import com.xd11cc.single.entity.vo.SystemNoticeSendVO;
import com.xd11cc.single.entity.vo.SystemNoticeUserQueryVO;
import com.xd11cc.single.service.ISystemNoticeUserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class SystemNoticeUserControllerTest {

    @Mock
    private ISystemNoticeUserService systemNoticeUserService;

    @InjectMocks
    private SystemNoticeUserController noticeUserController;

    // ==================== myPage ====================

    @Test
    void myPage_查询_返回分页结果() {
        SystemNoticeUserQueryVO vo = new SystemNoticeUserQueryVO();
        vo.setCurrentPage(1);
        vo.setPageSize(10);
        List<SystemNoticeUserDTO> dtos = Collections.singletonList(buildDTO(1L, "标题", "内容", 1, 0));
        given(systemNoticeUserService.getMyNoticeList(vo)).willReturn(dtos);

        ResponseVO<PageResult<SystemNoticeUserDTO>> result = noticeUserController.myPage(vo);

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData()).isNotNull();
        assertThat(result.getData().getRows()).hasSize(1);
    }

    // ==================== send ====================

    @Test
    void send_发送用户间消息_返回成功() {
        SystemNoticeSendVO vo = new SystemNoticeSendVO();
        vo.setTitle("消息标题");
        vo.setContent("消息内容");
        vo.setReceiverIds(Arrays.asList(1L, 2L));

        ResponseVO<Void> result = noticeUserController.send(vo);

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData()).isNull();
        assertThat(result.getMsg()).isEqualTo("发送成功");
    }

    // ==================== markAsRead ====================

    @Test
    void markAsRead_标记已读_返回成功() {
        ResponseVO<Void> result = noticeUserController.markAsRead(1L);

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData()).isNull();
        assertThat(result.getMsg()).isEqualTo("操作成功");
    }

    // ==================== markAllAsRead ====================

    @Test
    void markAllAsRead_全部标记已读_返回成功() {
        ResponseVO<Void> result = noticeUserController.markAllAsRead(1);

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData()).isNull();
        assertThat(result.getMsg()).isEqualTo("操作成功");
    }

    @Test
    void markAllAsRead_全部标记已读_类型为空_返回成功() {
        ResponseVO<Void> result = noticeUserController.markAllAsRead(null);

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData()).isNull();
        assertThat(result.getMsg()).isEqualTo("操作成功");
    }

    // ==================== unreadCount ====================

    @Test
    void unreadCount_未读数统计_返回统计结果() {
        Map<String, Integer> countMap = Collections.singletonMap("unreadCount", 3);
        given(systemNoticeUserService.getUnreadCount()).willReturn(countMap);

        ResponseVO<Map<String, Integer>> result = noticeUserController.unreadCount();

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData()).hasSize(1);
        assertThat(result.getData().get("unreadCount")).isEqualTo(3);
    }

    // ==================== 辅助方法 ====================

    private static SystemNoticeUserDTO buildDTO(Long id, String title, String content, Integer type, Integer readStatus) {
        SystemNoticeUserDTO dto = new SystemNoticeUserDTO();
        dto.setId(id);
        dto.setTitle(title);
        dto.setContent(content);
        dto.setType(type);
        dto.setReadStatus(readStatus);
        return dto;
    }
}
