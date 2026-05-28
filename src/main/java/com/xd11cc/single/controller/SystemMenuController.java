package com.xd11cc.single.controller;

import com.xd11cc.single.entity.base.ResponseVO;
import com.xd11cc.single.entity.vo.SystemMenuAddVO;
import com.xd11cc.single.entity.vo.SystemMenuQueryVO;
import com.xd11cc.single.entity.vo.SystemMenuTreeVO;
import com.xd11cc.single.entity.vo.SystemMenuUpdateVO;
import com.xd11cc.single.service.ISystemMenuService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * @Author: xd11cc
 * @Date: 2025/7/9 16:20
 **/
@Validated
@RestController
@RequestMapping("/menu")
@Api(tags = "菜单管理")
public class SystemMenuController {

    @Autowired
    private ISystemMenuService systemMenuService;

    @ApiOperation("新增菜单")
    @PostMapping("/add")
    @PreAuthorize("@ss.hasPermission('system:menu:add')")
    public ResponseVO<Integer> add(@Valid @RequestBody SystemMenuAddVO systemMenuAddVO){
        int i = systemMenuService.add(systemMenuAddVO);
        return ResponseVO.success(i, "新增成功");
    }

    @ApiOperation("删除菜单")
    @GetMapping("/removeById/{id}")
    @PreAuthorize("@ss.hasPermission('system:menu:delete')")
    public ResponseVO<Integer> removeById(@PathVariable("id") Long id){
        int i = systemMenuService.deleteById(id);
        return ResponseVO.success(i, "删除成功");
    }

    @ApiOperation("修改菜单")
    @PostMapping("/modifyById")
    @PreAuthorize("@ss.hasPermission('system:menu:update')")
    public ResponseVO<Integer> modifyById(@RequestBody SystemMenuUpdateVO systemMenuUpdateVO){
        int i = systemMenuService.modifyById(systemMenuUpdateVO);
        return ResponseVO.success(i, "修改成功");
    }

    @ApiOperation("树形列表")
    @PostMapping("/treeList")
    public ResponseVO<List<SystemMenuTreeVO>> menuTree(@RequestBody SystemMenuQueryVO systemMenuQueryVO){
        return ResponseVO.success(systemMenuService.getTreeList(systemMenuQueryVO));
    }
}
