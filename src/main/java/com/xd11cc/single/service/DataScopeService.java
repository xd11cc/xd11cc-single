package com.xd11cc.single.service;

import com.xd11cc.single.entity.dto.LoginUserDTO;

/**
 * @author xd11cc
 * @date 2026-05-28
 */
public interface DataScopeService {

    /**
     * 计算并设置用户的数据权限信息到 LoginUserDTO
     */
    void resolveDataScope(LoginUserDTO loginUserDTO);
}
