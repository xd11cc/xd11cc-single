package com.xd11cc.single.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xd11cc.single.entity.domain.SystemDictTypeDO;
import com.xd11cc.single.entity.vo.SystemDictTypeQueryVO;
import com.xd11cc.single.entity.base.ResponseVO;
import com.xd11cc.single.service.ISystemDictTypeService;
import com.xd11cc.single.utils.PageUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author xd11cc
 * @date 2026-01-22 22:28:46
 */
@Api(tags = "字典管理")
@RestController
@RequestMapping("/dict/type")
public class SystemDictTypeController {

    @Autowired
    private ISystemDictTypeService systemDictTypeService;

    @ApiOperation("字典类型分页")
    @PostMapping("/page")
    public ResponseVO<List<SystemDictTypeDO>> page(@RequestBody SystemDictTypeQueryVO systemDictTypeQueryVO){
        PageUtils.startPage(systemDictTypeQueryVO.getPageNo(), systemDictTypeQueryVO.getPageSize());
        LambdaQueryWrapper<SystemDictTypeDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(!StringUtils.isEmpty(systemDictTypeQueryVO.getName()),
                SystemDictTypeDO::getName, systemDictTypeQueryVO.getName());
        wrapper.like(!StringUtils.isEmpty(systemDictTypeQueryVO.getType()),
                SystemDictTypeDO::getType, systemDictTypeQueryVO.getType());
        return PageUtils.getDataTable(systemDictTypeService.list(wrapper));
    }

    @ApiOperation("新增字典类型")
    @PostMapping("/add")
    public ResponseVO<?> add(@RequestBody SystemDictTypeDO systemDictTypeDO){
        systemDictTypeService.save(systemDictTypeDO);
        return ResponseVO.success();
    }

    @ApiOperation("删除字典类型")
    @GetMapping("/removeByIds/{ids}")
    public ResponseVO<?> removeByIds(@PathVariable("ids") List<Long> ids){
        // todo 判断字典类型下是否有数据
        systemDictTypeService.removeByIds(ids);
        return ResponseVO.success();
    }

    @ApiOperation("更新字典类型")
    @PostMapping("/modifyById")
    public ResponseVO<?> modifyById(@RequestBody SystemDictTypeDO systemDictTypeDO){
        systemDictTypeService.updateById(systemDictTypeDO);
        return ResponseVO.success();
    }
}
