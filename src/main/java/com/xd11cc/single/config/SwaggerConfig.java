package com.xd11cc.single.config;

import io.swagger.annotations.ApiOperation;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.*;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @Author: xd11cc
 * @Date: 2025/6/16 22:36
 *
 * swagger配置类
 **/
@Configuration
@EnableSwagger2
public class SwaggerConfig {

    /**
     * 创建api
     * @return
     */
    @Bean
    public Docket createRestApi(SwaggerProperties swaggerProperties) {
        return new Docket(DocumentationType.SWAGGER_2)
                // 用来创建该API的基本信息，展示在文档的页面中（自定义展示信息）
                .apiInfo(apiInfo(swaggerProperties))
                // 设置哪些接口暴露给swagger展示
                .select()
                // 扫描所有有注解的api
                .apis(RequestHandlerSelectors.withMethodAnnotation(ApiOperation.class))
                .paths(PathSelectors.any())
                .build()
                // 设置安全模式，swagger可以设置访问token
                .securitySchemes(securitySchemes())
                .securityContexts(securityContexts());
    }

    /**
     * 安全上下文
     * @return
     */
    private List<SecurityContext> securityContexts() {
        List<SecurityContext> securityContexts = new ArrayList<>();
        securityContexts.add(
                SecurityContext.builder()
                        .securityReferences(defaultAuth())
                        .forPaths(PathSelectors.regex("^(?!auth).*$"))
                        .build()
        );
        return securityContexts;
    }

    /**
     * 默认的安全上引用
     * @return
     */
    private List<SecurityReference> defaultAuth() {
        AuthorizationScope authorizationScope = new AuthorizationScope("global", "accessEverything");
        AuthorizationScope[] authorizationScopes = new AuthorizationScope[1];
        authorizationScopes[0] = authorizationScope;
        List<SecurityReference> securityReferences = new ArrayList<>();
        securityReferences.add(new SecurityReference("Authorization", authorizationScopes));
        return securityReferences;
    }

    /**
     * 安全模式，制定token通过Authorization头请求头传递
     * @return
     */
    private List<SecurityScheme> securitySchemes() {
        return Collections.singletonList(new ApiKey("Authorization", "Authorization", "header"));
    }

    /**
     * 添加摘要信息
     * @param swaggerProperties
     * @return
     */
    private ApiInfo apiInfo(SwaggerProperties swaggerProperties) {
        return new ApiInfoBuilder()
                .title(swaggerProperties.getTitle())
                .description(swaggerProperties.getDescription())
                .version(swaggerProperties.getVersion())
                .build();
    }
}
