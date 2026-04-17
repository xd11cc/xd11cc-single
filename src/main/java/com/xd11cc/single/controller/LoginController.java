package com.xd11cc.single.controller;

import com.xd11cc.single.entity.vo.CaptchaVO;
import com.xd11cc.single.entity.vo.LoginPasswordVO;
import com.xd11cc.single.entity.vo.RouteVO;
import com.xd11cc.single.entity.vo.UserLoginInfoVO;
import com.xd11cc.single.entity.base.ResponseVO;
import com.xd11cc.single.service.ISystemMenuService;
import com.xd11cc.single.service.LoginService;
import com.xd11cc.single.utils.SecurityUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
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
}
