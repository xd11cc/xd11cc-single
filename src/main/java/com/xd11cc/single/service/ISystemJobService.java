package com.xd11cc.single.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xd11cc.single.entity.domain.SystemJobDO;
import com.xd11cc.single.entity.vo.SystemJobAddVO;
import com.xd11cc.single.entity.vo.SystemJobQueryVO;
import com.xd11cc.single.entity.vo.SystemJobUpdateVO;

import java.util.List;

/**
 * @author xd11cc
 * @date 2026-06-11
 */
public interface ISystemJobService extends IService<SystemJobDO> {

    int add(SystemJobAddVO addVO);

    int modifyById(SystemJobUpdateVO updateVO);

    int deleteByIds(List<Long> ids);

    List<SystemJobDO> getList(SystemJobQueryVO queryVO);

    int changeStatus(Long id, String status);

    void runOnce(Long id);
}
