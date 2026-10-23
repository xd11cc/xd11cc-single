package com.xd11cc.single.utils;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.assertj.core.api.Assertions.assertThat;

class IpUtilsTest {

    @Test
    void getIpAddr_XForwardedFor单层代理_返回第一层真实IP() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Forwarded-For", "203.0.113.1, 198.51.100.1");
        setRequest(request);

        assertThat(IpUtils.getIpAddr(request)).isEqualTo("203.0.113.1");
    }

    @Test
    void getIpAddr_IPv6本地回环_转为IPv4回环() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("0:0:0:0:0:0:0:1");
        setRequest(request);

        assertThat(IpUtils.getIpAddr(request)).isEqualTo("127.0.0.1");
    }

    @Test
    void getIpAddr_普通IPv4_原样返回() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("192.168.1.100");
        setRequest(request);

        assertThat(IpUtils.getIpAddr(request)).isEqualTo("192.168.1.100");
    }

    @Test
    void getIpAddr_无XForwardedFor_返回remoteAddr() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("10.0.0.1");
        setRequest(request);

        assertThat(IpUtils.getIpAddr(request)).isEqualTo("10.0.0.1");
    }

    private static void setRequest(MockHttpServletRequest request) {
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }
}
