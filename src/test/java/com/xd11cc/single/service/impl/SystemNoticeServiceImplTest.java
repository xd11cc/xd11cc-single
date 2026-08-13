package com.xd11cc.single.service.impl;

import com.xd11cc.single.config.exception.ServiceException;
import com.xd11cc.single.entity.domain.SystemNoticeDO;
import com.xd11cc.single.entity.domain.SystemNoticeUserDO;
import com.xd11cc.single.entity.domain.SystemUserDO;
import com.xd11cc.single.entity.dto.LoginUserDTO;
import com.xd11cc.single.entity.vo.SystemNoticeAddVO;
import com.xd11cc.single.entity.vo.SystemNoticeQueryVO;
import com.xd11cc.single.entity.vo.SystemNoticeUpdateVO;
import com.xd11cc.single.enums.NoticeScopeEnum;
import com.xd11cc.single.enums.NoticeStatusEnum;
import com.xd11cc.single.enums.SystemErrorEnum;
import com.xd11cc.single.mapper.SystemNoticeMapper;
import com.xd11cc.single.service.ISystemNoticeUserService;
import com.xd11cc.single.service.ISystemUserService;
import com.xd11cc.single.service.ISystemDeptService;
import com.xd11cc.single.util.BaseUnitTest;
import com.xd11cc.single.utils.SecurityUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DuplicateKeyException;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mockStatic;

@ExtendWith(MockitoExtension.class)
class SystemNoticeServiceImplTest extends BaseUnitTest {

    private static final Long SENDER_ID = 1L;
    private static final String SENDER_NAME = "管理员";

    @Mock
    private SystemNoticeMapper baseMapper;
    @Mock
    private ISystemNoticeUserService systemNoticeUserService;
    @Mock
    private ISystemUserService systemUserService;
    @Mock
    private ISystemDeptService systemDeptService;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private SystemNoticeServiceImpl noticeService;

    // ==================== add ====================

    @Test
    void add_成功_返回插入行数() {
        SystemNoticeAddVO vo = buildAddVO("测试通知", "内容", 1, NoticeScopeEnum.ALL.getCode(), null, null);
        given(baseMapper.insert(any(SystemNoticeDO.class))).willReturn(1);

        try (MockedStatic<SecurityUtils> mock = mockStatic(SecurityUtils.class)) {
            mock.when(SecurityUtils::getUserId).thenReturn(SENDER_ID);
            mock.when(SecurityUtils::getLoginUser).thenReturn(buildLoginUser(SENDER_NAME));

            int row = noticeService.add(vo);

            assertThat(row).isEqualTo(1);
            then(baseMapper).should().insert(any(SystemNoticeDO.class));
        }
    }

    @Test
    void add_重复键_抛异常() {
        SystemNoticeAddVO vo = buildAddVO("测试通知", "内容", 1, NoticeScopeEnum.ALL.getCode(), null, null);
        willThrow(new DuplicateKeyException("dup")).given(baseMapper).insert(any(SystemNoticeDO.class));

        try (MockedStatic<SecurityUtils> mock = mockStatic(SecurityUtils.class)) {
            mock.when(SecurityUtils::getUserId).thenReturn(SENDER_ID);
            mock.when(SecurityUtils::getLoginUser).thenReturn(buildLoginUser(SENDER_NAME));

            assertThrows(DuplicateKeyException.class, () -> noticeService.add(vo));
        }
    }

    // ==================== modifyById ====================

    @Test
    void modifyById_通知不存在_抛通知不存在() {
        SystemNoticeUpdateVO vo = buildUpdateVO(99L, "标题", "内容", 1, NoticeScopeEnum.ALL.getCode(), null, null);
        given(baseMapper.selectById(99L)).willReturn(null);

        ServiceException ex = assertThrows(ServiceException.class, () -> noticeService.modifyById(vo));
        assertThat(ex.getErrorCode()).isEqualTo(SystemErrorEnum.NOTICE_NOT_FOUND);
    }

