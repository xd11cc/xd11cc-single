package com.xd11cc.single.controller;

import com.xd11cc.single.entity.base.ResponseVO;
import com.xd11cc.single.entity.domain.SystemDictDataDO;
import com.xd11cc.single.entity.vo.SystemDictDataAddVO;
import com.xd11cc.single.entity.vo.SystemDictDataQueryVO;
import com.xd11cc.single.entity.vo.SystemDictDataUpdateVO;
import com.xd11cc.single.service.ISystemDictDataService;
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
 * @date 2026-01-28 15:26:11
 * @description
 */
@Api(tags = "字典数据管理")
@Validated
@RestController
@RequestMapping("/dict/data")
public class SystemDictDataController {

    @Autowired
    private ISystemDictDataService systemDictDataService;

    @ApiOperation("新增字典数据")
    @PostMapping("/add")
    public ResponseVO<Integer> add(@Valid @RequestBody SystemDictDataAddVO systemDictDataAddVO){
        int row = systemDictDataService.add(systemDictDataAddVO);
        if (row > 0){
            return ResponseVO.success(row, "新增成功");
        }else {
            return ResponseVO.fail("新增失败");
        }
    }

    @ApiOperation("批量删除")
    @GetMapping("/removeByIds/{ids}")
    public ResponseVO<Integer> removeByIds(@PathVariable("ids") List<Long> ids){
        int row = systemDictDataService.deleteByIds(ids);
        if (row > 0){
            return ResponseVO.success(row, "删除成功");
        }else {
            return ResponseVO.fail("删除失败");
        }
    }

    @ApiOperation("更新字典数据")
    @PostMapping("/modifyById")
    public ResponseVO<Integer> modifyById(@Valid @RequestBody SystemDictDataUpdateVO systemDictDataUpdateVO){
        int i = systemDictDataService.modifyById(systemDictDataUpdateVO);
        if (i > 0){
            return ResponseVO.success(i, "更新成功");
        }else {
            return ResponseVO.fail("更新失败");
        }
    }

    @ApiOperation("查询字典数据")
    @PostMapping("/page")
    public ResponseVO<SystemDictDataDO> page(@Valid @RequestBody SystemDictDataQueryVO systemDictDataQueryVO){
        return PageUtils.page(systemDictDataQueryVO, () ->systemDictDataService.getList(systemDictDataQueryVO));
    }
}
