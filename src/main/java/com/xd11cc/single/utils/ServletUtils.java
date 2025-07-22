package com.xd11cc.single.utils;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @Author: xd11cc
 * @Date: 2025/6/27 23:05
 **/
public class ServletUtils {

    /**
     * 获取request
     * @return
     */
    public static HttpServletRequest getRequest() {
        return getRequestAttributes().getRequest();
    }

    private static ServletRequestAttributes getRequestAttributes() {
        return (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
    }

    /**
     * 将字符串渲染到客户端
     * @param response
     * @param content
     * @return
     */
    public static String renderString(HttpServletResponse response, String content) {
        try {
            response.setContentType("application/json; charset=utf-8");
            response.setCharacterEncoding("utf-8");
            response.getWriter().println(content);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
