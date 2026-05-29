package com.xd11cc.single.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xd11cc.single.entity.domain.SystemRoleDeptDO;
import com.xd11cc.single.mapper.SystemRoleDeptMapper;
import com.xd11cc.single.service.ISystemRoleDeptService;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author xd11cc
 * @date 2026-05-28
 */
@Service
public class SystemRoleDeptServiceImpl extends ServiceImpl<SystemRoleDeptMapper, SystemRoleDeptDO> implements ISystemRoleDeptService {

    @Override
    public Set<Long> getDeptIdsByRoleId(Long roleId) {
        List<SystemRoleDeptDO> list = baseMapper.selectList(new LambdaQueryWrapper<SystemRoleDeptDO>()
                .eq(SystemRoleDeptDO::getRoleId, roleId));
        return list.stream().map(SystemRoleDeptDO::getDeptId).collect(Collectors.toSet());
    }

    @Override
    public Set<Long> getDeptIdsByRoleIds(Set<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return Collections.emptySet();
        }
        List<SystemRoleDeptDO> list = baseMapper.selectList(new LambdaQueryWrapper<SystemRoleDeptDO>()
                .in(SystemRoleDeptDO::getRoleId, roleIds));
        return list.stream().map(SystemRoleDeptDO::getDeptId).collect(Collectors.toSet());
    }

    @Override
    public void saveRoleDepts(Long roleId, List<Long> deptIds) {
        if (deptIds == null || deptIds.isEmpty()) {
            return;
        }
        List<SystemRoleDeptDO> roleDepts = deptIds.stream().map(deptId -> {
            SystemRoleDeptDO roleDept = new SystemRoleDeptDO();
            roleDept.setRoleId(roleId);
            roleDept.setDeptId(deptId);
            return roleDept;
        }).collect(Collectors.toList());
        saveBatch(roleDepts);
    }

    @Override
    public void removeByRoleId(Long roleId) {
        remove(new LambdaQueryWrapper<SystemRoleDeptDO>()
                .eq(SystemRoleDeptDO::getRoleId, roleId));
    }

    @Override
    public void removeByRoleIds(List<Long> roleIds) {
        remove(new LambdaQueryWrapper<SystemRoleDeptDO>()
                .in(SystemRoleDeptDO::getRoleId, roleIds));
    }
}
