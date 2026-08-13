package com.xd11cc.single.config.context;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DynamicDataSourceContextHolderTest {

    // ==================== setDataSourceType / getDataSourceType ====================

    @Test
    void setDataSourceType_后能正确获取() {
        DynamicDataSourceContextHolder.setDataSourceType("master");
        assertThat(DynamicDataSourceContextHolder.getDataSourceType()).isEqualTo("master");
    }

    @Test
    void getDataSourceType_未设置时返回null() {
        DynamicDataSourceContextHolder.clearDataSourceType();
        assertThat(DynamicDataSourceContextHolder.getDataSourceType()).isNull();
    }

    // ==================== 线程隔离验证 ====================

    @Test
    void 线程隔离_不同线程互不影响() throws Exception {
        DynamicDataSourceContextHolder.setDataSourceType("master");

        java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
        java.util.concurrent.atomic.AtomicReference<String> childResult = new java.util.concurrent.atomic.AtomicReference<>();
        Thread child = new Thread(() -> {
            try {
                latch.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            childResult.set(DynamicDataSourceContextHolder.getDataSourceType());
        });
        child.start();
        DynamicDataSourceContextHolder.setDataSourceType("slave");
        latch.countDown();
        child.join();

        // TransmittableThreadLocal 会继承父线程的值
        assertThat(childResult.get()).isEqualTo("master");
        assertThat(DynamicDataSourceContextHolder.getDataSourceType()).isEqualTo("slave");
    }

    // ==================== clearDataSourceType ====================

    @Test
    void clearDataSourceType_清除后获取为null() {
        DynamicDataSourceContextHolder.setDataSourceType("slave");
        DynamicDataSourceContextHolder.clearDataSourceType();
        assertThat(DynamicDataSourceContextHolder.getDataSourceType()).isNull();
    }
}
