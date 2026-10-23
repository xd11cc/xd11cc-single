package com.xd11cc.single.modules.workflow;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Flowable 相关的 Web MVC 配置。
 * <p>
 * 从 {@link FlowableConfig} 中拆出，使其只负责「引擎构建」这一件事，
 * Web 层关注点（拦截器注册）在此单独声明。
 * </p>
 *
 * @author xd11cc
 */
@Configuration
public class FlowableWebMvcConfig implements WebMvcConfigurer {

    /**
     * Flowable 身份上下文拦截路径。
     * <p>
     * 只在工作流接口上写入 Flowable 的 authenticated user，
     * 避免登录、字典、静态资源等全站请求产生无谓的 ThreadLocal 操作。
     * </p>
     */
    private static final String WORKFLOW_PATH_PATTERN = "/workflow/**";

    @Autowired
    private FlowableSecurityInterceptor flowableSecurityInterceptor;

    /**
     * 注册工作流身份拦截器。
     * <ul>
     *     <li>使用容器注入的 bean，不再 {@code new} 或传 {@code null}</li>
     *     <li>order=-10 与 HeaderInterceptor 一致，保证租户/用户上下文最先设置</li>
     * </ul>
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(flowableSecurityInterceptor)
                .addPathPatterns(WORKFLOW_PATH_PATTERN)
                .order(-10);
    }
}
