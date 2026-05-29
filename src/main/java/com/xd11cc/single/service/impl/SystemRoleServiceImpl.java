package com.xd11cc.single.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xd11cc.single.config.exception.ServiceException;
import com.xd11cc.single.convert.SystemRoleConvert;
import com.xd11cc.single.entity.domain.SystemRoleDO;
import com.xd11cc.single.entity.domain.SystemRoleMenuDO;
import com.xd11cc.single.entity.domain.SystemUserRoleDO;
import com.xd11cc.single.entity.vo.SystemRoleAddVO;
import com.xd11cc.single.entity.vo.SystemRoleQueryVO;
import com.xd11cc.single.entity.vo.SystemRoleUpdateVO;
import com.xd11cc.single.enums.SystemErrorEnum;
import com.xd11cc.single.mapper.SystemRoleMapper;
import com.xd11cc.single.service.ISystemRoleMenuService;
import com.xd11cc.single.service.ISystemRoleService;
import com.xd11cc.single.service.ISystemUserRoleService;
import com.xd11cc.single.service.ISystemRoleDeptService;
import com.xd11cc.single.enums.DataScopeEnum;
import com.xd11cc.single.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author xd11cc
 * @date 2026-05-27
 */
@Slf4j
@Service
public class SystemRoleServiceImpl extends ServiceImpl<SystemRoleMapper, SystemRoleDO> implements ISystemRoleService {

    @Autowired
    private ISystemRoleMenuService systemRoleMenuService;
    @Autowired
    private ISystemUserRoleService systemUserRoleService;
    @Autowired
    private ISystemRoleDeptService systemRoleDeptService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int add(SystemRoleAddVO systemRoleAddVO) {
        SystemRoleDO systemRoleDO = SystemRoleConvert.INSTANCE.addVO2DO(systemRoleAddVO);
        int row;
        try {
            row = baseMapper.insert(systemRoleDO);
        } catch (DuplicateKeyException e) {
            throw new ServiceException(SystemErrorEnum.ROLE_CODE_EXISTS);
        }
        if (row > 0) {
            saveRoleMenus(systemRoleDO.getId(), systemRoleAddVO.getMenuIds());
            saveRoleDepts(systemRoleDO.getId(), systemRoleAddVO.getDataScope(), systemRoleAddVO.getDeptIds());
        }
        return row;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteByIds(List<Long> ids) {
        // 校验角色是否已分配用户
        long userCount = systemUserRoleService.count(new LambdaQueryWrapper<SystemUserRoleDO>()
                .in(SystemUserRoleDO::getRoleId, ids));
        if (userCount > 0) {
            throw new ServiceException(SystemErrorEnum.ROLE_BINDEDE_USER);
        }
        int row = baseMapper.deleteBatchIds(ids);
        if (row > 0) {
            // 清理角色菜单关联
            systemRoleMenuService.remove(new LambdaQueryWrapper<SystemRoleMenuDO>()
                    .in(SystemRoleMenuDO::getRoleId, ids));
            // 清理角色部门关联
            systemRoleDeptService.removeByRoleIds(ids);
        }
        return row;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int modifyById(SystemRoleUpdateVO systemRoleUpdateVO) {
        SystemRoleDO systemRoleDO = SystemRoleConvert.INSTANCE.updateVO2DO(systemRoleUpdateVO);
        int row;
        try {
            row = baseMapper.updateById(systemRoleDO);
        } catch (DuplicateKeyException e) {
            throw new ServiceException(SystemErrorEnum.ROLE_CODE_EXISTS);
        }
        if (row > 0) {
            // 先删后插更新菜单关联
            systemRoleMenuService.remove(new LambdaQueryWrapper<SystemRoleMenuDO>()
                    .eq(SystemRoleMenuDO::getRoleId, systemRoleUpdateVO.getId()));
            saveRoleMenus(systemRoleUpdateVO.getId(), systemRoleUpdateVO.getMenuIds());
            // 更新角色部门关联
            saveRoleDepts(systemRoleUpdateVO.getId(), systemRoleUpdateVO.getDataScope(), systemRoleUpdateVO.getDeptIds());
        }
        return row;
    }

    @Override
    public List<SystemRoleDO> getList(SystemRoleQueryVO systemRoleQueryVO) {
        LambdaQueryWrapper<SystemRoleDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.isNotEmpty(systemRoleQueryVO.getRoleCode()),
                SystemRoleDO::getRoleCode, systemRoleQueryVO.getRoleCode());
        wrapper.like(StringUtils.isNotEmpty(systemRoleQueryVO.getRoleName()),
                SystemRoleDO::getRoleName, systemRoleQueryVO.getRoleName());
        wrapper.eq(StringUtils.isNotEmpty(systemRoleQueryVO.getStatus()),
                SystemRoleDO::getStatus, systemRoleQueryVO.getStatus());
        wrapper.ge(systemRoleQueryVO.getBeginTime() != null,
                SystemRoleDO::getCreateTime, systemRoleQueryVO.getBeginTime());
        wrapper.le(systemRoleQueryVO.getEndTime() != null,
                SystemRoleDO::getCreateTime, systemRoleQueryVO.getEndTime());
        wrapper.orderByDesc(SystemRoleDO::getId);
        return baseMapper.selectList(wrapper);
    }

    @Override
    public List<Long> getMenuIdsByRoleId(Long roleId) {
        List<SystemRoleMenuDO> roleMenus = systemRoleMenuService.list(new LambdaQueryWrapper<SystemRoleMenuDO>()
                .eq(SystemRoleMenuDO::getRoleId, roleId));
        return roleMenus.stream()
                .map(SystemRoleMenuDO::getMenuId)
                .collect(Collectors.toList());
    }

    private void saveRoleMenus(Long roleId, List<Long> menuIds) {
        if (menuIds == null || menuIds.isEmpty()) {
            return;
        }
        List<SystemRoleMenuDO> roleMenus = menuIds.stream().map(menuId -> {
            SystemRoleMenuDO roleMenu = new SystemRoleMenuDO();
            roleMenu.setRoleId(roleId);
            roleMenu.setMenuId(menuId);
            return roleMenu;
        }).collect(Collectors.toList());
        systemRoleMenuService.saveBatch(roleMenus);
    }

    private void saveRoleDepts(Long roleId, String dataScope, List<Long> deptIds) {
        systemRoleDeptService.removeByRoleId(roleId);
        if (DataScopeEnum.CUSTOM.getCode().equals(dataScope)) {
            systemRoleDeptService.saveRoleDepts(roleId, deptIds);
        }
    }
}
