package com.xd11cc.single.controller;

import com.xd11cc.single.entity.base.ResponseVO;
import com.xd11cc.single.entity.domain.SystemDictTypeDO;
import com.xd11cc.single.entity.vo.SystemDictTypeAddVO;
import com.xd11cc.single.entity.vo.SystemDictTypeQueryVO;
import com.xd11cc.single.service.ISystemDictTypeService;
import com.xd11cc.single.utils.PageUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * @author xd11cc
 * @date 2026-01-22 22:28:46
 */
@Api(tags = "字典管理")
@Validated
@RestController
@RequestMapping("/dict/type")
public class SystemDictTypeController {

    @Autowired
    private ISystemDictTypeService systemDictTypeService;

    @ApiOperation("字典类型分页")
    @PostMapping("/page")
    public ResponseVO<List<SystemDictTypeDO>> page(@RequestBody SystemDictTypeQueryVO systemDictTypeQueryVO){
        return PageUtils.page(systemDictTypeQueryVO, ()-> systemDictTypeService.getList(systemDictTypeQueryVO));
    }

    @ApiOperation("新增字典类型")
    @PostMapping("/add")
    public ResponseVO<?> add(@Valid @RequestBody SystemDictTypeAddVO systemDictTypeAddVO){
        systemDictTypeService.add(systemDictTypeAddVO);
        return ResponseVO.success();
    }

    @ApiOperation("删除字典类型")
    @GetMapping("/removeByIds/{ids}")
    public ResponseVO<?> removeByIds(@PathVariable("ids") List<Long> ids){
        systemDictTypeService.deleteByIds(ids);
        return ResponseVO.success();
    }

    @ApiOperation("更新字典类型")
    @PostMapping("/modifyById")
    public ResponseVO<?> modifyById(@Valid @RequestBody SystemDictTypeDO systemDictTypeDO){
        systemDictTypeService.updateById(systemDictTypeDO);
        return ResponseVO.success();
    }
}
