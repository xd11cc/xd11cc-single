package com.xd11cc.single.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xd11cc.single.entity.domain.SystemJobLogDO;
import com.xd11cc.single.entity.vo.SystemJobLogQueryVO;
import com.xd11cc.single.mapper.SystemJobLogMapper;
import com.xd11cc.single.service.ISystemJobLogService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author xd11cc
 * @date 2026-06-11
 */
@Service
public class SystemJobLogServiceImpl extends ServiceImpl<SystemJobLogMapper, SystemJobLogDO> implements ISystemJobLogService {

    @Override
    public List<SystemJobLogDO> getList(SystemJobLogQueryVO queryVO) {
        LambdaQueryWrapper<SystemJobLogDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.isNotEmpty(queryVO.getJobName()),
                SystemJobLogDO::getJobName, queryVO.getJobName());
        wrapper.like(StringUtils.isNotEmpty(queryVO.getJobGroup()),
                SystemJobLogDO::getJobGroup, queryVO.getJobGroup());
        wrapper.eq(StringUtils.isNotEmpty(queryVO.getStatus()),
                SystemJobLogDO::getStatus, queryVO.getStatus());
        wrapper.orderByDesc(SystemJobLogDO::getId);
        return baseMapper.selectList(wrapper);
    }

    @Override
    public int deleteByIds(List<Long> ids) {
        return baseMapper.deleteBatchIds(ids);
    }
}
