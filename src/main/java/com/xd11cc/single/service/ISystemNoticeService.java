package com.xd11cc.single.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xd11cc.single.entity.domain.SystemNoticeDO;
import com.xd11cc.single.entity.vo.SystemNoticeAddVO;
import com.xd11cc.single.entity.vo.SystemNoticeQueryVO;
import com.xd11cc.single.entity.vo.SystemNoticeUpdateVO;

import java.util.List;

/**
 * @author xd11cc
 * @date 2026-05-29
 */
public interface ISystemNoticeService extends IService<SystemNoticeDO> {

    int add(SystemNoticeAddVO vo);

    int modifyById(SystemNoticeUpdateVO vo);

    int deleteByIds(List<Long> ids);

    List<SystemNoticeDO> getList(SystemNoticeQueryVO queryVO);

    SystemNoticeDO getDetailById(Long id);

    void publish(Long id);

    void revoke(Long id);
}
