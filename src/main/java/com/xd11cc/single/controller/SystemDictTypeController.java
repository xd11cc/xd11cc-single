package com.xd11cc.single.controller;

import com.xd11cc.single.config.annotation.OperateLog;
import com.xd11cc.single.convert.SystemDictTypeConvert;
import com.xd11cc.single.entity.base.ResponseVO;
import com.xd11cc.single.entity.domain.SystemDictTypeDO;
import com.xd11cc.single.entity.vo.SystemDictTypeAddVO;
import com.xd11cc.single.entity.vo.SystemDictTypeQueryVO;
import com.xd11cc.single.entity.vo.SystemDictTypeUpdateVO;
import com.xd11cc.single.enums.OperateTypeEnum;
import com.xd11cc.single.service.ISystemDictTypeService;
import com.xd11cc.single.utils.PageUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
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

    @ApiOperation("新增字典类型")
    @PostMapping("/add")
    @PreAuthorize("@ss.hasPermission('system:dictType:add')")
    @OperateLog(module = "字典类型", operateType = OperateTypeEnum.ADD)
    public ResponseVO<Integer> add(@Valid @RequestBody SystemDictTypeAddVO systemDictTypeAddVO){
        int row = systemDictTypeService.add(systemDictTypeAddVO);
        if (row > 0){
            return ResponseVO.success(row, "新增成功");
        }else {
            return ResponseVO.fail("新增失败");
        }
    }

    @ApiOperation("删除字典类型")
    @GetMapping("/removeByIds/{ids}")
    @PreAuthorize("@ss.hasPermission('system:dictType:delete')")
    @OperateLog(module = "字典类型", operateType = OperateTypeEnum.DELETE)
    public ResponseVO<Integer> removeByIds(@PathVariable("ids") List<Long> ids){
        int row = systemDictTypeService.deleteByIds(ids);
        if (row > 0){
            return ResponseVO.success(row, "删除成功");
        }else {
            return ResponseVO.fail("删除失败");
        }
    }

    @ApiOperation("更新字典类型")
    @PostMapping("/modifyById")
    @PreAuthorize("@ss.hasPermission('system:dictType:update')")
    @OperateLog(module = "字典类型", operateType = OperateTypeEnum.UPDATE)
    public ResponseVO<Boolean> modifyById(@Valid @RequestBody SystemDictTypeUpdateVO systemDictTypeUpdateVO){
        SystemDictTypeDO systemDictTypeDO = SystemDictTypeConvert.INSTANCE.updateVO2DO(systemDictTypeUpdateVO);
        boolean b = systemDictTypeService.updateById(systemDictTypeDO);
        if (b){
            return ResponseVO.success(b, "修改成功");
        }else {
            return ResponseVO.fail("修改失败");
        }
    }

    @ApiOperation("字典类型分页")
    @PostMapping("/page")
    public ResponseVO<List<SystemDictTypeDO>> page(@RequestBody SystemDictTypeQueryVO systemDictTypeQueryVO){
        return PageUtils.page(systemDictTypeQueryVO, ()-> systemDictTypeService.getList(systemDictTypeQueryVO));
    }

    @ApiOperation("字典类型列表")
    @GetMapping("/list")
    public ResponseVO<List<SystemDictTypeDO>> list(){
        return ResponseVO.success(systemDictTypeService.list());
    }

    @ApiModelProperty("导出")
    @PostMapping("/export")
    @PreAuthorize("@ss.hasPermission('system:dictType:export')")
    @OperateLog(module = "字典类型", operateType = OperateTypeEnum.EXPORT)
    public void export(@RequestBody SystemDictTypeQueryVO systemDictTypeQueryVO){
        List<SystemDictTypeDO> list = systemDictTypeService.getList(systemDictTypeQueryVO);
    }
}