    @Test
    void modifyById_非草稿状态_抛通知已发布() {
        SystemNoticeDO existing = buildNotice(1L, "标题", 1, NoticeScopeEnum.ALL.getCode(), NoticeStatusEnum.PUBLISHED.getCode(), null, null);
        given(baseMapper.selectById(1L)).willReturn(existing);

        SystemNoticeUpdateVO vo = buildUpdateVO(1L, "新标题", "新内容", 1, NoticeScopeEnum.ALL.getCode(), null, null);

        ServiceException ex = assertThrows(ServiceException.class, () -> noticeService.modifyById(vo));
        assertThat(ex.getErrorCode()).isEqualTo(SystemErrorEnum.NOTICE_ALREADY_PUBLISHED);
        then(baseMapper).should(org.mockito.BDDMockito.never()).updateById(any());
    }

    @Test
    void modifyById_成功_返回行数() {
        SystemNoticeDO existing = buildNotice(1L, "标题", 1, NoticeScopeEnum.ALL.getCode(), NoticeStatusEnum.DRAFT.getCode(), null, null);
        given(baseMapper.selectById(1L)).willReturn(existing);
        given(baseMapper.updateById(any(SystemNoticeDO.class))).willReturn(1);

        SystemNoticeUpdateVO vo = buildUpdateVO(1L, "新标题", "新内容", 1, NoticeScopeEnum.ALL.getCode(), null, null);

        int row = noticeService.modifyById(vo);

        assertThat(row).isEqualTo(1);
        then(baseMapper).should().updateById(any(SystemNoticeDO.class));
    }

    // ==================== deleteByIds ====================

    @Test
    void deleteByIds_返回删除行数() {
        given(baseMapper.deleteBatchIds(Arrays.asList(1L, 2L))).willReturn(2);

        int row = noticeService.deleteByIds(Arrays.asList(1L, 2L));

        assertThat(row).isEqualTo(2);
        then(baseMapper).should().deleteBatchIds(Arrays.asList(1L, 2L));
    }

    // ==================== getList ====================

    @Test
    void getList_带标题筛选_拼接like() {
        SystemNoticeQueryVO vo = new SystemNoticeQueryVO();
        vo.setTitle("测试");
        vo.setType(1);
        vo.setStatus(NoticeStatusEnum.DRAFT.getCode());
        SystemNoticeDO expected = buildNotice(1L, "测试通知", 1, NoticeScopeEnum.ALL.getCode(), NoticeStatusEnum.DRAFT.getCode(), null, null);
        given(baseMapper.selectList(any())).willReturn(Collections.singletonList(expected));

        List<SystemNoticeDO> result = noticeService.getList(vo);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("测试通知");
    }

    @Test
    void getList_带类型筛选_拼接eq() {
        SystemNoticeQueryVO vo = new SystemNoticeQueryVO();
        vo.setTitle(null);
        vo.setType(2);
        vo.setStatus(null);
        given(baseMapper.selectList(any())).willReturn(Collections.emptyList());

        List<SystemNoticeDO> result = noticeService.getList(vo);

        assertThat(result).isEmpty();
    }

    @Test
    void getList_空筛选_返回全部() {
        SystemNoticeQueryVO vo = new SystemNoticeQueryVO();
        SystemNoticeDO n1 = buildNotice(1L, "通知1", 1, NoticeScopeEnum.ALL.getCode(), 0, null, null);
        SystemNoticeDO n2 = buildNotice(2L, "通知2", 2, NoticeScopeEnum.DEPT.getCode(), 1, "1,2", null);
        given(baseMapper.selectList(any())).willReturn(Arrays.asList(n1, n2));

        List<SystemNoticeDO> result = noticeService.getList(vo);

        assertThat(result).hasSize(2);
    }

    // ==================== getDetailById ====================

    @Test
    void getDetailById_存在_返回详情() {
        SystemNoticeDO notice = buildNotice(1L, "标题", 1, NoticeScopeEnum.ALL.getCode(), 0, null, null);
        given(baseMapper.selectById(1L)).willReturn(notice);

        SystemNoticeDO result = noticeService.getDetailById(1L);

        assertThat(result).isSameAs(notice);
        assertThat(result.getTitle()).isEqualTo("标题");
    }

