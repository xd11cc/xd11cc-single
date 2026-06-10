package com.xd11cc.single.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xd11cc.single.entity.domain.SystemOperateLogDO;
import com.xd11cc.single.entity.vo.SystemOperateLogQueryVO;
import com.xd11cc.single.mapper.SystemOperateLogMapper;
import com.xd11cc.single.service.ISystemOperateLogService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author xd11cc
 * @date 2026-05-28
 */
@Slf4j
@Service
public class SystemOperateLogServiceImpl extends ServiceImpl<SystemOperateLogMapper, SystemOperateLogDO> implements ISystemOperateLogService {

    @Override
    public void saveLog(SystemOperateLogDO operateLog) {
        baseMapper.insert(operateLog);
    }

    @Override
    public List<SystemOperateLogDO> getList(SystemOperateLogQueryVO queryVO) {
        LambdaQueryWrapper<SystemOperateLogDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.isNotEmpty(queryVO.getModule()),
                SystemOperateLogDO::getModule, queryVO.getModule());
        wrapper.eq(StringUtils.isNotEmpty(queryVO.getOperateType()),
                SystemOperateLogDO::getOperateType, queryVO.getOperateType());
        wrapper.eq(StringUtils.isNotEmpty(queryVO.getStatus()),
                SystemOperateLogDO::getStatus, queryVO.getStatus());
        wrapper.ge(queryVO.getBeginTime() != null,
                SystemOperateLogDO::getCreateTime, queryVO.getBeginTime());
        wrapper.le(queryVO.getEndTime() != null,
                SystemOperateLogDO::getCreateTime, queryVO.getEndTime());
        wrapper.orderByDesc(SystemOperateLogDO::getId);
        return baseMapper.selectList(wrapper);
    }

    @Override
    public int deleteByIds(List<Long> ids) {
        return baseMapper.deleteBatchIds(ids);
    }

    @Override
    public void clean() {
        baseMapper.delete(new LambdaQueryWrapper<>());
    }
}
