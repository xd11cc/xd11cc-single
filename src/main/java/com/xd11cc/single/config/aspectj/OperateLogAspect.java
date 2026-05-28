package com.xd11cc.single.config.aspectj;

import com.alibaba.fastjson2.JSON;
import com.xd11cc.single.config.annotation.OperateLog;
import com.xd11cc.single.entity.domain.SystemOperateLogDO;
import com.xd11cc.single.enums.OperateStatusEnum;
import com.xd11cc.single.service.ISystemOperateLogService;
import com.xd11cc.single.utils.IpUtils;
import com.xd11cc.single.utils.ServletUtils;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.concurrent.Executor;

/**
 * @author xd11cc
 * @date 2026-05-28
 * @description 异步线程涉及到获取用户上下文要提前获取手动设置
 */
@Aspect
@Component
@Slf4j
public class OperateLogAspect {

    @Autowired
    private ISystemOperateLogService systemOperateLogService;
    @Autowired
    @Qualifier("operateLogExecutor")
    private Executor operateLogExecutor;

    @Pointcut("@annotation(com.xd11cc.single.config.annotation.OperateLog)")
    public void pointCut() {}

    @Around("pointCut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        OperateLog operateLog = method.getAnnotation(OperateLog.class);

        Object result = null;
        Throwable error = null;
        try {
            result = joinPoint.proceed();
            return result;
        } catch (Throwable throwable) {
            error = throwable;
            throw throwable;
        } finally {
            long costTime = System.currentTimeMillis() - startTime;
            recordLog(joinPoint, method, operateLog, result, error, costTime);
        }
    }

    private void recordLog(ProceedingJoinPoint joinPoint, Method method, OperateLog operateLog,
                           Object result, Throwable error, long costTime) {
        try {
            SystemOperateLogDO logDO = new SystemOperateLogDO();
            logDO.setModule(operateLog.module());
            logDO.setOperateType(operateLog.operateType().getCode());

            String operateDesc = operateLog.operateDesc();
            if (operateDesc.isEmpty()) {
                ApiOperation apiOperation = method.getAnnotation(ApiOperation.class);
                if (apiOperation != null) {
                    operateDesc = apiOperation.value();
                }
            }
            logDO.setOperateDesc(operateDesc);

            String className = joinPoint.getTarget().getClass().getSimpleName();
            logDO.setMethod(className + "." + method.getName());

            HttpServletRequest request = ServletUtils.getRequest();
            logDO.setRequestMethod(request.getMethod());
            logDO.setRequestUrl(request.getRequestURI());
            logDO.setOperateIp(IpUtils.getIpAddr(request));

            if (operateLog.saveRequestParam()) {
                String params = JSON.toJSONString(joinPoint.getArgs());
                logDO.setRequestParam(truncate(params, 2000));
            }

            if (error != null) {
                logDO.setStatus(OperateStatusEnum.FAIL.getCode());
                logDO.setErrorMsg(truncate(error.getMessage(), 2000));
            } else {
                logDO.setStatus(OperateStatusEnum.SUCCESS.getCode());
                if (operateLog.saveResponseResult() && result != null) {
                    logDO.setResponseResult(truncate(JSON.toJSONString(result), 2000));
                }
            }

            logDO.setCostTime(costTime);

            operateLogExecutor.execute(() -> {
                try {
                    systemOperateLogService.saveLog(logDO);
                } catch (Exception e) {
                    log.error("保存操作日志失败", e);
                }
            });
        } catch (Exception e) {
            log.error("记录操作日志异常", e);
        }
    }

    private String truncate(String str, int maxLength) {
        if (str == null) {
            return null;
        }
        return str.length() > maxLength ? str.substring(0, maxLength) : str;
    }
}
