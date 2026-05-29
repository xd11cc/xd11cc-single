package com.xd11cc.single.utils;

import cn.hutool.extra.servlet.ServletUtil;

import javax.servlet.http.HttpServletRequest;

/**
 * @author xd11cc
 * @date 2025/6/27 22:42
 */
public class IpUtils {

    public static String getIpAddr() {
        return getIpAddr(ServletUtils.getRequest());
    }

    public static String getIpAddr(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }
        String ip = ServletUtil.getClientIP(request);
        return "0:0:0:0:0:0:0:1".equals(ip) ? "127.0.0.1" : ip;
    }
}
