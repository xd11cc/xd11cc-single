package com.xd11cc.single.utils;

import com.xd11cc.single.config.context.TenantContextHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TenantUtilsTest {

    @AfterEach
    void tearDown() {
        TenantContextHolder.clear();
    }

    // ==================== execute(Runnable) ====================

    @Test
    void execute_设置租户ID_执行期间可获取_执行后恢复() {
        TenantContextHolder.setTenantId(1L);
        TenantContextHolder.setIgnore(true);

        TenantUtils.execute(2L, () -> {
            assertThat(TenantContextHolder.getTenantId()).isEqualTo(2L);
            assertThat(TenantContextHolder.isIgnore()).isFalse();
        });

        assertThat(TenantContextHolder.getTenantId()).isEqualTo(1L);
        assertThat(TenantContextHolder.isIgnore()).isTrue();
    }

    @Test
    void execute_原本无租户_执行后恢复为null() {
        TenantContextHolder.clear();

        TenantUtils.execute(5L, () -> {
            assertThat(TenantContextHolder.getTenantId()).isEqualTo(5L);
        });

        assertThat(TenantContextHolder.getTenantId()).isNull();
    }

    @Test
    void execute_返回supplier结果() {
        String result = TenantUtils.execute(1L, () -> "hello");
        assertThat(result).isEqualTo("hello");
    }

    @Test
    void execute_嵌套调用_外层租户不被污染() {
        TenantContextHolder.setTenantId(1L);

        TenantUtils.execute(2L, () -> {
            assertThat(TenantContextHolder.getTenantId()).isEqualTo(2L);
            TenantUtils.execute(3L, () -> {
                assertThat(TenantContextHolder.getTenantId()).isEqualTo(3L);
            });
            assertThat(TenantContextHolder.getTenantId()).isEqualTo(2L);
        });

        assertThat(TenantContextHolder.getTenantId()).isEqualTo(1L);
    }

    @Test
    void execute_Runnable_发生异常时仍恢复上下文() {
        TenantContextHolder.setTenantId(10L);
        TenantContextHolder.setIgnore(true);

        assertThatThrownBy(() ->
                TenantUtils.execute(20L, () -> { throw new RuntimeException("boom"); })
        ).isInstanceOf(RuntimeException.class);

        assertThat(TenantContextHolder.getTenantId()).isEqualTo(10L);
        assertThat(TenantContextHolder.isIgnore()).isTrue();
    }

    // ==================== executeIgnore(Runnable) ====================

    @Test
    void executeIgnore_执行期间忽略租户() {
        TenantContextHolder.setTenantId(1L);
        TenantContextHolder.setIgnore(false);

        TenantUtils.executeIgnore(1L, () -> assertThat(TenantContextHolder.isIgnore()).isTrue());

        assertThat(TenantContextHolder.isIgnore()).isFalse();
    }

    @Test
    void executeIgnore_原本未忽略_恢复为false() {
        TenantContextHolder.setIgnore(false);

        TenantUtils.executeIgnore(1L, () -> {
            assertThat(TenantContextHolder.isIgnore()).isTrue();
        });

        assertThat(TenantContextHolder.isIgnore()).isFalse();
    }

    @Test
    void executeIgnore_嵌套_外层恢复忽略状态() {
        // 验证 executeIgnore 的 finally 块在嵌套场景下正确恢复
        TenantContextHolder.setIgnore(false);
        TenantContextHolder.setTenantId(1L);

        TenantUtils.executeIgnore(2L, () -> {
            assertThat(TenantContextHolder.isIgnore()).isTrue();
            assertThat(TenantContextHolder.getTenantId()).isEqualTo(1L);  // executeIgnore 不改变 tenantId
        });

        assertThat(TenantContextHolder.getTenantId()).isEqualTo(1L);
        assertThat(TenantContextHolder.isIgnore()).isFalse();
    }

    @Test
    void executeIgnore_发生异常时恢复忽略状态() {
        TenantContextHolder.setIgnore(false);

        assertThatThrownBy(() -> {
            Long tenantId = 1L;
            TenantUtils.executeIgnore(tenantId, () -> { throw new RuntimeException("boom"); });
        }).isInstanceOf(RuntimeException.class);

        assertThat(TenantContextHolder.isIgnore()).isFalse();
    }

    // ==================== executeAndClear ====================

    @Test
    void executeAndClear_执行后租户上下文被清除() {
        TenantContextHolder.setTenantId(5L);

        TenantUtils.executeAndClear(9L, () -> {
            assertThat(TenantContextHolder.getTenantId()).isEqualTo(9L);
        });

        assertThat(TenantContextHolder.getTenantId()).isNull();
        assertThat(TenantContextHolder.isIgnore()).isFalse();
    }

    @Test
    void executeAndClear_返回supplier结果() {
        String result = TenantUtils.executeAndClear(1L, () -> "result");
        assertThat(result).isEqualTo("result");
        assertThat(TenantContextHolder.getTenantId()).isNull();
    }

    @Test
    void executeAndClear_发生异常时仍清除() {
        TenantContextHolder.setTenantId(5L);

        assertThatThrownBy(() ->
                TenantUtils.executeAndClear(9L, () -> { throw new RuntimeException("boom"); })
        ).isInstanceOf(RuntimeException.class);

        assertThat(TenantContextHolder.getTenantId()).isNull();
    }
}

