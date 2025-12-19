package com.xd11cc.single.controller;

import com.xd11cc.single.service.ISystemMenuService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author: xd11cc
 * @Date: 2025/7/9 16:20
 **/
@RestController
@RequestMapping("/menu")
@Api(tags = "菜单管理")
public class SystemMenuController {

    @Autowired
    private ISystemMenuService systemMenuService;

}
