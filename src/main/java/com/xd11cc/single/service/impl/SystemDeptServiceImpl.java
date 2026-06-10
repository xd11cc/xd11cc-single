package com.xd11cc.single.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xd11cc.single.config.exception.ServiceException;
import com.xd11cc.single.convert.SystemDeptConvert;
import com.xd11cc.single.entity.domain.SystemDeptDO;
import com.xd11cc.single.entity.vo.SystemDeptAddVO;
import com.xd11cc.single.entity.vo.SystemDeptQueryVO;
import com.xd11cc.single.entity.vo.SystemDeptTreeVO;
import com.xd11cc.single.entity.vo.SystemDeptUpdateVO;
import com.xd11cc.single.enums.SystemErrorEnum;
import com.xd11cc.single.mapper.SystemDeptMapper;
import com.xd11cc.single.service.ISystemDeptService;
import com.xd11cc.single.utils.TreeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author xd11cc
 * @date 2026-05-26
 */
@Slf4j
@Service
public class SystemDeptServiceImpl extends ServiceImpl<SystemDeptMapper, SystemDeptDO> implements ISystemDeptService {

    @Override
    public int add(SystemDeptAddVO systemDeptAddVO) {
        SystemDeptDO systemDeptDO = SystemDeptConvert.INSTANCE.addVO2DO(systemDeptAddVO);
        try {
            return baseMapper.insert(systemDeptDO);
        } catch (DuplicateKeyException e) {
            throw new ServiceException(SystemErrorEnum.DEPT_CODE_EXISTS);
        }
    }

    @Override
    public int deleteById(Long id) {
        // 校验是否有子部门
        long childCount = count(new LambdaQueryWrapper<SystemDeptDO>()
                .eq(SystemDeptDO::getParentId, id));
        if (childCount > 0) {
            throw new ServiceException(SystemErrorEnum.DEPT_HAVE_CHILDREN);
        }
        return baseMapper.deleteById(id);
    }

    @Override
    public int modifyById(SystemDeptUpdateVO systemDeptUpdateVO) {
        // 不能将父部门设置为自己
        if (systemDeptUpdateVO.getId().equals(systemDeptUpdateVO.getParentId())) {
            throw new ServiceException(SystemErrorEnum.DEPT_PARENT_CANNOT_SELF);
        }
        SystemDeptDO systemDeptDO = SystemDeptConvert.INSTANCE.updateVO2DO(systemDeptUpdateVO);
        try {
            return baseMapper.updateById(systemDeptDO);
        } catch (DuplicateKeyException e) {
            throw new ServiceException(SystemErrorEnum.DEPT_CODE_EXISTS);
        }
    }

    @Override
    public List<SystemDeptDO> getList(SystemDeptQueryVO systemDeptQueryVO) {
        LambdaQueryWrapper<SystemDeptDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.isNotEmpty(systemDeptQueryVO.getDeptName()),
                SystemDeptDO::getDeptName, systemDeptQueryVO.getDeptName());
        wrapper.eq(StringUtils.isNotEmpty(systemDeptQueryVO.getStatus()),
                SystemDeptDO::getStatus, systemDeptQueryVO.getStatus());
        wrapper.orderByAsc(SystemDeptDO::getSort);
        return baseMapper.selectList(wrapper);
    }

    @Override
    public List<SystemDeptTreeVO> getTreeList(SystemDeptQueryVO systemDeptQueryVO) {
        List<SystemDeptDO> deptList = getList(systemDeptQueryVO);
        List<SystemDeptTreeVO> treeVOList = deptList.stream()
                .map(SystemDeptConvert.INSTANCE::do2TreeVO)
                .collect(Collectors.toList());
        return TreeUtils.buildTree(treeVOList,
                SystemDeptTreeVO::getId,
                SystemDeptTreeVO::getParentId,
                SystemDeptTreeVO::setChildren,
                SystemDeptTreeVO::getSort,
                null);
    }

    @Override
    public Set<Long> getSubDeptIds(Long deptId) {
        List<SystemDeptDO> allDepts = baseMapper.selectList(new LambdaQueryWrapper<>());
        Set<Long> result = new HashSet<>();
        result.add(deptId);
        Queue<Long> queue = new LinkedList<>();
        queue.add(deptId);
        while (!queue.isEmpty()) {
            Long parentId = queue.poll();
            for (SystemDeptDO dept : allDepts) {
                if (parentId.equals(dept.getParentId())) {
                    result.add(dept.getId());
                    queue.add(dept.getId());
                }
            }
        }
        return result;
    }
}
