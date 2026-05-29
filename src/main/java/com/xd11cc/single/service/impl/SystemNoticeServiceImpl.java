package com.xd11cc.single.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xd11cc.single.config.exception.ServiceException;
import com.xd11cc.single.convert.SystemNoticeConvert;
import com.xd11cc.single.entity.domain.SystemNoticeDO;
import com.xd11cc.single.entity.domain.SystemNoticeUserDO;
import com.xd11cc.single.entity.domain.SystemUserDO;
import com.xd11cc.single.enums.NoticeScopeEnum;
import com.xd11cc.single.enums.NoticeStatusEnum;
import com.xd11cc.single.enums.SystemErrorEnum;
import com.xd11cc.single.config.event.NoticeEvent;
import com.xd11cc.single.mapper.SystemNoticeMapper;
import com.xd11cc.single.entity.vo.SystemNoticeAddVO;
import com.xd11cc.single.entity.vo.SystemNoticeQueryVO;
import com.xd11cc.single.entity.vo.SystemNoticeUpdateVO;
import com.xd11cc.single.service.ISystemDeptService;
import com.xd11cc.single.service.ISystemNoticeService;
import com.xd11cc.single.service.ISystemNoticeUserService;
import com.xd11cc.single.service.ISystemUserService;
import com.xd11cc.single.utils.SecurityUtils;
import com.xd11cc.single.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author xd11cc
 * @date 2026-05-29
 */
@Slf4j
@Service
public class SystemNoticeServiceImpl extends ServiceImpl<SystemNoticeMapper, SystemNoticeDO> implements ISystemNoticeService {

    @Autowired
    private ISystemNoticeUserService systemNoticeUserService;
    @Autowired
    private ISystemUserService systemUserService;
    @Autowired
    private ISystemDeptService systemDeptService;
    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Override
    public int add(SystemNoticeAddVO vo) {
        SystemNoticeDO noticeDO = SystemNoticeConvert.INSTANCE.addVO2DO(vo);
        noticeDO.setSenderId(SecurityUtils.getUserId());
        noticeDO.setSenderName(SecurityUtils.getLoginUser().getSystemUserDO().getNickname());
        noticeDO.setStatus(NoticeStatusEnum.DRAFT.getCode());
        return baseMapper.insert(noticeDO);
    }

    @Override
    public int modifyById(SystemNoticeUpdateVO vo) {
        SystemNoticeDO existing = baseMapper.selectById(vo.getId());
        if (existing == null) {
            throw new ServiceException(SystemErrorEnum.NOTICE_NOT_FOUND);
        }
        if (!NoticeStatusEnum.DRAFT.getCode().equals(existing.getStatus())) {
            throw new ServiceException(SystemErrorEnum.NOTICE_ALREADY_PUBLISHED);
        }
        SystemNoticeDO noticeDO = SystemNoticeConvert.INSTANCE.updateVO2DO(vo);
        return baseMapper.updateById(noticeDO);
    }

    @Override
    public int deleteByIds(List<Long> ids) {
        return baseMapper.deleteBatchIds(ids);
    }

