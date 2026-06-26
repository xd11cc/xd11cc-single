package com.xd11cc.single.config.schedule.quartz;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.PersistJobDataAfterExecution;

/**
 * @author xd11cc
 * @date 2026-06-11
 */
@DisallowConcurrentExecution
@PersistJobDataAfterExecution
public class QuartzDisallowConcurrentExecution extends AbstractQuartzJob {
}
