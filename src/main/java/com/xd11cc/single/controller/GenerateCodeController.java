package com.xd11cc.single.controller;

import com.xd11cc.single.entity.base.PageResult;
import com.xd11cc.single.entity.base.ResponseVO;
import com.xd11cc.single.entity.vo.PreviewCodeVO;
import com.xd11cc.single.entity.vo.TableInfoQueryVO;
import com.xd11cc.single.entity.vo.TableInfoVO;
import com.xd11cc.single.service.GenerateCodeService;
import com.xd11cc.single.utils.PageUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author xd11cc
 * @date 2026-04-22 16:22:38
 * @description
 */
@Api(tags = "代码生成管理")
@RestController
@RequestMapping("/generate/code")
public class GenerateCodeController {

    @Autowired
    private GenerateCodeService generateCodeService;

    @ApiOperation("表信息列表")
    @PostMapping("/page")
    public ResponseVO<PageResult<TableInfoVO>> page(@RequestBody TableInfoQueryVO tableInfoQueryVO){
        return PageUtils.page(tableInfoQueryVO, ()->generateCodeService.list(tableInfoQueryVO));
    }

    @ApiOperation("生成代码")
    @GetMapping
    @PreAuthorize("ss.hasPermission('system:generate:code')")
    public ResponseVO<List<PreviewCodeVO>> generateCode(String tableName){
        return ResponseVO.success(generateCodeService.generateCode(tableName));
    }

}
