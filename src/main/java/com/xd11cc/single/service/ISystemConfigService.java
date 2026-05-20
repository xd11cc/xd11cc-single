package com.xd11cc.single.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xd11cc.single.entity.domain.SystemConfigDO;
import com.xd11cc.single.entity.vo.SystemConfigAddVO;
import com.xd11cc.single.entity.vo.SystemConfigQueryVO;
import com.xd11cc.single.entity.vo.SystemConfigUpdateVO;

import javax.validation.Valid;
import java.util.List;

/**
 * @author xd11cc
 * @date 2026-05-20
 */
public interface ISystemConfigService extends IService<SystemConfigDO> {

    int add(SystemConfigAddVO systemConfigAddVO);

    int deleteByIds(List<Long> ids);

    int modifyById(@Valid SystemConfigUpdateVO systemConfigUpdateVO);

    List<SystemConfigDO> getList(SystemConfigQueryVO systemConfigQueryVO);

    String getConfig(String configKey);
}
