package com.xd11cc.single.controller;

import com.xd11cc.single.config.RedisCache;
import com.xd11cc.single.constants.CacheConstants;
import com.xd11cc.single.entity.vo.*;
import com.xd11cc.single.entity.base.ResponseVO;
import com.xd11cc.single.service.ISystemMenuService;
import com.xd11cc.single.service.LoginService;
import com.xd11cc.single.utils.SecurityUtils;
import com.xd11cc.single.utils.TenantUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import me.zhyd.oauth.config.AuthConfig;
import me.zhyd.oauth.model.AuthCallback;
import me.zhyd.oauth.model.AuthResponse;
import me.zhyd.oauth.model.AuthUser;
import me.zhyd.oauth.request.AuthGithubRequest;
import me.zhyd.oauth.request.AuthRequest;
import me.zhyd.oauth.utils.AuthStateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.List;

/**
 * @Author: xd11cc
 * @Date: 2025/6/14 00:18
 **/
@Slf4j
@RestController
@RequestMapping("/login")
@Api(tags = "登录管理")
@Validated
public class LoginController {

    @Autowired
    private LoginService loginService;
    @Autowired
    private ISystemMenuService systemMenuService;
    @Autowired
    private RedisCache redisCache;

    private String getAuthStateKey(String key) {
        return CacheConstants.AUTH_STATE_KEY + key;
    }

    @PostMapping("/loginByPassword")
    @ApiOperation("账号密码登录")
    public ResponseVO<String> loginByPassword(@Valid @RequestBody LoginPasswordVO loginPasswordVO){
        return ResponseVO.success(loginService.loginByPassword(loginPasswordVO));
    }

    @GetMapping("/getCaptcha")
    @ApiOperation("获取验证码")
    public ResponseVO<CaptchaVO> captcha(){
        return ResponseVO.success(loginService.getCaptcha());
    }

    @GetMapping("/getUserInfo")
    @ApiOperation("查询当前用户登录信息")
    public ResponseVO<UserLoginInfoVO> getUserLoginInfo(){
        return ResponseVO.success(loginService.getUserLoginInfo(SecurityUtils.getUserId()));
    }

    @GetMapping("/getRoutes")
    @ApiOperation("查询当前用户路由信息")
    public ResponseVO<List<RouteVO>> getRoutes(){
        return ResponseVO.success(systemMenuService.getRoutes(SecurityUtils.getUserId()));
    }

    @GetMapping("/authorize/{source}")
    @ApiOperation("社交授权认证")
    public ResponseVO<String> render(HttpServletRequest request, HttpServletResponse response,
                       @PathVariable("source") String source) throws IOException {
        return ResponseVO.success(loginService.getRedirectUri(source));
    }


    /**
     * @param source 认证来源
     * @param tenantId 授权租户
     * @param code
     * @return
     */
    @GetMapping("/callback/{source}/{tenantId}")
    @ApiOperation("社交授权认证回调")
    public void oauth(@PathVariable("source") String source,
                                          @PathVariable("tenantId") Long tenantId,
                                          @RequestParam("code") String code,
                                          @RequestParam("state") String state,
                                          HttpServletResponse response) {
        TenantUtils.execute(tenantId, () ->loginService.callback(source, code, state, response));
    }

    @PostMapping("/social-user/info/{state}")
    @ApiOperation("社交用户信息")
    public ResponseVO<AuthUser> socialUserInfo(@PathVariable("state") String state){
        return ResponseVO.success(redisCache.getCacheObject(getAuthStateKey(state)));
    }

    @PostMapping("/social-user/bind")
    @ApiOperation("社交绑定")
    public ResponseVO<String> socialUserBind(@Valid @RequestBody SocialUserBindVO socialUserBindVO){
        return ResponseVO.success(loginService.socialUserBind(socialUserBindVO));
    }
}
