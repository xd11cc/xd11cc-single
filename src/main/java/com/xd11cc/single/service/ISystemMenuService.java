package com.xd11cc.single.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xd11cc.single.entity.domain.SystemMenuDO;
import com.xd11cc.single.entity.domain.SystemUserDO;

import java.util.Set;

/**
 * @Author: xd11cc
 * @Date: 2025/6/19 13:35
 **/
public interface ISystemMenuService extends IService<SystemMenuDO> {

    Set<String> getPermissionMenu(SystemUserDO systemUserDO);
}
