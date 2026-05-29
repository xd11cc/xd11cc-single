package com.xd11cc.single.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xd11cc.single.entity.domain.SystemRoleDeptDO;

import java.util.List;
import java.util.Set;

/**
 * @author xd11cc
 * @date 2026-05-28
 */
public interface ISystemRoleDeptService extends IService<SystemRoleDeptDO> {

    /**
     * 根据角色ID获取关联的部门ID集合
     */
    Set<Long> getDeptIdsByRoleId(Long roleId);

    /**
     * 根据多个角色ID获取关联的部门ID集合（并集）
     */
    Set<Long> getDeptIdsByRoleIds(Set<Long> roleIds);

    /**
     * 保存角色部门关联
     */
    void saveRoleDepts(Long roleId, List<Long> deptIds);

    /**
     * 删除角色部门关联
     */
    void removeByRoleId(Long roleId);

    /**
     * 批量删除角色部门关联
     */
    void removeByRoleIds(List<Long> roleIds);
}
