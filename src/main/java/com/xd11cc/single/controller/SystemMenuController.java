package com.xd11cc.single.controller;

import com.xd11cc.single.entity.vo.TreeMenuVO;
import com.xd11cc.single.entity.vo.base.ResponseVO;
import com.xd11cc.single.service.ISystemMenuService;
import com.xd11cc.single.utils.SecurityUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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

    @GetMapping("/getTreeMenu")
    @ApiOperation("获取当前登录用户的菜单")
    public ResponseVO<List<TreeMenuVO>> getTreeMenu(){
        return ResponseVO.success(systemMenuService.getTreeMenu(SecurityUtils.getUserId()));
    }
}
