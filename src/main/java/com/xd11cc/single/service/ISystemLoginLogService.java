package com.xd11cc.single.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xd11cc.single.entity.domain.SystemLoginLogDO;
import com.xd11cc.single.entity.vo.SystemLoginLogQueryVO;
import com.xd11cc.single.enums.OperateStatusEnum;
import com.xd11cc.single.enums.LoginTypeEnum;

import java.util.List;

/**
 * @author xd11cc
 * @date 2026-05-28
 */
public interface ISystemLoginLogService extends IService<SystemLoginLogDO> {

    void recordLoginLog(String username, LoginTypeEnum loginType, OperateStatusEnum status, String msg);

    List<SystemLoginLogDO> getList(SystemLoginLogQueryVO queryVO);

    int deleteByIds(List<Long> ids);

    void clean();
}
