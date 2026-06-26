package com.xd11cc.single.config.schedule.quartz;

import cn.hutool.extra.spring.SpringUtil;
import com.xd11cc.single.entity.domain.SystemJobDO;
import com.xd11cc.single.entity.domain.SystemJobLogDO;
import com.xd11cc.single.service.ISystemJobLogService;
import com.xd11cc.single.utils.JobInvokeUtils;
import com.xd11cc.single.utils.TenantUtils;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.springframework.scheduling.quartz.QuartzJobBean;

/**
 * @author xd11cc
 * @date 2026-06-11
 */
@Slf4j
public abstract class AbstractQuartzJob extends QuartzJobBean {

    public static final String JOB_DATA_KEY = "TASK_PROPERTIES";

    @Override
    protected void executeInternal(JobExecutionContext context) {
        JobDataMap dataMap = context.getMergedJobDataMap();
        SystemJobDO jobDO = (SystemJobDO) dataMap.get(JOB_DATA_KEY);

        SystemJobLogDO jobLog = new SystemJobLogDO();
        jobLog.setJobId(jobDO.getId());
        jobLog.setJobName(jobDO.getJobName());
        jobLog.setJobGroup(jobDO.getJobGroup());
        jobLog.setInvokeTarget(jobDO.getInvokeTarget());
        jobLog.setCreateUserId(-1L);
        jobLog.setUpdateUserId(-1L);

        long startTime = System.currentTimeMillis();
        try {
            JobInvokeUtils.invokeMethod(jobDO.getInvokeTarget());
            jobLog.setStatus("0");
            jobLog.setJobMessage(jobDO.getJobName() + " 执行成功，耗时：" + (System.currentTimeMillis() - startTime) + "ms");
        } catch (Exception e) {
            jobLog.setStatus("1");
            jobLog.setJobMessage(jobDO.getJobName() + " 执行失败：" + e.getMessage());
            log.error("定时任务执行异常 - 任务名称：{}，调用目标：{}", jobDO.getJobName(), jobDO.getInvokeTarget(), e);
        } finally {
            TenantUtils.execute(jobDO.getTenantId(), () ->{
                SpringUtil.getBean(ISystemJobLogService.class).save(jobLog);
            });
        }
    }
}
