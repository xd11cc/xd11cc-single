package com.xd11cc.single.config.handler;

import com.alibaba.fastjson2.JSONObject;
import com.xd11cc.single.config.context.TenantContextHolder;
import com.xd11cc.single.constants.SecurityConstants;
import com.xd11cc.single.entity.dto.LoginUserDTO;
import com.xd11cc.single.entity.base.ResponseVO;
import com.xd11cc.single.enums.OperateStatusEnum;
import com.xd11cc.single.enums.LoginTypeEnum;
import com.xd11cc.single.service.ISystemLoginLogService;
import com.xd11cc.single.service.TokenService;
import com.xd11cc.single.utils.ServletUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @Author: xd11cc
 * @Date: 2025/6/20 20:54
 *
 * 退出登录处理
 **/
@Component
public class LogoutSuccessHandlerImpl implements LogoutSuccessHandler {

    @Autowired
    private TokenService tokenService;
    @Autowired
    private ISystemLoginLogService systemLoginLogService;

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        Long tenantId = (Long) request.getAttribute(SecurityConstants.TENANT_ID);
        try {
            TenantContextHolder.setTenantId(tenantId);
            LoginUserDTO loginUser = tokenService.getLoginUser(request);
            if (null != loginUser) {
                systemLoginLogService.recordLoginLog(loginUser.getUsername(), LoginTypeEnum.LOGOUT, OperateStatusEnum.SUCCESS, "退出成功");
                tokenService.removeLoginUser(loginUser);
            }
        } finally {
            TenantContextHolder.clear();
        }
        ServletUtils.renderString(response, JSONObject.toJSONString(ResponseVO.success()));
    }
}
