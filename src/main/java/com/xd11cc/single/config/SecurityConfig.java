package com.xd11cc.single.config;

import com.xd11cc.single.config.filter.JwtAuthenticationTokenFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.web.filter.CorsFilter;

/**
 * @Author: xd11cc
 * @Date: 2025/6/16 15:22
 *
 * security配置类
 **/
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private UserDetailsService userDetailsService;
    @Autowired
    private AuthenticationEntryPoint authenticationEntryPoint;
    @Autowired
    private LogoutSuccessHandler logoutSuccessHandler;
    @Autowired
    private JwtAuthenticationTokenFilter jwtAuthenticationTokenFilter;
    @Autowired
    private CorsFilter corsFilter;
    @Autowired
    private SecurityProperties securityProperties;

    @Bean
    public AuthenticationManager authenticationManagerBean() {
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setUserDetailsService(userDetailsService);
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
        return new ProviderManager(daoAuthenticationProvider);
    }

    @Bean
    protected SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                // CSRF禁用，因为不使用session
                .csrf(AbstractHttpConfigurer::disable)
                // 禁用HTTP响应标头
                .headers(headers -> {
                    headers.cacheControl(HeadersConfigurer.CacheControlConfig::disable).frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin);
                })
                // 异常处理类（此处处理认证失败异常）
                .exceptionHandling(exception -> exception.authenticationEntryPoint(authenticationEntryPoint))
                // 给予token，所以不需要session
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 注解标记允许匿名访问的url
                .authorizeHttpRequests(requests -> {
                    requests.antMatchers("/login", "/register").permitAll()
                            // 静态资源
                            .antMatchers("/doc.html",
                                    "/webjars/**",
                                    "/swagger-resources/**",
                                    "/v2/api-docs/**",
                                    "/favicon.ico").permitAll()
                            // 需要匿名的路径
                            .antMatchers(securityProperties.getPermitAllUrls().toArray(new String[0])).permitAll()
                            // 其他的都需要认证
                            .anyRequest().authenticated();
                })
                // 添加Logout Filter
                .logout(logout -> logout.logoutUrl("/logout").logoutSuccessHandler(logoutSuccessHandler))
                // 添加JWT Filter
                .addFilterBefore(jwtAuthenticationTokenFilter, UsernamePasswordAuthenticationFilter.class)
                // 添加CORS Filter
                .addFilterBefore(corsFilter, JwtAuthenticationTokenFilter.class)
                .addFilterBefore(corsFilter, LogoutFilter.class)
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
