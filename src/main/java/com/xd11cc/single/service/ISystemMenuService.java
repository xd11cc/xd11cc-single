package com.xd11cc.single.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xd11cc.single.entity.domain.SystemMenuDO;
import com.xd11cc.single.entity.domain.SystemUserDO;
import com.xd11cc.single.entity.vo.TreeMenuVO;

import java.util.List;
import java.util.Set;

/**
 * @Author: xd11cc
 * @Date: 2025/6/19 13:35
 **/
public interface ISystemMenuService extends IService<SystemMenuDO> {

    /**
     * 获取权限列表
     * @param systemUserDO
     * @return
     */
    Set<String> getPermissionMenu(SystemUserDO systemUserDO);

    /**
     * 获取菜单列表
     * @return
     */
    List<TreeMenuVO> getTreeMenu(Long userId);
}
