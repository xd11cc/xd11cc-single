package com.xd11cc.single.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xd11cc.single.entity.domain.SystemRoleDO;
import com.xd11cc.single.entity.domain.SystemUserRoleDO;
import com.xd11cc.single.entity.dto.LoginUserDTO;
import com.xd11cc.single.enums.DataScopeEnum;
import com.xd11cc.single.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author xd11cc
 * @date 2026-05-28
 */
@Service
public class DataScopeServiceImpl implements DataScopeService {

    @Autowired
    private ISystemUserRoleService systemUserRoleService;
    @Autowired
    private ISystemRoleService systemRoleService;
    @Autowired
    private ISystemDeptService systemDeptService;
    @Autowired
    private ISystemRoleDeptService systemRoleDeptService;

    @Override
    public void resolveDataScope(LoginUserDTO loginUserDTO) {
        Long userId = loginUserDTO.getUserId();
        Long deptId = loginUserDTO.getDeptId();

        // 查询用户角色
        List<SystemUserRoleDO> userRoles = systemUserRoleService.list(new LambdaQueryWrapper<SystemUserRoleDO>()
                .eq(SystemUserRoleDO::getUserId, userId));
        if (userRoles.isEmpty()) {
            loginUserDTO.setDataScope(DataScopeEnum.SELF.getCode());
            return;
        }

        Set<Long> roleIds = userRoles.stream()
                .map(SystemUserRoleDO::getRoleId)
                .collect(Collectors.toSet());
        loginUserDTO.setRoleIds(roleIds);

        // 查询角色信息
        List<SystemRoleDO> roles = systemRoleService.listByIds(roleIds);

        // 判断是否有 ALL 权限
        boolean hasAll = roles.stream()
                .anyMatch(r -> DataScopeEnum.ALL.getCode().equals(r.getDataScope()));
        if (hasAll) {
            loginUserDTO.setDataScope(DataScopeEnum.ALL.getCode());
            return;
        }

        // 判断是否全部为 SELF
        boolean allSelf = roles.stream()
                .allMatch(r -> DataScopeEnum.SELF.getCode().equals(r.getDataScope()));
        if (allSelf) {
            loginUserDTO.setDataScope(DataScopeEnum.SELF.getCode());
            return;
        }

        // 合并所有角色的可见部门
        Set<Long> deptIds = new HashSet<>();
        Set<Long> customRoleIds = new HashSet<>();

        for (SystemRoleDO role : roles) {
            String scope = role.getDataScope();
            if (DataScopeEnum.DEPT_AND_SUB.getCode().equals(scope)) {
                deptIds.addAll(systemDeptService.getSubDeptIds(deptId));
            } else if (DataScopeEnum.DEPT.getCode().equals(scope)) {
                deptIds.add(deptId);
            } else if (DataScopeEnum.CUSTOM.getCode().equals(scope)) {
                customRoleIds.add(role.getId());
            }
        }

        // 查询自定义部门关联
        if (!customRoleIds.isEmpty()) {
            deptIds.addAll(systemRoleDeptService.getDeptIdsByRoleIds(customRoleIds));
        }

        if (deptIds.isEmpty()) {
            loginUserDTO.setDataScope(DataScopeEnum.SELF.getCode());
        } else {
            loginUserDTO.setDataScope(DataScopeEnum.CUSTOM.getCode());
            loginUserDTO.setDataScopeDeptIds(deptIds);
        }
    }
}
