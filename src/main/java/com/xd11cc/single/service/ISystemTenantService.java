package com.xd11cc.single.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xd11cc.single.entity.domain.SystemTenantDO;
import com.xd11cc.single.entity.vo.SystemTenantAddVO;
import com.xd11cc.single.entity.vo.SystemTenantQueryVO;
import com.xd11cc.single.entity.vo.SystemTenantUpdateVO;

import java.util.List;

/**
 * @author xd11cc
 * @date 2026-05-28
 */
public interface ISystemTenantService extends IService<SystemTenantDO> {

    int add(SystemTenantAddVO vo);

    int deleteByIds(List<Long> ids);

    int modifyById(SystemTenantUpdateVO vo);

    SystemTenantDO getDetail(Long id);

    List<SystemTenantDO> getList(SystemTenantQueryVO queryVO);

    void refreshCache();
}