    @Test
    void getDetailById_不存在_返回null() {
        given(baseMapper.selectById(99L)).willReturn(null);

        SystemNoticeDO result = noticeService.getDetailById(99L);

        assertThat(result).isNull();
    }

    // ==================== publish ====================

    @Test
    void publish_草稿状态_更新为已发布并创建notice_user() {
        SystemNoticeDO draft = buildNotice(1L, "测试", 1, NoticeScopeEnum.USER.getCode(), NoticeStatusEnum.DRAFT.getCode(), null, "1,2,3");
        given(baseMapper.selectById(1L)).willReturn(draft);
        given(baseMapper.updateById(any(SystemNoticeDO.class))).willReturn(1);

        try (MockedStatic<SecurityUtils> mock = mockStatic(SecurityUtils.class)) {
            mock.when(SecurityUtils::getUserId).thenReturn(SENDER_ID);
            mock.when(SecurityUtils::getLoginUser).thenReturn(buildLoginUser(SENDER_NAME));

            noticeService.publish(1L);

            then(baseMapper).should().updateById(any(SystemNoticeDO.class));
            then(systemNoticeUserService).should().saveBatch(anyList(), any(Integer.class));
            then(eventPublisher).should().publishEvent(any());
        }
    }

    @Test
    void publish_草稿无目标用户_仅更新状态() {
        SystemNoticeDO draft = buildNotice(1L, "测试", 1, NoticeScopeEnum.USER.getCode(), NoticeStatusEnum.DRAFT.getCode(), null, "");
        given(baseMapper.selectById(1L)).willReturn(draft);
        given(baseMapper.updateById(any(SystemNoticeDO.class))).willReturn(1);

        try (MockedStatic<SecurityUtils> mock = mockStatic(SecurityUtils.class)) {
            mock.when(SecurityUtils::getUserId).thenReturn(SENDER_ID);
            mock.when(SecurityUtils::getLoginUser).thenReturn(buildLoginUser(SENDER_NAME));

            noticeService.publish(1L);

            then(baseMapper).should().updateById(any(SystemNoticeDO.class));
            then(systemNoticeUserService).should(org.mockito.BDDMockito.never()).saveBatch(anyList(), any(Integer.class));
            then(eventPublisher).should(org.mockito.BDDMockito.never()).publishEvent(any());
        }
    }

    @Test
    void publish_通知不存在_抛通知不存在() {
        given(baseMapper.selectById(99L)).willReturn(null);

        ServiceException ex = assertThrows(ServiceException.class, () -> noticeService.publish(99L));
        assertThat(ex.getErrorCode()).isEqualTo(SystemErrorEnum.NOTICE_NOT_FOUND);
        then(baseMapper).should(org.mockito.BDDMockito.never()).updateById(any());
        then(eventPublisher).should(org.mockito.BDDMockito.never()).publishEvent(any());
    }

    @Test
    void publish_已发布_抛通知已发布异常() {
        SystemNoticeDO published = buildNotice(1L, "测试", 1, NoticeScopeEnum.ALL.getCode(), NoticeStatusEnum.PUBLISHED.getCode(), null, null);
        given(baseMapper.selectById(1L)).willReturn(published);

        ServiceException ex = assertThrows(ServiceException.class, () -> noticeService.publish(1L));
        assertThat(ex.getErrorCode()).isEqualTo(SystemErrorEnum.NOTICE_ALREADY_PUBLISHED);
        then(baseMapper).should(org.mockito.BDDMockito.never()).updateById(any());
        then(eventPublisher).should(org.mockito.BDDMockito.never()).publishEvent(any());
    }

    // ==================== revoke ====================

