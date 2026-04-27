package com.xd11cc.single.controller;

import com.xd11cc.single.entity.base.ResponseVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Author: xd11cc
 * @Date: 2025/7/9 16:38
 **/
@RestController
@RequestMapping("/user")
@Api(tags = "用户管理")
public class SystemUserController {

    @PostMapping("/add")
    @ApiOperation("新增用户")
    public ResponseVO<Integer> add(){
        return ResponseVO.success();
    }

    @GetMapping("/removeByIds/{ids}")
    @ApiOperation("删除用户")
    public ResponseVO<Integer> removeByIds(@PathVariable("ids") List<Long> ids){
        return ResponseVO.success();
    }

    @PostMapping("/modifyById")
    @ApiOperation("修改用户")
    public ResponseVO<Integer> modifyById(){
        return ResponseVO.success();
    }

    @PostMapping("/page")
    @ApiOperation("用户分页")
    public ResponseVO<?> page(){
        return ResponseVO.success();
    }
}
