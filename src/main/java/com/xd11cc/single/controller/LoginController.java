package com.xd11cc.single.controller;

import cloud.tianai.captcha.application.ImageCaptchaApplication;
import cloud.tianai.captcha.application.vo.ImageCaptchaVO;
import cloud.tianai.captcha.common.constant.CaptchaTypeConstant;
import cloud.tianai.captcha.common.response.ApiResponse;
import com.xd11cc.single.annotation.RateLimit;
import com.xd11cc.single.entity.vo.LoginPasswordVO;
import com.xd11cc.single.entity.vo.RouterVO;
import com.xd11cc.single.entity.vo.UserLoginInfoVO;
import com.xd11cc.single.entity.vo.base.ResponseVO;
import com.xd11cc.single.enums.RateLimitEnum;
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

    @Autowired
    private ImageCaptchaApplication imageCaptchaApplication;

    private static final String CAPTCHA_CODE_KEY = "captchaCodeKey:";

    @PostMapping("/loginByPassword")
    @ApiOperation("账号密码登录")
    public ResponseVO<String> loginByPassword(@Valid @RequestBody LoginPasswordVO loginPasswordVO){
        return ResponseVO.success(loginService.loginByPassword(loginPasswordVO));
    }

    @GetMapping("/getUserInfo")
    @ApiOperation("查询当前用户登录信息")
    public ResponseVO<UserLoginInfoVO> getUserLoginInfo(){
        return ResponseVO.success(loginService.getUserLoginInfo(SecurityUtils.getUserId()));
    }

    @GetMapping("/getRoutes")
    @ApiOperation("查询当前用户路由信息")
    public ResponseVO<List<RouterVO>> getRoutes(){
        return ResponseVO.success(systemMenuService.getRoutes(SecurityUtils.getUserId()));
    }

    @GetMapping("/getCaptcha")
    @ApiOperation("获取验证码")
    @RateLimit(key = CAPTCHA_CODE_KEY, count = 10, type = RateLimitEnum.IP)
    public ResponseVO<ImageCaptchaVO> getCaptcha(){
        ApiResponse<ImageCaptchaVO> imageCaptchaVOApiResponse = imageCaptchaApplication.generateCaptcha(CaptchaTypeConstant.SLIDER);
        if (imageCaptchaVOApiResponse.isSuccess()){
            return ResponseVO.success(imageCaptchaVOApiResponse.getData());
        }else {
            return ResponseVO.fail(imageCaptchaVOApiResponse.getMsg());
        }
    }
}
