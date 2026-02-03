package com.xd11cc.single.config.filter;

import com.alibaba.fastjson2.JSONObject;
import com.xd11cc.single.config.RedisCache;
import com.xd11cc.single.config.properties.TenantIgnoreProperties;
import com.xd11cc.single.constants.CacheConstants;
import com.xd11cc.single.constants.SecurityConstants;
import com.xd11cc.single.entity.base.ResponseVO;
import com.xd11cc.single.entity.dto.TenantDTO;
import com.xd11cc.single.enums.SystemErrorEnum;
import com.xd11cc.single.enums.SystemStatusEnum;
import com.xd11cc.single.utils.ServletUtils;
import com.xd11cc.single.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;

/**
 * @author xd11cc
 * @date 2026-01-26 11:01:25
 * @description 租户过滤器
 */
@Slf4j
@Order(value = -201)
@Component
public class TenantFilter extends OncePerRequestFilter {

    @Autowired
    private RedisCache redisCache;

    @Autowired
    private TenantIgnoreProperties tenantIgnoreProperties;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String requestURI = request.getRequestURI();
        // 跳过不需要验证的路径
        if (StringUtils.matches(requestURI, tenantIgnoreProperties.getIgnoreUrls())){
            filterChain.doFilter(request, response);
            return;
        }

        String domain = request.getServerName();
        // 根据域名查询租户ID
        TenantDTO tenantDTO = redisCache.getCacheMapValue(CacheConstants.TENANT_DOMAIN_KEY, domain, false);
        if (tenantDTO == null){
            log.warn("域名：{}，查不到租户ID", domain);
            ServletUtils.renderString(response, JSONObject.toJSONString(ResponseVO.fail(SystemErrorEnum.CHOOSE_RIGHT_DOMAIN)));
            return;
        }
        // 验证是否禁用
        if (SystemStatusEnum.FORBIDDEN.getCode().equals(tenantDTO.getStatus())){
            log.warn("租户已禁用：{}", tenantDTO);
            ServletUtils.renderString(response, JSONObject.toJSONString(ResponseVO.fail(SystemErrorEnum.SYSTEM_ERROR)));
            return;
        }
        if (new Date().getTime() > tenantDTO.getExpireTime().getTime()){
            log.warn("租户已过期：{}", tenantDTO);
            ServletUtils.renderString(response, JSONObject.toJSONString(ResponseVO.fail(SystemErrorEnum.SYSTEM_ERROR)));
            return;
        }

        request.setAttribute(SecurityConstants.TENANT_ID, tenantDTO.getId());
        filterChain.doFilter(request, response);
    }
}
