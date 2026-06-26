package com.xd11cc.single.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xd11cc.single.entity.domain.SystemJobLogDO;
import com.xd11cc.single.entity.vo.SystemJobLogQueryVO;

import java.util.List;

/**
 * @author xd11cc
 * @date 2026-06-11
 */
public interface ISystemJobLogService extends IService<SystemJobLogDO> {

    List<SystemJobLogDO> getList(SystemJobLogQueryVO queryVO);

    int deleteByIds(List<Long> ids);
}
