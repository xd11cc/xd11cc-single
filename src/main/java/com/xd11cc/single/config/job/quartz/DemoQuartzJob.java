package com.xd11cc.single.config.job.quartz;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author xd11cc
 * @date 2026-06-04
 * 示例定时任务
 * 调用示例：demoQuartzJob.demoJob()
 */
@Slf4j
@Component("demoQuartzJob")
public class DemoQuartzJob {

    public void demoJob() {
        log.info("执行示例定时任务: {}", System.currentTimeMillis());
    }

    public void demoJobWithParams(String param) {
        log.info("执行示例定时任务(带参数): {}", param);
    }
}
