package com.xd11cc.single.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xd11cc.single.entity.domain.SystemRoleDO;
import com.xd11cc.single.entity.vo.SystemRoleAddVO;
import com.xd11cc.single.entity.vo.SystemRoleQueryVO;
import com.xd11cc.single.entity.vo.SystemRoleUpdateVO;

import java.util.List;

/**
 * @author xd11cc
 * @date 2026-05-27
 */
public interface ISystemRoleService extends IService<SystemRoleDO> {

    /**
     * 新增角色
     * @param systemRoleAddVO
     * @return
     */
    int add(SystemRoleAddVO systemRoleAddVO);

    /**
     * 批量删除角色
     * @param ids
     * @return
     */
    int deleteByIds(List<Long> ids);

    /**
     * 修改角色
     * @param systemRoleUpdateVO
     * @return
     */
    int modifyById(SystemRoleUpdateVO systemRoleUpdateVO);

    /**
     * 查询角色列表
     * @param systemRoleQueryVO
     * @return
     */
    List<SystemRoleDO> getList(SystemRoleQueryVO systemRoleQueryVO);

    /**
     * 查询角色已分配的菜单id列表
     * @param roleId
     * @return
     */
    List<Long> getMenuIdsByRoleId(Long roleId);
}
