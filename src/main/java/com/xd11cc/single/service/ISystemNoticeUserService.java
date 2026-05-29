package com.xd11cc.single.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xd11cc.single.entity.domain.SystemNoticeUserDO;
import com.xd11cc.single.entity.dto.SystemNoticeUserDTO;
import com.xd11cc.single.entity.vo.SystemNoticeSendVO;
import com.xd11cc.single.entity.vo.SystemNoticeUserQueryVO;

import java.util.List;
import java.util.Map;

/**
 * @author xd11cc
 * @date 2026-05-29
 */
public interface ISystemNoticeUserService extends IService<SystemNoticeUserDO> {

    List<SystemNoticeUserDTO> getMyNoticeList(SystemNoticeUserQueryVO queryVO);

    void send(SystemNoticeSendVO sendVO);

    void markAsRead(Long noticeId);

    void markAllAsRead(Integer type);

    Map<String, Integer> getUnreadCount();
}
