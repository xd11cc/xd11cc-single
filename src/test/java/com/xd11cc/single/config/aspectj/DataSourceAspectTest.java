package com.xd11cc.single.config.aspectj;

import com.xd11cc.single.config.annotation.DataSource;
import com.xd11cc.single.config.context.DynamicDataSourceContextHolder;
import com.xd11cc.single.enums.DataSourceEnum;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class DataSourceAspectTest {

    private final DataSourceAspect aspect = new DataSourceAspect();

    @AfterEach
    void tearDown() {
        DynamicDataSourceContextHolder.clearDataSourceType();
    }

    // ==================== 辅助类（带 @DataSource 注解，必须 public） ====================

    public static class SlaveMethodService {
        @DataSource(DataSourceEnum.SLAVE)
        public String queryData() {
            return "data";
        }
    }

    public static class MasterMethodService {
        @DataSource(DataSourceEnum.MASTER)
        public String queryData() {
            return "data";
        }
    }

    @DataSource(DataSourceEnum.SLAVE)
    public static class SlaveClassService {
        public String queryData() {
            return "data";
        }
    }

    public static class NoAnnotationService {
        public String queryData() {
            return "data";
        }
    }

    // 方法级 @DataSource(MASTER) 覆盖类级 @DataSource(SLAVE)
    public static class OverrideChildService extends SlaveClassService {
        @Override
        @DataSource(DataSourceEnum.MASTER)
        public String queryData() {
            return "data";
        }
    }

    // ==================== 方法级 @DataSource(SLAVE) ====================

    @Test
    void doAround_方法上有DataSource_SLAVE_执行期间切换从库() throws Throwable {
        Method method = SlaveMethodService.class.getMethod("queryData");
        ProceedingJoinPoint joinPoint = mockJoinPoint(SlaveMethodService.class, method);

        // 验证执行期间上下文被设置为 SLAVE
        given(joinPoint.proceed()).willAnswer(invocation -> {
            assertThat(DynamicDataSourceContextHolder.getDataSourceType()).isEqualTo("SLAVE");
            return "data";
        });

        Object result = aspect.doAround(joinPoint);

        assertThat(result).isEqualTo("data");
        // finally 执行了 clear
        assertThat(DynamicDataSourceContextHolder.getDataSourceType()).isNull();
    }

    // ==================== 方法级 @DataSource(MASTER) ====================

    @Test
    void doAround_方法上有DataSource_MASTER_执行期间切换主库() throws Throwable {
        Method method = MasterMethodService.class.getMethod("queryData");
        ProceedingJoinPoint joinPoint = mockJoinPoint(MasterMethodService.class, method);

        given(joinPoint.proceed()).willAnswer(invocation -> {
            assertThat(DynamicDataSourceContextHolder.getDataSourceType()).isEqualTo("MASTER");
            return "data";
        });

        Object result = aspect.doAround(joinPoint);

        assertThat(result).isEqualTo("data");
        assertThat(DynamicDataSourceContextHolder.getDataSourceType()).isNull();
    }

    // ==================== 类级 @DataSource(SLAVE) ====================

    @Test
    void doAround_类上有DataSource_方法无_执行期间使用类上值() throws Throwable {
        Method method = SlaveClassService.class.getMethod("queryData");
        ProceedingJoinPoint joinPoint = mockJoinPoint(SlaveClassService.class, method);

        given(joinPoint.proceed()).willAnswer(invocation -> {
            assertThat(DynamicDataSourceContextHolder.getDataSourceType()).isEqualTo("SLAVE");
            return "data";
        });

        Object result = aspect.doAround(joinPoint);

        assertThat(result).isEqualTo("data");
        assertThat(DynamicDataSourceContextHolder.getDataSourceType()).isNull();
    }

    // ==================== 方法级优先于类级 ====================

    @Test
    void doAround_方法级覆盖类级_执行期间使用方法上的值() throws Throwable {
        Method method = OverrideChildService.class.getMethod("queryData");
        ProceedingJoinPoint joinPoint = mockJoinPoint(OverrideChildService.class, method);

        given(joinPoint.proceed()).willAnswer(invocation -> {
            assertThat(DynamicDataSourceContextHolder.getDataSourceType()).isEqualTo("MASTER");
            return "data";
        });

        Object result = aspect.doAround(joinPoint);

        assertThat(result).isEqualTo("data");
        assertThat(DynamicDataSourceContextHolder.getDataSourceType()).isNull();
    }

    // ==================== 无注解 ====================

    @Test
    void doAround_无DataSource注解_执行期间不切换数据源() throws Throwable {
        Method method = NoAnnotationService.class.getMethod("queryData");
        ProceedingJoinPoint joinPoint = mockJoinPoint(NoAnnotationService.class, method);

        given(joinPoint.proceed()).willAnswer(invocation -> {
            assertThat(DynamicDataSourceContextHolder.getDataSourceType()).isNull();
            return "data";
        });

        Object result = aspect.doAround(joinPoint);

        assertThat(result).isEqualTo("data");
        assertThat(DynamicDataSourceContextHolder.getDataSourceType()).isNull();
    }

    // ==================== 异常处理 ====================

    @Test
    void doAround_方法抛出异常_传播异常且清除数据源() throws Throwable {
        Method method = SlaveMethodService.class.getMethod("queryData");
        ProceedingJoinPoint joinPoint = mockJoinPoint(SlaveMethodService.class, method);
        given(joinPoint.proceed()).willThrow(new RuntimeException("db error"));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> aspect.doAround(joinPoint));
        assertThat(ex.getMessage()).isEqualTo("db error");

        // finally 块执行了 clear
        assertThat(DynamicDataSourceContextHolder.getDataSourceType()).isNull();
    }

    @Test
    void doAround_方法返回null_不抛出异常() throws Throwable {
        Method method = SlaveMethodService.class.getMethod("queryData");
        ProceedingJoinPoint joinPoint = mockJoinPoint(SlaveMethodService.class, method);

        Object result = aspect.doAround(joinPoint);

        assertThat(result).isNull();
        assertThat(DynamicDataSourceContextHolder.getDataSourceType()).isNull();
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
