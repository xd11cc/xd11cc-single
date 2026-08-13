package com.xd11cc.single.config.context;

import com.xd11cc.single.enums.SystemErrorEnum;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TenantContextHolderTest {

    @AfterEach
    void tearDown() {
        TenantContextHolder.clear();
    }

    // ==================== setTenantId / getTenantId ====================

    @Test
    void setTenantId_后能正确获取() {
        TenantContextHolder.setTenantId(10L);
        assertThat(TenantContextHolder.getTenantId()).isEqualTo(10L);
    }

    @Test
    void setTenantId_null_能正确获取null() {
        TenantContextHolder.setTenantId(null);
        assertThat(TenantContextHolder.getTenantId()).isNull();
    }

    // ==================== getRequiredTenantId ====================

    @Test
    void getRequiredTenantId_已设置_返回租户ID() {
        TenantContextHolder.setTenantId(5L);
        assertThat(TenantContextHolder.getRequiredTenantId()).isEqualTo(5L);
    }

    @Test
    void getRequiredTenantId_未设置_抛ServiceException() {
        TenantContextHolder.clear();
        assertThatThrownBy(() -> TenantContextHolder.getRequiredTenantId())
                .isInstanceOf(com.xd11cc.single.config.exception.ServiceException.class);
    }

    // ==================== setIgnore / isIgnore ====================

    @Test
    void setIgnore_true_返回true() {
        TenantContextHolder.setIgnore(true);
        assertThat(TenantContextHolder.isIgnore()).isTrue();
    }

    @Test
    void setIgnore_false_返回false() {
        TenantContextHolder.setIgnore(false);
        assertThat(TenantContextHolder.isIgnore()).isFalse();
    }

    @Test
    void setIgnore_null_返回false() {
        TenantContextHolder.setIgnore(null);
        assertThat(TenantContextHolder.isIgnore()).isFalse();
    }

    // ==================== isTenantAware / setTenantAware ====================

    @Test
    void isTenantAware_默认返回true() {
        TenantContextHolder.clear();
        assertThat(TenantContextHolder.isTenantAware()).isTrue();
    }

    @Test
    void setTenantAware_false_返回false() {
        TenantContextHolder.setTenantAware(false);
        assertThat(TenantContextHolder.isTenantAware()).isFalse();
    }

    // ==================== runWithoutTenantAwareness ====================

    @Test
    void runWithoutTenantAwareness_执行期间关闭租户感知() {
        TenantContextHolder.setTenantId(10L);
        TenantContextHolder.setTenantAware(true);

        TenantContextHolder.runWithoutTenantAwareness(() -> {
            assertThat(TenantContextHolder.isTenantAware()).isFalse();
            assertThat(TenantContextHolder.getTenantId()).isEqualTo(10L);
        });

        assertThat(TenantContextHolder.isTenantAware()).isTrue();
        assertThat(TenantContextHolder.getTenantId()).isEqualTo(10L);
    }

    @Test
    void runWithoutTenantAwareness_供应商返回结果() {
        TenantContextHolder.setTenantId(10L);

        String result = TenantContextHolder.runWithoutTenantAwareness(() -> "hello");

        assertThat(result).isEqualTo("hello");
        assertThat(TenantContextHolder.isTenantAware()).isTrue();
    }

    // ==================== runAsTenant ====================

    @Test
    void runAsTenant_切换租户ID执行() {
        TenantContextHolder.setTenantId(10L);

        TenantContextHolder.runAsTenant(20L, () -> {
            assertThat(TenantContextHolder.getTenantId()).isEqualTo(20L);
        });

        assertThat(TenantContextHolder.getTenantId()).isEqualTo(10L);
    }

    @Test
    void runAsTenant_供应商返回结果() {
        TenantContextHolder.setTenantId(10L);

        Integer result = TenantContextHolder.runAsTenant(20L, () -> 42);

        assertThat(result).isEqualTo(42);
        assertThat(TenantContextHolder.getTenantId()).isEqualTo(10L);
    }

    @Test
    void runAsTenant_当前为空时切换() {
        TenantContextHolder.clear();

        TenantContextHolder.runAsTenant(30L, () -> {
            assertThat(TenantContextHolder.getTenantId()).isEqualTo(30L);
        });

        assertThat(TenantContextHolder.getTenantId()).isNull();
    }

    // ==================== clear ====================

    @Test
    void clear_清除所有上下文() {
        TenantContextHolder.setTenantId(10L);
        TenantContextHolder.setIgnore(true);
        TenantContextHolder.setTenantAware(false);

        TenantContextHolder.clear();

        assertThat(TenantContextHolder.getTenantId()).isNull();
        assertThat(TenantContextHolder.isIgnore()).isFalse();
        assertThat(TenantContextHolder.isTenantAware()).isTrue();
    }

    // ==================== 线程隔离 ====================

    @Test
    void 线程隔离_子线程不继承父线程的租户ID() throws Exception {
        TenantContextHolder.setTenantId(10L);

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Long> childTenantId = new AtomicReference<>();
        Thread child = new Thread(() -> {
            try {
                latch.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            childTenantId.set(TenantContextHolder.getTenantId());
        });
        child.start();

        TenantContextHolder.setTenantId(20L);
        latch.countDown();
        child.join();

        // 父线程改变不影响子线程已读取的值
        assertThat(childTenantId.get()).isEqualTo(10L);
        assertThat(TenantContextHolder.getTenantId()).isEqualTo(20L);
    }
}
