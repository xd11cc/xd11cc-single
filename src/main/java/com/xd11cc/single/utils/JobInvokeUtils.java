package com.xd11cc.single.utils;

import cn.hutool.extra.spring.SpringUtil;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * @author xd11cc
 * @date 2026-06-11
 */
public class JobInvokeUtils {

    public static void invokeMethod(String invokeTarget) throws Exception {
        String beanName = getBeanName(invokeTarget);
        String methodName = getMethodName(invokeTarget);
        List<Object[]> methodParams = getMethodParams(invokeTarget);

        Object bean = SpringUtil.getBean(beanName);
        invokeMethod(bean, methodName, methodParams);
    }

    private static void invokeMethod(Object bean, String methodName, List<Object[]> methodParams)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        if (methodParams.isEmpty()) {
            Method method = bean.getClass().getDeclaredMethod(methodName);
            method.invoke(bean);
        } else {
            Method method = bean.getClass().getDeclaredMethod(methodName, getMethodParamsType(methodParams));
            method.invoke(bean, getMethodParamsValue(methodParams));
        }
    }

    private static Class<?>[] getMethodParamsType(List<Object[]> methodParams) {
        Class<?>[] types = new Class[methodParams.size()];
        for (int i = 0; i < methodParams.size(); i++) {
            types[i] = (Class<?>) methodParams.get(i)[0];
        }
        return types;
    }

    private static Object[] getMethodParamsValue(List<Object[]> methodParams) {
        Object[] values = new Object[methodParams.size()];
        for (int i = 0; i < methodParams.size(); i++) {
            values[i] = methodParams.get(i)[1];
        }
        return values;
    }

    private static String getBeanName(String invokeTarget) {
        String beanName = StringUtils.substringBefore(invokeTarget, "(");
        return StringUtils.substringBeforeLast(beanName, ".");
    }

    private static String getMethodName(String invokeTarget) {
        String methodName = StringUtils.substringBefore(invokeTarget, "(");
        return StringUtils.substringAfterLast(methodName, ".");
    }

    private static List<Object[]> getMethodParams(String invokeTarget) {
        String methodStr = StringUtils.substringBetween(invokeTarget, "(", ")");
        if (StringUtils.isEmpty(methodStr)) {
            return new ArrayList<>();
        }
        String[] methodParams = methodStr.split(",(?=(?:[^']*'[^']*')*[^']*$)");
        List<Object[]> paramList = new ArrayList<>();
        for (String param : methodParams) {
            String str = StringUtils.trim(param);
            if (StringUtils.contains(str, "'")) {
                paramList.add(new Object[]{String.class, StringUtils.replace(str, "'", "")});
            } else if ("true".equalsIgnoreCase(str) || "false".equalsIgnoreCase(str)) {
                paramList.add(new Object[]{Boolean.class, Boolean.valueOf(str)});
            } else if (StringUtils.contains(str, ".")) {
                paramList.add(new Object[]{Double.class, Double.valueOf(str)});
            } else if (StringUtils.endsWithIgnoreCase(str, "L")) {
                paramList.add(new Object[]{Long.class, Long.valueOf(StringUtils.removeEnd(str, "L"))});
            } else {
                paramList.add(new Object[]{Long.class, Long.valueOf(str)});
            }
        }
        return paramList;
    }
}
