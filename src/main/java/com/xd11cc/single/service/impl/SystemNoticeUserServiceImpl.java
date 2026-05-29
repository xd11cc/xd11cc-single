package com.xd11cc.single.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xd11cc.single.entity.domain.SystemNoticeDO;
import com.xd11cc.single.entity.domain.SystemNoticeUserDO;
import com.xd11cc.single.entity.dto.SystemNoticeUserDTO;
import com.xd11cc.single.entity.vo.SystemNoticeSendVO;
import com.xd11cc.single.entity.vo.SystemNoticeUserQueryVO;
import com.xd11cc.single.enums.NoticeScopeEnum;
import com.xd11cc.single.enums.NoticeStatusEnum;
import com.xd11cc.single.enums.NoticeTypeEnum;
import com.xd11cc.single.config.event.NoticeEvent;
import com.xd11cc.single.mapper.SystemNoticeMapper;
import com.xd11cc.single.mapper.SystemNoticeUserMapper;
import com.xd11cc.single.service.ISystemNoticeUserService;
import com.xd11cc.single.utils.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author xd11cc
 * @date 2026-05-29
 */
@Slf4j
@Service
public class SystemNoticeUserServiceImpl extends ServiceImpl<SystemNoticeUserMapper, SystemNoticeUserDO> implements ISystemNoticeUserService {

    @Autowired
    private SystemNoticeMapper systemNoticeMapper;
    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Override
    public List<SystemNoticeUserDTO> getMyNoticeList(SystemNoticeUserQueryVO queryVO) {
        Long userId = SecurityUtils.getUserId();
        return baseMapper.selectMyNoticeList(userId, queryVO.getType(),
                queryVO.getReadStatus(), queryVO.getTitle());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void send(SystemNoticeSendVO sendVO) {
        Long senderId = SecurityUtils.getUserId();
        String senderName = SecurityUtils.getLoginUser().getSystemUserDO().getNickname();

        // 创建消息记录
        SystemNoticeDO notice = new SystemNoticeDO();
        notice.setTitle(sendVO.getTitle());
        notice.setContent(sendVO.getContent());
        notice.setType(NoticeTypeEnum.MESSAGE.getCode());
        notice.setScope(NoticeScopeEnum.USER.getCode());
        notice.setScopeUserIds(sendVO.getReceiverIds().stream()
                .map(String::valueOf).collect(Collectors.joining(",")));
        notice.setSenderId(senderId);
        notice.setSenderName(senderName);
        notice.setStatus(NoticeStatusEnum.PUBLISHED.getCode());
        notice.setPublishTime(new Date());
        systemNoticeMapper.insert(notice);

        // 创建接收记录
        List<SystemNoticeUserDO> noticeUserList = sendVO.getReceiverIds().stream()
                .map(userId -> {
                    SystemNoticeUserDO nu = new SystemNoticeUserDO();
                    nu.setNoticeId(notice.getId());
                    nu.setUserId(userId);
                    nu.setReadStatus(0);
                    return nu;
                }).collect(Collectors.toList());
        saveBatch(noticeUserList, 500);

        // 发布事件（事务提交后由 NoticeEventListener 异步推送）
        eventPublisher.publishEvent(NoticeEvent.publish(this, notice.getId(),
                notice.getTitle(), notice.getType(), NoticeScopeEnum.USER.getCode(),
                null, senderName, sendVO.getReceiverIds()));
    }

    @Override
    public void markAsRead(Long noticeId) {
        Long userId = SecurityUtils.getUserId();
        LambdaUpdateWrapper<SystemNoticeUserDO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(SystemNoticeUserDO::getNoticeId, noticeId);
        wrapper.eq(SystemNoticeUserDO::getUserId, userId);
        wrapper.eq(SystemNoticeUserDO::getReadStatus, 0);
        wrapper.set(SystemNoticeUserDO::getReadStatus, 1);
        wrapper.set(SystemNoticeUserDO::getReadTime, new Date());
        update(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAllAsRead(Integer type) {
        Long userId = SecurityUtils.getUserId();
        if (type == null) {
            LambdaUpdateWrapper<SystemNoticeUserDO> wrapper = new LambdaUpdateWrapper<>();
            wrapper.eq(SystemNoticeUserDO::getUserId, userId);
            wrapper.eq(SystemNoticeUserDO::getReadStatus, 0);
            wrapper.set(SystemNoticeUserDO::getReadStatus, 1);
            wrapper.set(SystemNoticeUserDO::getReadTime, new Date());
            update(wrapper);
        } else {
            LambdaQueryWrapper<SystemNoticeUserDO> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SystemNoticeUserDO::getUserId, userId);
            queryWrapper.eq(SystemNoticeUserDO::getReadStatus, 0);
            queryWrapper.inSql(SystemNoticeUserDO::getNoticeId,
                    "SELECT id FROM system_notice WHERE type = " + type + " AND del_flag = 0 AND status = 1");
            List<SystemNoticeUserDO> unreadList = list(queryWrapper);
            if (!unreadList.isEmpty()) {
                List<Long> ids = unreadList.stream().map(SystemNoticeUserDO::getId).collect(Collectors.toList());
                LambdaUpdateWrapper<SystemNoticeUserDO> updateWrapper = new LambdaUpdateWrapper<>();
                updateWrapper.in(SystemNoticeUserDO::getId, ids);
                updateWrapper.set(SystemNoticeUserDO::getReadStatus, 1);
                updateWrapper.set(SystemNoticeUserDO::getReadTime, new Date());
                update(updateWrapper);
            }
        }
    }

    @Override
    public Map<String, Integer> getUnreadCount() {
        Long userId = SecurityUtils.getUserId();
        List<Map<String, Object>> countList = baseMapper.selectUnreadCount(userId);

        Map<String, Integer> result = new HashMap<>();
        result.put("notice", 0);
        result.put("message", 0);
        result.put("todo", 0);

        for (Map<String, Object> item : countList) {
            Integer typeCode = ((Number) item.get("type")).intValue();
            Integer count = ((Number) item.get("count")).intValue();
            if (NoticeTypeEnum.NOTICE.getCode().equals(typeCode)) {
                result.put("notice", count);
            } else if (NoticeTypeEnum.MESSAGE.getCode().equals(typeCode)) {
                result.put("message", count);
            } else if (NoticeTypeEnum.TODO.getCode().equals(typeCode)) {
                result.put("todo", count);
            }
        }
        return result;
    }
}
