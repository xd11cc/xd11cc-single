package com.xd11cc.single.config.aspectj;

import io.swagger.annotations.ApiOperation;
import com.xd11cc.single.config.annotation.OperateLog;
import com.xd11cc.single.entity.domain.SystemOperateLogDO;
import com.xd11cc.single.enums.OperateStatusEnum;
import com.xd11cc.single.enums.OperateTypeEnum;
import com.xd11cc.single.service.ISystemOperateLogService;
import com.xd11cc.single.utils.IpUtils;
import com.xd11cc.single.utils.SecurityUtils;
import com.xd11cc.single.utils.ServletUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.concurrent.Executor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

class OperateLogAspectTest {

    // ==================== 正常执行 - 记录成功日志 ====================

    @Test
    void around_正常执行_记录成功日志() throws Throwable {
        ProceedingJoinPoint joinPoint = mockJoinPoint(TargetService.METHOD, "arg1", "arg2");
        given(joinPoint.proceed()).willReturn(123);

        ISystemOperateLogService logService = mock(ISystemOperateLogService.class);
        Executor executor = mockExecutor(logService);

        try (MockedStatic<ServletUtils> servletMock = mockStatic(ServletUtils.class);
             MockedStatic<IpUtils> ipMock = mockStatic(IpUtils.class)) {
            HttpServletRequest request = mock(HttpServletRequest.class);
            when(request.getMethod()).thenReturn("POST");
            when(request.getRequestURI()).thenReturn("/api/test");
            servletMock.when(ServletUtils::getRequest).thenReturn(request);
            ipMock.when(() -> IpUtils.getIpAddr(request)).thenReturn("192.168.1.1");

            OperateLogAspect aspect = new OperateLogAspect();
            injectServices(aspect, logService, executor);

            Object result = aspect.around(joinPoint);

            assertThat(result).isEqualTo(123);

            // 验证异步任务执行了 saveLog
            verify(logService).saveLog(argThat(logDO -> {
                assertThat(logDO.getModule()).isEqualTo("testModule");
                assertThat(logDO.getOperateType()).isEqualTo(OperateTypeEnum.ADD.getCode());
                assertThat(logDO.getOperateDesc()).isEqualTo("测试操作");
                assertThat(logDO.getMethod()).isEqualTo("TargetService.testMethod");
                assertThat(logDO.getRequestMethod()).isEqualTo("POST");
                assertThat(logDO.getRequestUrl()).isEqualTo("/api/test");
                assertThat(logDO.getOperateIp()).isEqualTo("192.168.1.1");
                assertThat(logDO.getStatus()).isEqualTo(OperateStatusEnum.SUCCESS.getCode());
                assertThat(logDO.getRequestParam()).contains("arg1").contains("arg2");
                assertThat(logDO.getResponseResult()).isEqualTo("123");
                assertThat(logDO.getErrorMsg()).isNull();
                assertThat(logDO.getCostTime()).isNotNull();
                return true;
            }));
        }
    }

    // ==================== 方法抛异常 - 记录失败日志 ====================

    @Test
    void around_方法抛异常_记录失败日志() throws Throwable {
        ProceedingJoinPoint joinPoint = mockJoinPoint(TargetService.METHOD, "arg1");
        given(joinPoint.proceed()).willThrow(new RuntimeException("boom"));

        ISystemOperateLogService logService = mock(ISystemOperateLogService.class);
        Executor executor = mockExecutor(logService);

        try (MockedStatic<ServletUtils> servletMock = mockStatic(ServletUtils.class);
             MockedStatic<IpUtils> ipMock = mockStatic(IpUtils.class)) {
            HttpServletRequest request = mock(HttpServletRequest.class);
            when(request.getMethod()).thenReturn("GET");
            when(request.getRequestURI()).thenReturn("/api/error");
            servletMock.when(ServletUtils::getRequest).thenReturn(request);
            ipMock.when(() -> IpUtils.getIpAddr(request)).thenReturn("10.0.0.1");

            OperateLogAspect aspect = new OperateLogAspect();
            injectServices(aspect, logService, executor);

            assertThatThrownBy(() -> aspect.around(joinPoint))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("boom");

            // 验证异步任务执行了 saveLog（finally 块）
            verify(logService).saveLog(argThat(logDO -> {
                assertThat(logDO.getStatus()).isEqualTo(OperateStatusEnum.FAIL.getCode());
                assertThat(logDO.getErrorMsg()).isEqualTo("boom");
                return true;
            }));
        }
    }

