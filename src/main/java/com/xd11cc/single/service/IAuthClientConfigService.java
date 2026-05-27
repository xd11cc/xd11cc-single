package com.xd11cc.single.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xd11cc.single.entity.domain.AuthClientConfigDO;
import com.xd11cc.single.entity.vo.AuthClientConfigAddVO;
import com.xd11cc.single.entity.vo.AuthClientConfigQueryVO;
import com.xd11cc.single.entity.vo.AuthClientConfigUpdateVO;

import java.util.List;

/**
 * @author xd11cc
 * @date 2026-05-20
 */
public interface IAuthClientConfigService extends IService<AuthClientConfigDO> {

    AuthClientConfigDO getBySource(String source);

    int add(AuthClientConfigAddVO authClientConfigAddVO);

    int deleteByIds(List<Long> ids);

    int modifyById(AuthClientConfigUpdateVO authClientConfigUpdateVO);

    List<AuthClientConfigDO> getPageList(AuthClientConfigQueryVO authClientConfigQueryVO);
}
