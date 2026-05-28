package com.xd11cc.single.controller;

import com.xd11cc.single.config.annotation.OperateLog;
import com.xd11cc.single.entity.base.ResponseVO;
import com.xd11cc.single.entity.domain.SystemDeptDO;
import com.xd11cc.single.entity.vo.SystemDeptAddVO;
import com.xd11cc.single.entity.vo.SystemDeptQueryVO;
import com.xd11cc.single.entity.vo.SystemDeptTreeVO;
import com.xd11cc.single.entity.vo.SystemDeptUpdateVO;
import com.xd11cc.single.enums.OperateTypeEnum;
import com.xd11cc.single.service.ISystemDeptService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * @author xd11cc
 * @date 2026-05-26
 */
@Validated
@RestController
@RequestMapping("/system/dept")
@Api(tags = "部门管理")
public class SystemDeptController {

    @Autowired
    private ISystemDeptService systemDeptService;

    @ApiOperation("新增部门")
    @PostMapping("/add")
    @PreAuthorize("@ss.hasPermission('system:dept:add')")
    @OperateLog(module = "部门管理", operateType = OperateTypeEnum.ADD)
    public ResponseVO<Integer> add(@Valid @RequestBody SystemDeptAddVO systemDeptAddVO) {
        int row = systemDeptService.add(systemDeptAddVO);
        if (row > 0) {
            return ResponseVO.success(row, "新增成功");
        } else {
            return ResponseVO.fail("新增失败");
        }
    }

    @ApiOperation("删除部门")
    @GetMapping("/removeById/{id}")
    @PreAuthorize("@ss.hasPermission('system:dept:delete')")
    @OperateLog(module = "部门管理", operateType = OperateTypeEnum.DELETE)
    public ResponseVO<Integer> removeById(@PathVariable("id") Long id) {
        int row = systemDeptService.deleteById(id);
        if (row > 0) {
            return ResponseVO.success(row, "删除成功");
        } else {
            return ResponseVO.fail("删除失败");
        }
    }

    @ApiOperation("修改部门")
    @PostMapping("/modifyById")
    @PreAuthorize("@ss.hasPermission('system:dept:update')")
    @OperateLog(module = "部门管理", operateType = OperateTypeEnum.UPDATE)
    public ResponseVO<Integer> modifyById(@Valid @RequestBody SystemDeptUpdateVO systemDeptUpdateVO) {
        int row = systemDeptService.modifyById(systemDeptUpdateVO);
        if (row > 0) {
            return ResponseVO.success(row, "修改成功");
        } else {
            return ResponseVO.fail("修改失败");
        }
    }

    @ApiOperation("部门树形结构")
    @PostMapping("/treeList")
    public ResponseVO<List<SystemDeptTreeVO>> treeList(@RequestBody SystemDeptQueryVO systemDeptQueryVO) {
        return ResponseVO.success(systemDeptService.getTreeList(systemDeptQueryVO));
    }
}