    // ==================== 不保存请求参数 ====================

    @Test
    void around_不保存请求参数_param为null() throws Throwable {
        ProceedingJoinPoint joinPoint = mockJoinPoint(NoParamService.METHOD, "secret");
        given(joinPoint.proceed()).willReturn("result");

        ISystemOperateLogService logService = mock(ISystemOperateLogService.class);
        Executor executor = mockExecutor(logService);

        try (MockedStatic<ServletUtils> servletMock = mockStatic(ServletUtils.class);
             MockedStatic<IpUtils> ipMock = mockStatic(IpUtils.class)) {
            HttpServletRequest request = mock(HttpServletRequest.class);
            when(request.getMethod()).thenReturn("POST");
            when(request.getRequestURI()).thenReturn("/api/noParam");
            servletMock.when(ServletUtils::getRequest).thenReturn(request);
            ipMock.when(() -> IpUtils.getIpAddr(request)).thenReturn("127.0.0.1");

            OperateLogAspect aspect = new OperateLogAspect();
            injectServices(aspect, logService, executor);

            aspect.around(joinPoint);

            verify(logService).saveLog(argThat(logDO -> {
                assertThat(logDO.getRequestParam()).isNull();
                return true;
            }));
        }
    }

    // ==================== 不保存响应结果 ====================

    @Test
    void around_不保存响应结果_result为null() throws Throwable {
        ProceedingJoinPoint joinPoint = mockJoinPoint(NoResultService.METHOD, "arg");
        given(joinPoint.proceed()).willReturn("result");

        ISystemOperateLogService logService = mock(ISystemOperateLogService.class);
        Executor executor = mockExecutor(logService);

        try (MockedStatic<ServletUtils> servletMock = mockStatic(ServletUtils.class);
             MockedStatic<IpUtils> ipMock = mockStatic(IpUtils.class)) {
            HttpServletRequest request = mock(HttpServletRequest.class);
            when(request.getMethod()).thenReturn("POST");
            when(request.getRequestURI()).thenReturn("/api/noResult");
            servletMock.when(ServletUtils::getRequest).thenReturn(request);
            ipMock.when(() -> IpUtils.getIpAddr(request)).thenReturn("127.0.0.1");

            OperateLogAspect aspect = new OperateLogAspect();
            injectServices(aspect, logService, executor);

            aspect.around(joinPoint);

            verify(logService).saveLog(argThat(logDO -> {
                assertThat(logDO.getResponseResult()).isNull();
                return true;
            }));
        }
    }

    // ==================== 操作描述为空时取 @ApiOperation ====================

    @Test
    void around_operateDesc为空_取ApiOperation值() throws Throwable {
        ProceedingJoinPoint joinPoint = mockJoinPoint(WithApiOpService.METHOD);
        given(joinPoint.proceed()).willReturn(null);

        ISystemOperateLogService logService = mock(ISystemOperateLogService.class);
        Executor executor = mockExecutor(logService);

        try (MockedStatic<ServletUtils> servletMock = mockStatic(ServletUtils.class);
             MockedStatic<IpUtils> ipMock = mockStatic(IpUtils.class)) {
            HttpServletRequest request = mock(HttpServletRequest.class);
            when(request.getMethod()).thenReturn("POST");
            when(request.getRequestURI()).thenReturn("/api/apiOp");
            servletMock.when(ServletUtils::getRequest).thenReturn(request);
            ipMock.when(() -> IpUtils.getIpAddr(request)).thenReturn("127.0.0.1");

            OperateLogAspect aspect = new OperateLogAspect();
            injectServices(aspect, logService, executor);

            aspect.around(joinPoint);

            verify(logService).saveLog(argThat(logDO -> {
                assertThat(logDO.getOperateDesc()).isEqualTo("根据ApiOperation取值");
                return true;
            }));
        }
    }

    // ==================== 超长消息截断 ====================

