package com.xd11cc.single.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xd11cc.single.entity.domain.SystemMenuDO;
import com.xd11cc.single.entity.domain.SystemUserDO;
import com.xd11cc.single.entity.vo.*;

import javax.validation.Valid;
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
     * 获取路由信息
     * @return
     */
    List<RouteVO> getRoutes(Long userId);

    /**
     * 新增菜单
     * @param systemMenuAddVO
     * @return
     */
    int add(@Valid SystemMenuAddVO systemMenuAddVO);

    /**
     * 删除菜单
     * @param id
     * @return
     */
    int deleteById(Long id);

    /**
     * 修改菜单
     * @param systemMenuUpdateVO
     * @return
     */
    int modifyById(SystemMenuUpdateVO systemMenuUpdateVO);

    /**
     * 菜单分页
     * @param systemMenuQueryVO
     * @return
     */
    List<SystemMenuTreeVO> getTreeList(SystemMenuQueryVO systemMenuQueryVO);
}
