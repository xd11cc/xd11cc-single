package com.xd11cc.single.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xd11cc.single.entity.domain.SystemOperateLogDO;
import com.xd11cc.single.entity.vo.SystemOperateLogQueryVO;

import java.util.List;

/**
 * @author xd11cc
 * @date 2026-05-28
 */
public interface ISystemOperateLogService extends IService<SystemOperateLogDO> {

    void saveLog(SystemOperateLogDO operateLog);

    List<SystemOperateLogDO> getList(SystemOperateLogQueryVO queryVO);

    int deleteByIds(List<Long> ids);

    void clean();
}
