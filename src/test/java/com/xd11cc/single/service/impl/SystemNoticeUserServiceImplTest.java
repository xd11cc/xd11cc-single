package com.xd11cc.single.service.impl;

import com.xd11cc.single.entity.dto.SystemNoticeUserDTO;
import com.xd11cc.single.entity.vo.SystemNoticeUserQueryVO;
import com.xd11cc.single.enums.NoticeTypeEnum;
import com.xd11cc.single.mapper.SystemNoticeUserMapper;
import com.xd11cc.single.util.BaseUnitTest;
import com.xd11cc.single.utils.SecurityUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mockStatic;

@ExtendWith(MockitoExtension.class)
class SystemNoticeUserServiceImplTest extends BaseUnitTest {

    private static final Long USER_ID = 1L;

    @Mock
    private SystemNoticeUserMapper baseMapper;

    @InjectMocks
    private SystemNoticeUserServiceImpl noticeUserService;

    // ==================== getMyNoticeList ====================

    @Test
    void getMyNoticeList_返回当前用户的通知列表() {
        try (MockedStatic<SecurityUtils> mock = mockStatic(SecurityUtils.class)) {
            mock.when(SecurityUtils::getUserId).thenReturn(USER_ID);
            SystemNoticeUserDTO dto = buildDTO(1L, 1L, 0, "通知标题", "内容", NoticeTypeEnum.NOTICE.getCode(), "发送人", new Date());
            given(baseMapper.selectMyNoticeList(USER_ID, 1, 0, "标题")).willReturn(Collections.singletonList(dto));

            List<SystemNoticeUserDTO> result = noticeUserService.getMyNoticeList(buildQueryVO(1, 0, "标题"));

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getType()).isEqualTo(NoticeTypeEnum.NOTICE.getCode());
            assertThat(result.get(0).getTitle()).isEqualTo("通知标题");
            then(baseMapper).should().selectMyNoticeList(USER_ID, 1, 0, "标题");
        }
    }

    @Test
    void getMyNoticeList_无记录_返回空列表() {
        try (MockedStatic<SecurityUtils> mock = mockStatic(SecurityUtils.class)) {
            mock.when(SecurityUtils::getUserId).thenReturn(USER_ID);
            given(baseMapper.selectMyNoticeList(USER_ID, null, null, null)).willReturn(Collections.emptyList());

            List<SystemNoticeUserDTO> result = noticeUserService.getMyNoticeList(buildQueryVO(null, null, null));

            assertThat(result).isEmpty();
        }
    }

    // ==================== send ====================
    // send 方法内部调用 saveBatch（IService）触达 baseMapper.insert()，
    // 在 baseMapper mock 场景下构建 Lambda 表达式即抛 "Not Found TableInfoCache"；
    // 该路径涉及事务提交 + 事件发布，更适合在集成测试中覆盖。
    // 此处仅保留不触发 TableInfoCache 的方法单测。

    // ==================== getUnreadCount ====================

    @Test
    void getUnreadCount_返回各类型未读数() {
        try (MockedStatic<SecurityUtils> mock = mockStatic(SecurityUtils.class)) {
            mock.when(SecurityUtils::getUserId).thenReturn(USER_ID);
            List<Map<String, Object>> countList = Arrays.asList(
                    row(NoticeTypeEnum.NOTICE.getCode(), 2),
                    row(NoticeTypeEnum.MESSAGE.getCode(), 5),
                    row(NoticeTypeEnum.TODO.getCode(), 1));
            given(baseMapper.selectUnreadCount(USER_ID)).willReturn(countList);

            Map<String, Integer> result = noticeUserService.getUnreadCount();

            assertThat(result).containsEntry("notice", 2);
            assertThat(result).containsEntry("message", 5);
            assertThat(result).containsEntry("todo", 1);
        }
    }

    @Test
    void getUnreadCount_全部已读_各类型为0() {
        try (MockedStatic<SecurityUtils> mock = mockStatic(SecurityUtils.class)) {
            mock.when(SecurityUtils::getUserId).thenReturn(USER_ID);
            given(baseMapper.selectUnreadCount(USER_ID)).willReturn(Collections.emptyList());

            Map<String, Integer> result = noticeUserService.getUnreadCount();

            assertThat(result).containsEntry("notice", 0);
            assertThat(result).containsEntry("message", 0);
            assertThat(result).containsEntry("todo", 0);
        }
    }

    // ==================== 辅助方法 ====================

    private static SystemNoticeUserQueryVO buildQueryVO(Integer type, Integer readStatus, String title) {
        SystemNoticeUserQueryVO vo = new SystemNoticeUserQueryVO();
        vo.setType(type);
        vo.setReadStatus(readStatus);
        vo.setTitle(title);
        return vo;
    }

    private static SystemNoticeUserDTO buildDTO(Long id, Long noticeId, Integer readStatus,
                                                 String title, String content, Integer type,
                                                 String senderName, Date publishTime) {
        SystemNoticeUserDTO dto = new SystemNoticeUserDTO();
        dto.setId(id);
        dto.setNoticeId(noticeId);
        dto.setReadStatus(readStatus);
        dto.setReadTime(readStatus == 1 ? new Date() : null);
        dto.setTitle(title);
        dto.setContent(content);
        dto.setType(type);
        dto.setSenderName(senderName);
        dto.setPublishTime(publishTime);
        return dto;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> row(Integer typeCode, Integer count) {
        Map<String, Object> map = new java.util.HashMap<>();
        map.put("type", typeCode);
        map.put("count", count);
        return map;
    }
}