    @Test
    void revoke_已发布状态_撤回并清理notice_user() {
        SystemNoticeDO published = buildNotice(1L, "测试", 1, NoticeScopeEnum.ALL.getCode(), NoticeStatusEnum.PUBLISHED.getCode(), null, null);
        given(baseMapper.selectById(1L)).willReturn(published);
        given(baseMapper.updateById(any(SystemNoticeDO.class))).willReturn(1);

        noticeService.revoke(1L);

        then(baseMapper).should().updateById(any(SystemNoticeDO.class));
        then(systemNoticeUserService).should().remove(any());
        then(eventPublisher).should().publishEvent(any());
    }

    @Test
    void revoke_通知不存在_抛通知不存在() {
        given(baseMapper.selectById(99L)).willReturn(null);

        ServiceException ex = assertThrows(ServiceException.class, () -> noticeService.revoke(99L));
        assertThat(ex.getErrorCode()).isEqualTo(SystemErrorEnum.NOTICE_NOT_FOUND);
        then(baseMapper).should(org.mockito.BDDMockito.never()).updateById(any());
        then(eventPublisher).should(org.mockito.BDDMockito.never()).publishEvent(any());
    }

    @Test
    void revoke_草稿状态_抛通知未发布异常() {
        SystemNoticeDO draft = buildNotice(1L, "测试", 1, NoticeScopeEnum.ALL.getCode(), NoticeStatusEnum.DRAFT.getCode(), null, null);
        given(baseMapper.selectById(1L)).willReturn(draft);

        ServiceException ex = assertThrows(ServiceException.class, () -> noticeService.revoke(1L));
        assertThat(ex.getErrorCode()).isEqualTo(SystemErrorEnum.NOTICE_NOT_PUBLISHED);
        then(baseMapper).should(org.mockito.BDDMockito.never()).updateById(any());
        then(systemNoticeUserService).should(org.mockito.BDDMockito.never()).remove(any());
        then(eventPublisher).should(org.mockito.BDDMockito.never()).publishEvent(any());
    }

    // ==================== 辅助方法 ====================

    private static LoginUserDTO buildLoginUser(String nickname) {
        SystemUserDO user = new SystemUserDO();
        user.setId(SENDER_ID);
        user.setNickname(nickname);
        user.setUsername("admin");
        LoginUserDTO dto = new LoginUserDTO();
        dto.setUserId(SENDER_ID);
        dto.setSystemUserDO(user);
        return dto;
    }

    private static SystemNoticeDO buildNotice(Long id, String title, Integer type, Integer scope,
                                               Integer status, String scopeDeptIds, String scopeUserIds) {
        SystemNoticeDO notice = new SystemNoticeDO();
        notice.setId(id);
        notice.setTitle(title);
        notice.setContent("内容");
        notice.setType(type);
        notice.setScope(scope);
        notice.setScopeDeptIds(scopeDeptIds);
        notice.setScopeUserIds(scopeUserIds);
        notice.setSenderId(SENDER_ID);
        notice.setSenderName(SENDER_NAME);
        notice.setStatus(status);
        notice.setPublishTime(status != null && status == 1 ? new Date() : null);
        notice.setRemark("备注");
        return notice;
    }

    private static SystemNoticeAddVO buildAddVO(String title, String content, Integer type,
                                                Integer scope, List<Long> scopeDeptIds,
                                                List<Long> scopeUserIds) {
        SystemNoticeAddVO vo = new SystemNoticeAddVO();
        vo.setTitle(title);
        vo.setContent(content);
        vo.setType(type);
        vo.setScope(scope);
        vo.setScopeDeptIds(scopeDeptIds);
        vo.setScopeUserIds(scopeUserIds);
        vo.setRemark("备注");
        return vo;
    }

    private static SystemNoticeUpdateVO buildUpdateVO(Long id, String title, String content, Integer type,
                                                       Integer scope, List<Long> scopeDeptIds,
                                                       List<Long> scopeUserIds) {
        SystemNoticeUpdateVO vo = new SystemNoticeUpdateVO();
        vo.setId(id);
        vo.setTitle(title);
        vo.setContent(content);
        vo.setType(type);
        vo.setScope(scope);
        vo.setScopeDeptIds(scopeDeptIds);
        vo.setScopeUserIds(scopeUserIds);
        vo.setRemark("备注");
        return vo;
    }
}
