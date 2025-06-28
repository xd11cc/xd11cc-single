package com.xd11cc.single.controller;

import com.xd11cc.single.annotation.RateLimit;
import com.xd11cc.single.entity.vo.LoginPasswordVO;
import com.xd11cc.single.entity.vo.LoginResultVO;
import com.xd11cc.single.entity.vo.base.ResponseVO;
import com.xd11cc.single.service.LoginService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @Author: xd11cc
 * @Date: 2025/6/14 00:18
 **/
@Slf4j
@RestController
@RequestMapping("/user")
@Api(tags = "登录管理")
public class LoginController {

    @Autowired
    private LoginService loginService;

    @PostMapping("/loginByPassword")
    @ApiOperation("账号密码登录")
    public ResponseVO<LoginResultVO> loginByPassword(@RequestBody LoginPasswordVO loginPasswordVO){
        return ResponseVO.success(loginService.loginByPassword(loginPasswordVO));
    }

    @GetMapping("/test")
    @ApiOperation("测试接口")
    @RateLimit
    public ResponseVO<LoginResultVO> test(){
        log.info("test");
        return ResponseVO.success();
    }
}