    @Test
    void around_超长响应结果_截断到2000字符() throws Throwable {
        StringBuilder sb = new StringBuilder(2500);
        for (int i = 0; i < 2500; i++) sb.append('x');
        String longStr = sb.toString();
        ProceedingJoinPoint joinPoint = mockJoinPoint(TargetService.METHOD, longStr);
        given(joinPoint.proceed()).willReturn(longStr);

        ISystemOperateLogService logService = mock(ISystemOperateLogService.class);
        Executor executor = mockExecutor(logService);

        try (MockedStatic<ServletUtils> servletMock = mockStatic(ServletUtils.class);
             MockedStatic<IpUtils> ipMock = mockStatic(IpUtils.class)) {
            HttpServletRequest request = mock(HttpServletRequest.class);
            when(request.getMethod()).thenReturn("POST");
            when(request.getRequestURI()).thenReturn("/api/long");
            servletMock.when(ServletUtils::getRequest).thenReturn(request);
            ipMock.when(() -> IpUtils.getIpAddr(request)).thenReturn("127.0.0.1");

            OperateLogAspect aspect = new OperateLogAspect();
            injectServices(aspect, logService, executor);

            aspect.around(joinPoint);

            verify(logService).saveLog(argThat(logDO -> {
                assertThat(logDO.getResponseResult()).hasSize(2000);
                return true;
            }));
        }
    }

    // ==================== 辅助方法 ====================

    @SuppressWarnings("unchecked")
    private Executor mockExecutor(ISystemOperateLogService logService) {
        return command -> {
            try {
                command.run();
            } catch (Exception e) {
                // 日志异常被捕获，不传播
            }
        };
    }

    private void injectServices(OperateLogAspect aspect,
                                ISystemOperateLogService logService,
                                Executor executor) {
        try {
            java.lang.reflect.Field logField = OperateLogAspect.class.getDeclaredField("systemOperateLogService");
            logField.setAccessible(true);
            logField.set(aspect, logService);
            java.lang.reflect.Field execField = OperateLogAspect.class.getDeclaredField("operateLogExecutor");
            execField.setAccessible(true);
            execField.set(aspect, executor);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private ProceedingJoinPoint mockJoinPoint(Method method, Object... args) {
        try {
            MethodSignature signature = mock(MethodSignature.class);
            when(signature.getMethod()).thenReturn(method);
            when(signature.getDeclaringTypeName()).thenReturn(method.getDeclaringClass().getName());
            when(signature.getName()).thenReturn(method.getName());

            Object targetInstance = method.getDeclaringClass().newInstance();
            ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
            when(joinPoint.getSignature()).thenReturn(signature);
            when(joinPoint.getTarget()).thenReturn(targetInstance);
            when(joinPoint.getArgs()).thenReturn(args);
            return joinPoint;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // ==================== 测试用服务类 ====================

    static class TargetService {
        public static final Method METHOD;
        static {
            try { METHOD = TargetService.class.getMethod("testMethod", String.class, String.class); } catch (NoSuchMethodException e) { throw new RuntimeException(e); }
        }
        @OperateLog(module = "testModule", operateType = OperateTypeEnum.ADD, operateDesc = "测试操作",
                saveRequestParam = true, saveResponseResult = true)
        public void testMethod(String arg1, String arg2) {}
    }

    static class NoParamService {
        public static final Method METHOD;
        static {
            try { METHOD = NoParamService.class.getMethod("secretMethod", String.class); } catch (NoSuchMethodException e) { throw new RuntimeException(e); }
        }
        @OperateLog(module = "testModule", operateType = OperateTypeEnum.ADD,
                saveRequestParam = false, saveResponseResult = true)
        public void secretMethod(String arg) {}
    }

    static class NoResultService {
        public static final Method METHOD;
        static {
            try { METHOD = NoResultService.class.getMethod("noResultMethod", String.class); } catch (NoSuchMethodException e) { throw new RuntimeException(e); }
        }
        @OperateLog(module = "testModule", operateType = OperateTypeEnum.ADD,
                saveRequestParam = true, saveResponseResult = false)
        public void noResultMethod(String arg) {}
    }

    static class WithApiOpService {
        public static final Method METHOD;
        static {
            try { METHOD = WithApiOpService.class.getMethod("apiOpMethod"); } catch (NoSuchMethodException e) { throw new RuntimeException(e); }
        }
        @OperateLog(module = "testModule", operateType = OperateTypeEnum.ADD,
                operateDesc = "", saveRequestParam = false, saveResponseResult = false)
        @ApiOperation(value = "根据ApiOperation取值")
        public void apiOpMethod() {}
    }
}
