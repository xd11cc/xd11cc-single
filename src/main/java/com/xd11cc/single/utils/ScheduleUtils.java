package com.xd11cc.single.utils;

import com.xd11cc.single.config.schedule.quartz.AbstractQuartzJob;
import com.xd11cc.single.config.schedule.quartz.QuartzDisallowConcurrentExecution;
import com.xd11cc.single.config.schedule.quartz.QuartzJobExecution;
import com.xd11cc.single.config.exception.ServiceException;
import com.xd11cc.single.entity.domain.SystemJobDO;
import com.xd11cc.single.enums.SystemErrorEnum;
import org.quartz.*;

/**
 * @author xd11cc
 * @date 2026-06-11
 */
public class ScheduleUtils {

    private static final String SYSTEM_JOB = "SYSTEM_JOB";

    public static JobKey getJobKey(Long jobId, String jobGroup) {
        return JobKey.jobKey(SYSTEM_JOB + jobId, jobGroup);
    }

    public static TriggerKey getTriggerKey(Long jobId, String jobGroup) {
        return TriggerKey.triggerKey(SYSTEM_JOB + jobId, jobGroup);
    }

    public static void createScheduleJob(Scheduler scheduler, SystemJobDO jobDO) throws SchedulerException {
        Class<? extends Job> jobClass = "0".equals(jobDO.getConcurrent())
                ? QuartzDisallowConcurrentExecution.class : QuartzJobExecution.class;

        JobDetail jobDetail = JobBuilder.newJob(jobClass)
                .withIdentity(getJobKey(jobDO.getId(), jobDO.getJobGroup()))
                .setJobData(buildJobDataMap(jobDO))
                .build();

        scheduler.scheduleJob(jobDetail, buildCronTrigger(jobDO));

        if ("1".equals(jobDO.getStatus())) {
            scheduler.pauseJob(getJobKey(jobDO.getId(), jobDO.getJobGroup()));
        }
    }

    public static void updateScheduleJob(Scheduler scheduler, SystemJobDO jobDO) throws SchedulerException {
        JobDetail existingJob = scheduler.getJobDetail(getJobKey(jobDO.getId(), jobDO.getJobGroup()));
        if (existingJob == null) {
            throw new ServiceException(SystemErrorEnum.JOB_NOT_FOUND);
        }
        JobDetail jobDetail = JobBuilder.newJob(existingJob.getJobClass())
                .withIdentity(getJobKey(jobDO.getId(), jobDO.getJobGroup()))
                .setJobData(buildJobDataMap(jobDO))
                .storeDurably()
                .build();

        scheduler.addJob(jobDetail, true);
        scheduler.rescheduleJob(getTriggerKey(jobDO.getId(), jobDO.getJobGroup()), buildCronTrigger(jobDO));

        if ("1".equals(jobDO.getStatus())) {
            scheduler.pauseJob(getJobKey(jobDO.getId(), jobDO.getJobGroup()));
        }
    }

    private static JobDataMap buildJobDataMap(SystemJobDO jobDO) {
        JobDataMap dataMap = new JobDataMap();
        dataMap.put(AbstractQuartzJob.JOB_DATA_KEY, jobDO);
        return dataMap;
    }

    public static void deleteScheduleJob(Scheduler scheduler, Long jobId, String jobGroup) throws SchedulerException {
        scheduler.deleteJob(getJobKey(jobId, jobGroup));
    }

    public static void pauseJob(Scheduler scheduler, Long jobId, String jobGroup) throws SchedulerException {
        scheduler.pauseJob(getJobKey(jobId, jobGroup));
    }

    public static void resumeJob(Scheduler scheduler, Long jobId, String jobGroup) throws SchedulerException {
        scheduler.resumeJob(getJobKey(jobId, jobGroup));
    }

    public static void runOnce(Scheduler scheduler, Long jobId, String jobGroup) throws SchedulerException {
        scheduler.triggerJob(getJobKey(jobId, jobGroup));
    }

    public static boolean checkCronExpression(String cronExpression) {
        try {
            new CronExpression(cronExpression);
            return true;
        } catch (Exception e) {
            throw new ServiceException(SystemErrorEnum.CRON_EXPRESSION_ERROR);
        }
    }

    private static CronTrigger buildCronTrigger(SystemJobDO jobDO) {
        CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(jobDO.getCronExpression());
        switch (jobDO.getExecutionPolicy()) {
            case "2":
                scheduleBuilder = scheduleBuilder.withMisfireHandlingInstructionFireAndProceed();
                break;
            case "3":
                scheduleBuilder = scheduleBuilder.withMisfireHandlingInstructionIgnoreMisfires();
                break;
            default:
                scheduleBuilder = scheduleBuilder.withMisfireHandlingInstructionDoNothing();
                break;
        }
        return TriggerBuilder.newTrigger()
                .withIdentity(getTriggerKey(jobDO.getId(), jobDO.getJobGroup()))
                .withSchedule(scheduleBuilder)
                .startNow()
                .build();
    }
}
