package com.xd11cc.single.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xd11cc.single.config.context.TenantContextHolder;
import com.xd11cc.single.config.exception.ServiceException;
import com.xd11cc.single.convert.SystemJobConvert;
import com.xd11cc.single.entity.domain.SystemJobDO;
import com.xd11cc.single.entity.vo.SystemJobAddVO;
import com.xd11cc.single.entity.vo.SystemJobQueryVO;
import com.xd11cc.single.entity.vo.SystemJobUpdateVO;
import com.xd11cc.single.enums.SystemErrorEnum;
import com.xd11cc.single.enums.SystemStatusEnum;
import com.xd11cc.single.mapper.SystemJobMapper;
import com.xd11cc.single.service.ISystemJobService;
import com.xd11cc.single.utils.ScheduleUtils;
import com.xd11cc.single.utils.TenantUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.quartz.Scheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * @author xd11cc
 * @date 2026-06-11
 */
@Slf4j
@Service
public class SystemJobServiceImpl extends ServiceImpl<SystemJobMapper, SystemJobDO> implements ISystemJobService {

    @Autowired
    private Scheduler scheduler;

    @PostConstruct
    public void init() {
        TenantUtils.executeIgnore(() -> {
            List<SystemJobDO> jobList = baseMapper.selectList(new LambdaQueryWrapper<>());
            for (SystemJobDO jobDO : jobList) {
                try {
                    ScheduleUtils.createScheduleJob(scheduler, jobDO);
                } catch (Exception e) {
                    log.error("初始化定时任务失败 - 任务名称：{}", jobDO.getJobName(), e);
                }
            }
            log.info("定时任务初始化完成，共加载 {} 个任务", jobList.size());
            return null;
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int add(SystemJobAddVO addVO) {
        ScheduleUtils.checkCronExpression(addVO.getCronExpression());

        SystemJobDO jobDO = SystemJobConvert.INSTANCE.addVO2DO(addVO);
        jobDO.setStatus(SystemStatusEnum.NORMAL.getCode());

        int row = baseMapper.insert(jobDO);
        if (row > 0) {
            try {
                jobDO.setTenantId(TenantContextHolder.getTenantId());
                ScheduleUtils.createScheduleJob(scheduler, jobDO);
            } catch (Exception e) {
                throw new ServiceException(SystemErrorEnum.JOB_RUN_FAILURE, new Object[]{e.getMessage()});
            }
        }
        return row;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int modifyById(SystemJobUpdateVO updateVO) {
        ScheduleUtils.checkCronExpression(updateVO.getCronExpression());

        SystemJobDO jobDO = SystemJobConvert.INSTANCE.updateVO2DO(updateVO);
        int row = baseMapper.updateById(jobDO);
        if (row > 0) {
            try {
                jobDO.setTenantId(TenantContextHolder.getTenantId());
                ScheduleUtils.updateScheduleJob(scheduler, jobDO);
            } catch (Exception e) {
                throw new ServiceException(SystemErrorEnum.JOB_RUN_FAILURE, new Object[]{e.getMessage()});
            }
        }
        return row;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteByIds(List<Long> ids) {
        List<SystemJobDO> jobList = baseMapper.selectBatchIds(ids);
        int row = baseMapper.deleteBatchIds(ids);
        if (row > 0) {
            for (SystemJobDO jobDO : jobList) {
                try {
                    ScheduleUtils.deleteScheduleJob(scheduler, jobDO.getId(), jobDO.getJobGroup());
                } catch (Exception e) {
                    log.error("删除定时任务失败 - 任务名称：{}", jobDO.getJobName(), e);
                }
            }
        }
        return row;
    }

    @Override
    public List<SystemJobDO> getList(SystemJobQueryVO queryVO) {
        LambdaQueryWrapper<SystemJobDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.isNotEmpty(queryVO.getJobName()),
                SystemJobDO::getJobName, queryVO.getJobName());
        wrapper.eq(StringUtils.isNotEmpty(queryVO.getJobGroup()),
                SystemJobDO::getJobGroup, queryVO.getJobGroup());
        wrapper.eq(StringUtils.isNotEmpty(queryVO.getStatus()),
                SystemJobDO::getStatus, queryVO.getStatus());
        wrapper.orderByDesc(SystemJobDO::getId);
        return baseMapper.selectList(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int changeStatus(Long id, String status) {
        SystemJobDO jobDO = baseMapper.selectById(id);
        if (jobDO == null) {
            throw new ServiceException(SystemErrorEnum.JOB_NOT_FOUND);
        }
        jobDO.setStatus(status);
        int row = baseMapper.updateById(jobDO);
        if (row > 0) {
            try {
                if (SystemStatusEnum.NORMAL.getCode().equals(status)) {
                    ScheduleUtils.resumeJob(scheduler, jobDO.getId(), jobDO.getJobGroup());
                } else {
                    ScheduleUtils.pauseJob(scheduler, jobDO.getId(), jobDO.getJobGroup());
                }
            } catch (Exception e) {
                throw new ServiceException(SystemErrorEnum.JOB_RUN_FAILURE, new Object[]{e.getMessage()});
            }
        }
        return row;
    }

    @Override
    public void runOnce(Long id) {
        SystemJobDO jobDO = baseMapper.selectById(id);
        if (jobDO == null) {
            throw new ServiceException(SystemErrorEnum.JOB_NOT_FOUND);
        }
        try {
            ScheduleUtils.runOnce(scheduler, jobDO.getId(), jobDO.getJobGroup());
        } catch (Exception e) {
            throw new ServiceException(SystemErrorEnum.JOB_RUN_FAILURE, new Object[]{e.getMessage()});
        }
    }
}
