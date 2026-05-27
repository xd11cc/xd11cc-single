package com.xd11cc.single.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xd11cc.single.config.exception.ServiceException;
import com.xd11cc.single.convert.SystemUserConvert;
import com.xd11cc.single.entity.domain.SystemUserDO;
import com.xd11cc.single.entity.domain.SystemUserRoleDO;
import com.xd11cc.single.entity.vo.SystemUserAddVO;
import com.xd11cc.single.entity.vo.SystemUserDetailVO;
import com.xd11cc.single.entity.vo.SystemUserQueryVO;
import com.xd11cc.single.entity.vo.SystemUserUpdateVO;
import com.xd11cc.single.enums.SystemErrorEnum;
import com.xd11cc.single.mapper.SystemUserMapper;
import com.xd11cc.single.service.ISystemUserRoleService;
import com.xd11cc.single.service.ISystemUserService;
import com.xd11cc.single.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author: xd11cc
 * @Date: 2025/6/14 00:27
 **/
@Slf4j
@Service
public class SystemUserServiceImpl extends ServiceImpl<SystemUserMapper, SystemUserDO> implements ISystemUserService {

    @Autowired
    private ISystemUserRoleService systemUserRoleService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public SystemUserDO getByUsername(String username) {
        return baseMapper.selectOne(new LambdaQueryWrapper<SystemUserDO>()
                .eq(SystemUserDO::getUsername, username));
    }

    @Override
    public SystemUserDO getByEmail(String email) {
        return baseMapper.selectOne(new LambdaQueryWrapper<SystemUserDO>()
                .eq(SystemUserDO::getEmail, email));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int add(SystemUserAddVO systemUserAddVO) {
        SystemUserDO systemUserDO = SystemUserConvert.INSTANCE.addVO2DO(systemUserAddVO);
        systemUserDO.setPassword(passwordEncoder.encode(systemUserAddVO.getPassword()));
        int row;
        try {
            row = baseMapper.insert(systemUserDO);
        } catch (DuplicateKeyException e) {
            throw new ServiceException(SystemErrorEnum.USERNAME_EXISTS);
        }
        if (row > 0) {
            saveUserRoles(systemUserDO.getId(), systemUserAddVO.getRoleIds());
        }
        return row;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteByIds(List<Long> ids) {
        // 不允许删除管理员
        for (Long id : ids) {
            if (SystemUserDO.isAdmin(id)) {
                throw new ServiceException(SystemErrorEnum.ADMIN_NOT_ALLOW_DELETE);
            }
        }
        int row = baseMapper.deleteBatchIds(ids);
        if (row > 0) {
            // 删除用户角色关联
            systemUserRoleService.remove(new LambdaQueryWrapper<SystemUserRoleDO>()
                    .in(SystemUserRoleDO::getUserId, ids));
        }
        return row;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int modifyById(SystemUserUpdateVO systemUserUpdateVO) {
        SystemUserDO systemUserDO = SystemUserConvert.INSTANCE.updateVO2DO(systemUserUpdateVO);
        int row = baseMapper.updateById(systemUserDO);
        if (row > 0) {
            // 先删后插更新角色关联
            systemUserRoleService.remove(new LambdaQueryWrapper<SystemUserRoleDO>()
                    .eq(SystemUserRoleDO::getUserId, systemUserUpdateVO.getId()));
            saveUserRoles(systemUserUpdateVO.getId(), systemUserUpdateVO.getRoleIds());
        }
        return row;
    }

    @Override
    public List<SystemUserDO> getList(SystemUserQueryVO systemUserQueryVO) {
        LambdaQueryWrapper<SystemUserDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.isNotEmpty(systemUserQueryVO.getUsername()),
                SystemUserDO::getUsername, systemUserQueryVO.getUsername());
        wrapper.like(StringUtils.isNotEmpty(systemUserQueryVO.getNickname()),
                SystemUserDO::getNickname, systemUserQueryVO.getNickname());
        wrapper.eq(StringUtils.isNotEmpty(systemUserQueryVO.getPhone()),
                SystemUserDO::getPhone, systemUserQueryVO.getPhone());
        wrapper.eq(StringUtils.isNotEmpty(systemUserQueryVO.getStatus()),
                SystemUserDO::getStatus, systemUserQueryVO.getStatus());
        wrapper.eq(systemUserQueryVO.getDeptId() != null,
                SystemUserDO::getDeptId, systemUserQueryVO.getDeptId());
        wrapper.ge(systemUserQueryVO.getBeginTime() != null,
                SystemUserDO::getCreateTime, systemUserQueryVO.getBeginTime());
        wrapper.le(systemUserQueryVO.getEndTime() != null,
                SystemUserDO::getCreateTime, systemUserQueryVO.getEndTime());
        wrapper.orderByDesc(SystemUserDO::getId);
        return baseMapper.selectList(wrapper);
    }

    @Override
    public int resetPassword(Long userId, String newPassword) {
        SystemUserDO systemUserDO = new SystemUserDO();
        systemUserDO.setId(userId);
        systemUserDO.setPassword(passwordEncoder.encode(newPassword));
        return baseMapper.updateById(systemUserDO);
    }

    @Override
    public SystemUserDetailVO getDetailById(Long userId) {
        SystemUserDO systemUserDO = baseMapper.selectById(userId);
        if (systemUserDO == null) {
            return null;
        }
        SystemUserDetailVO detailVO = SystemUserConvert.INSTANCE.do2DetailVO(systemUserDO);
        List<SystemUserRoleDO> userRoles = systemUserRoleService.list(new LambdaQueryWrapper<SystemUserRoleDO>()
                .eq(SystemUserRoleDO::getUserId, userId));
        List<Long> roleIds = userRoles.stream()
                .map(SystemUserRoleDO::getRoleId)
                .collect(Collectors.toList());
        detailVO.setRoleIds(roleIds);
        return detailVO;
    }

    private void saveUserRoles(Long userId, List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return;
        }
        List<SystemUserRoleDO> userRoles = roleIds.stream().map(roleId -> {
            SystemUserRoleDO userRole = new SystemUserRoleDO();
            userRole.setUserId(userId);
            userRole.setRoleId(roleId);
            return userRole;
        }).collect(Collectors.toList());
        systemUserRoleService.saveBatch(userRoles);
    }
}
