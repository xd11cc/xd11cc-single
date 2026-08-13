package com.xd11cc.single.config.aspectj;

import com.xd11cc.single.config.annotation.TenantIgnore;
import com.xd11cc.single.config.context.TenantContextHolder;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class TenantIgnoreAspectTest {

    private final TenantIgnoreAspect aspect = new TenantIgnoreAspect();

    @AfterEach
    void tearDown() {
        TenantContextHolder.clear();
    }

    // ==================== 辅助类 ====================

    public static class IgnoredService {
        @TenantIgnore
        public String queryAll() {
            return "all";
        }
    }

    // ==================== @TenantIgnore 注解方法 ====================

    @Test
    void around_有TenantIgnore注解_执行期间忽略租户() throws Throwable {
        Method method = IgnoredService.class.getMethod("queryAll");
        ProceedingJoinPoint joinPoint = mockJoinPoint(IgnoredService.class, method);
        TenantIgnore tenantIgnore = method.getAnnotation(TenantIgnore.class);

        TenantContextHolder.setIgnore(false);
        TenantContextHolder.setTenantId(1L);

        given(joinPoint.proceed()).willAnswer(invocation -> {
            assertThat(TenantContextHolder.isIgnore()).isTrue();
            assertThat(TenantContextHolder.getTenantId()).isEqualTo(1L);
            return "all";
        });

        Object result = aspect.around(joinPoint, tenantIgnore);

        assertThat(result).isEqualTo("all");
        // finally 恢复了原始状态
        assertThat(TenantContextHolder.isIgnore()).isFalse();
        assertThat(TenantContextHolder.getTenantId()).isEqualTo(1L);
    }

    // ==================== 异常处理 ====================

    @Test
    void around_方法抛出异常_仍恢复原始忽略状态() throws Throwable {
        Method method = IgnoredService.class.getMethod("queryAll");
        ProceedingJoinPoint joinPoint = mockJoinPoint(IgnoredService.class, method);
        given(joinPoint.proceed()).willThrow(new RuntimeException("db error"));
        TenantIgnore tenantIgnore = method.getAnnotation(TenantIgnore.class);

        TenantContextHolder.setIgnore(true);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> aspect.around(joinPoint, tenantIgnore));
        assertThat(ex.getMessage()).isEqualTo("db error");

        // finally 恢复了原始状态
        assertThat(TenantContextHolder.isIgnore()).isTrue();
    }

    // ==================== 嵌套恢复 ====================

    @Test
    void around_原本已忽略_不改变忽略状态() throws Throwable {
        Method method = IgnoredService.class.getMethod("queryAll");
        ProceedingJoinPoint joinPoint = mockJoinPoint(IgnoredService.class, method);
        TenantIgnore tenantIgnore = method.getAnnotation(TenantIgnore.class);

        TenantContextHolder.setIgnore(true);

        given(joinPoint.proceed()).willAnswer(invocation -> {
            assertThat(TenantContextHolder.isIgnore()).isTrue();
            return "all";
        });

        Object result = aspect.around(joinPoint, tenantIgnore);

        assertThat(result).isEqualTo("all");
        assertThat(TenantContextHolder.isIgnore()).isTrue();
    }

    // ==================== 辅助方法 ====================

    private ProceedingJoinPoint mockJoinPoint(Class<?> targetClass, Method method) throws Throwable {
        MethodSignature signature = mock(MethodSignature.class);
        given(signature.getMethod()).willReturn(method);
        given(signature.getDeclaringType()).willReturn(targetClass);

        Object targetInstance;
        try {
            targetInstance = targetClass.newInstance();
        } catch (Exception e) {
            throw new RuntimeException("无法实例化目标类 " + targetClass.getName() + "，需要 public 无参构造函数", e);
        }

        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        given(joinPoint.getSignature()).willReturn(signature);
        given(joinPoint.getTarget()).willReturn(targetInstance);

        return joinPoint;
    }
}
