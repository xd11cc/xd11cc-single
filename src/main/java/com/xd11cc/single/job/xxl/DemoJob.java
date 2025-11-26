package com.xd11cc.single.job.xxl;

import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author xd11cc
 * @date 2025-11-26 14:42:28
 */
@Slf4j
@Component
public class DemoJob {

    @XxlJob("DemoJob")
    public void demoJob(){
        log.info("xxl-job demoJob...");
    }
}
