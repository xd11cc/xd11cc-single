package com.xd11cc.single.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xd11cc.single.config.exception.ServiceException;
import com.xd11cc.single.convert.SystemPostConvert;
import com.xd11cc.single.entity.domain.SystemDeptPostDO;
import com.xd11cc.single.entity.domain.SystemPostDO;
import com.xd11cc.single.entity.vo.SystemPostAddVO;
import com.xd11cc.single.entity.vo.SystemPostQueryVO;
import com.xd11cc.single.entity.vo.SystemPostUpdateVO;
import com.xd11cc.single.enums.SystemErrorEnum;
import com.xd11cc.single.mapper.SystemPostMapper;
import com.xd11cc.single.service.ISystemDeptPostService;
import com.xd11cc.single.service.ISystemPostService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author xd11cc
 * @date 2026-05-27
 */
@Slf4j
@Service
public class SystemPostServiceImpl extends ServiceImpl<SystemPostMapper, SystemPostDO> implements ISystemPostService {

    @Autowired
    private ISystemDeptPostService systemDeptPostService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int add(SystemPostAddVO systemPostAddVO) {
        SystemPostDO systemPostDO = SystemPostConvert.INSTANCE.addVO2DO(systemPostAddVO);
        int row;
        try {
            row = baseMapper.insert(systemPostDO);
        } catch (DuplicateKeyException e) {
            throw new ServiceException(SystemErrorEnum.POST_CODE_EXISTS);
        }
        if (row > 0) {
            saveDeptPosts(systemPostDO.getId(), systemPostAddVO.getDeptIds());
        }
        return row;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteByIds(List<Long> ids) {
        int row = baseMapper.deleteBatchIds(ids);
        if (row > 0) {
            systemDeptPostService.remove(new LambdaQueryWrapper<SystemDeptPostDO>()
                    .in(SystemDeptPostDO::getPostId, ids));
        }
        return row;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int modifyById(SystemPostUpdateVO systemPostUpdateVO) {
        SystemPostDO systemPostDO = SystemPostConvert.INSTANCE.updateVO2DO(systemPostUpdateVO);
        int row;
        try {
            row = baseMapper.updateById(systemPostDO);
        } catch (DuplicateKeyException e) {
            throw new ServiceException(SystemErrorEnum.POST_CODE_EXISTS);
        }
        if (row > 0) {
            systemDeptPostService.remove(new LambdaQueryWrapper<SystemDeptPostDO>()
                    .eq(SystemDeptPostDO::getPostId, systemPostUpdateVO.getId()));
            saveDeptPosts(systemPostUpdateVO.getId(), systemPostUpdateVO.getDeptIds());
        }
        return row;
    }

    @Override
    public List<SystemPostDO> getList(SystemPostQueryVO systemPostQueryVO) {
        LambdaQueryWrapper<SystemPostDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.isNotEmpty(systemPostQueryVO.getPostCode()),
                SystemPostDO::getPostCode, systemPostQueryVO.getPostCode());
        wrapper.like(StringUtils.isNotEmpty(systemPostQueryVO.getPostName()),
                SystemPostDO::getPostName, systemPostQueryVO.getPostName());
        wrapper.eq(StringUtils.isNotEmpty(systemPostQueryVO.getStatus()),
                SystemPostDO::getStatus, systemPostQueryVO.getStatus());
        wrapper.orderByDesc(SystemPostDO::getId);
        return baseMapper.selectList(wrapper);
    }

    @Override
    public List<Long> getDeptIdsByPostId(Long postId) {
        List<SystemDeptPostDO> deptPosts = systemDeptPostService.list(new LambdaQueryWrapper<SystemDeptPostDO>()
                .eq(SystemDeptPostDO::getPostId, postId));
        return deptPosts.stream()
                .map(SystemDeptPostDO::getDeptId)
                .collect(Collectors.toList());
    }

    @Override
    public List<SystemPostDO> getListByDeptId(Long deptId) {
        List<SystemDeptPostDO> deptPosts = systemDeptPostService.list(new LambdaQueryWrapper<SystemDeptPostDO>()
                .eq(SystemDeptPostDO::getDeptId, deptId));
        List<Long> postIds = deptPosts.stream()
                .map(SystemDeptPostDO::getPostId)
                .collect(Collectors.toList());
        if (postIds.isEmpty()) {
            return Collections.emptyList();
        }
        return baseMapper.selectBatchIds(postIds);
    }

    private void saveDeptPosts(Long postId, List<Long> deptIds) {
        if (deptIds == null || deptIds.isEmpty()) {
            return;
        }
        List<SystemDeptPostDO> deptPosts = deptIds.stream().map(deptId -> {
            SystemDeptPostDO deptPost = new SystemDeptPostDO();
            deptPost.setPostId(postId);
            deptPost.setDeptId(deptId);
            return deptPost;
        }).collect(Collectors.toList());
        systemDeptPostService.saveBatch(deptPosts);
    }
}
