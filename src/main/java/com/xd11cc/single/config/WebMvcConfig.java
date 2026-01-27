package com.xd11cc.single.config;

import com.xd11cc.single.config.interceptor.HeaderInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author xd11cc
 * @date 2026-01-27 14:07:16
 * @description 拦截器配置
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private HeaderInterceptor headerInterceptor;

    // 不需要拦截的地址
//    public static final String[] excludeUrls = {""};

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
//        registry.addInterceptor(headerInterceptor).addPathPatterns("/**").excludePathPatterns(excludeUrls).order(-10);
        registry.addInterceptor(headerInterceptor).addPathPatterns("/**").order(-10);
    }
}