    @Override
    public List<SystemNoticeDO> getList(SystemNoticeQueryVO queryVO) {
        LambdaQueryWrapper<SystemNoticeDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.isNotEmpty(queryVO.getTitle()),
                SystemNoticeDO::getTitle, queryVO.getTitle());
        wrapper.eq(queryVO.getType() != null,
                SystemNoticeDO::getType, queryVO.getType());
        wrapper.eq(queryVO.getStatus() != null,
                SystemNoticeDO::getStatus, queryVO.getStatus());
        wrapper.orderByDesc(SystemNoticeDO::getId);
        return baseMapper.selectList(wrapper);
    }

    @Override
    public SystemNoticeDO getDetailById(Long id) {
        return baseMapper.selectById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publish(Long id) {
        SystemNoticeDO notice = baseMapper.selectById(id);
        if (notice == null) {
            throw new ServiceException(SystemErrorEnum.NOTICE_NOT_FOUND);
        }
        if (!NoticeStatusEnum.DRAFT.getCode().equals(notice.getStatus())) {
            throw new ServiceException(SystemErrorEnum.NOTICE_ALREADY_PUBLISHED);
        }

        // 更新状态为已发布
        notice.setStatus(NoticeStatusEnum.PUBLISHED.getCode());
        notice.setPublishTime(new Date());
        baseMapper.updateById(notice);

        // 解析目标用户
        List<Long> targetUserIds = resolveTargetUserIds(notice);
        if (targetUserIds.isEmpty()) {
            return;
        }

        // 批量创建 notice_user 记录
        List<SystemNoticeUserDO> noticeUserList = targetUserIds.stream()
                .map(userId -> {
                    SystemNoticeUserDO nu = new SystemNoticeUserDO();
                    nu.setNoticeId(id);
                    nu.setUserId(userId);
                    nu.setReadStatus(0);
                    return nu;
                }).collect(Collectors.toList());
        systemNoticeUserService.saveBatch(noticeUserList, 500);

        // 发布事件（事务提交后由 NoticeEventListener 异步推送）
        eventPublisher.publishEvent(NoticeEvent.publish(this, notice.getId(),
                notice.getTitle(), notice.getType(), notice.getScope(),
                notice.getTenantId(), notice.getSenderName(), targetUserIds));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void revoke(Long id) {
        SystemNoticeDO notice = baseMapper.selectById(id);
        if (notice == null) {
            throw new ServiceException(SystemErrorEnum.NOTICE_NOT_FOUND);
        }
        if (!NoticeStatusEnum.PUBLISHED.getCode().equals(notice.getStatus())) {
            throw new ServiceException(SystemErrorEnum.NOTICE_NOT_PUBLISHED);
        }

        // 更新状态为已撤回
        notice.setStatus(NoticeStatusEnum.REVOKED.getCode());
        baseMapper.updateById(notice);

        // 软删所有 notice_user 记录
        systemNoticeUserService.remove(new LambdaQueryWrapper<SystemNoticeUserDO>()
                .eq(SystemNoticeUserDO::getNoticeId, id));

        // 发布撤回事件
        eventPublisher.publishEvent(NoticeEvent.revoke(this, id, notice.getType(), notice.getTenantId()));
    }

    private List<Long> resolveTargetUserIds(SystemNoticeDO notice) {
        Integer scope = notice.getScope();
        if (NoticeScopeEnum.ALL.getCode().equals(scope)) {
            LambdaQueryWrapper<SystemUserDO> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SystemUserDO::getStatus, "0");
            wrapper.select(SystemUserDO::getId);
            return systemUserService.list(wrapper).stream()
                    .map(SystemUserDO::getId)
                    .collect(Collectors.toList());
        } else if (NoticeScopeEnum.DEPT.getCode().equals(scope)) {
            String deptIdsStr = notice.getScopeDeptIds();
            if (StringUtils.isEmpty(deptIdsStr)) {
                return Collections.emptyList();
            }
            Set<Long> allDeptIds = new HashSet<>();
            for (String deptIdStr : deptIdsStr.split(",")) {
                Long deptId = Long.valueOf(deptIdStr.trim());
                allDeptIds.addAll(systemDeptService.getSubDeptIds(deptId));
            }
            if (allDeptIds.isEmpty()) {
                return Collections.emptyList();
            }
            LambdaQueryWrapper<SystemUserDO> wrapper = new LambdaQueryWrapper<>();
            wrapper.in(SystemUserDO::getDeptId, allDeptIds);
            wrapper.eq(SystemUserDO::getStatus, "0");
            wrapper.select(SystemUserDO::getId);
            return systemUserService.list(wrapper).stream()
                    .map(SystemUserDO::getId)
                    .collect(Collectors.toList());
        } else if (NoticeScopeEnum.USER.getCode().equals(scope)) {
            String userIdsStr = notice.getScopeUserIds();
            if (StringUtils.isEmpty(userIdsStr)) {
                return Collections.emptyList();
            }
            return Stream.of(userIdsStr.split(","))
                    .map(String::trim)
                    .map(Long::valueOf)
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}
