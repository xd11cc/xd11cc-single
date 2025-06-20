package com.xd11cc.single.controller;

import com.xd11cc.single.entity.vo.LoginPasswordVO;
import com.xd11cc.single.entity.vo.LoginResultVO;
import com.xd11cc.single.entity.vo.base.ResponseVO;
import com.xd11cc.single.service.ISystemUserService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author: xd11cc
 * @Date: 2025/6/14 00:18
 **/
@RestController
@RequestMapping("/user")
@Api("用户管理")
public class SystemUserController {

    @Autowired
    private ISystemUserService systemUserService;

    @PostMapping("/loginByPassword")
    public ResponseVO<LoginResultVO> loginByPassword(@RequestBody LoginPasswordVO loginPasswordVO){
        return ResponseVO.success(systemUserService.loginByPassword(loginPasswordVO));
    }
}
