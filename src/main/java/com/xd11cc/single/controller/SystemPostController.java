package com.xd11cc.single.controller;

import com.xd11cc.single.config.annotation.OperateLog;
import com.xd11cc.single.entity.base.ResponseVO;
import com.xd11cc.single.entity.domain.SystemPostDO;
import com.xd11cc.single.entity.vo.SystemPostAddVO;
import com.xd11cc.single.entity.vo.SystemPostQueryVO;
import com.xd11cc.single.entity.vo.SystemPostUpdateVO;
import com.xd11cc.single.enums.OperateTypeEnum;
import com.xd11cc.single.service.ISystemPostService;
import com.xd11cc.single.utils.PageUtils;
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
 * @date 2026-05-27
 */
@Validated
@RestController
@RequestMapping("/system/post")
@Api(tags = "岗位管理")
public class SystemPostController {

    @Autowired
    private ISystemPostService systemPostService;

    @ApiOperation("新增岗位")
    @PostMapping("/add")
    @PreAuthorize("@ss.hasPermission('system:post:add')")
    @OperateLog(module = "岗位管理", operateType = OperateTypeEnum.ADD)
    public ResponseVO<Integer> add(@Valid @RequestBody SystemPostAddVO systemPostAddVO) {
        int row = systemPostService.add(systemPostAddVO);
        if (row > 0) {
            return ResponseVO.success(row, "新增成功");
        } else {
            return ResponseVO.fail("新增失败");
        }
    }

    @ApiOperation("删除岗位")
    @GetMapping("/removeByIds/{ids}")
    @PreAuthorize("@ss.hasPermission('system:post:delete')")
    @OperateLog(module = "岗位管理", operateType = OperateTypeEnum.DELETE)
    public ResponseVO<Integer> removeByIds(@PathVariable("ids") List<Long> ids) {
        int row = systemPostService.deleteByIds(ids);
        if (row > 0) {
            return ResponseVO.success(row, "删除成功");
        } else {
            return ResponseVO.fail("删除失败");
        }
    }

    @ApiOperation("修改岗位")
    @PostMapping("/modifyById")
    @PreAuthorize("@ss.hasPermission('system:post:update')")
    @OperateLog(module = "岗位管理", operateType = OperateTypeEnum.UPDATE)
    public ResponseVO<Integer> modifyById(@Valid @RequestBody SystemPostUpdateVO systemPostUpdateVO) {
        int row = systemPostService.modifyById(systemPostUpdateVO);
        if (row > 0) {
            return ResponseVO.success(row, "修改成功");
        } else {
            return ResponseVO.fail("修改失败");
        }
    }

    @ApiOperation("岗位分页")
    @PostMapping("/page")
    public ResponseVO<List<SystemPostDO>> page(@Valid @RequestBody SystemPostQueryVO systemPostQueryVO) {
        return PageUtils.page(systemPostQueryVO, () -> systemPostService.getList(systemPostQueryVO));
    }

    @ApiOperation("查询岗位已分配的部门id列表")
    @GetMapping("/deptIds/{postId}")
    public ResponseVO<List<Long>> getDeptIds(@PathVariable("postId") Long postId) {
        return ResponseVO.success(systemPostService.getDeptIdsByPostId(postId));
    }

    @ApiOperation("根据部门id查询岗位列表")
    @GetMapping("/listByDeptId/{deptId}")
    public ResponseVO<List<SystemPostDO>> listByDeptId(@PathVariable("deptId") Long deptId) {
        return ResponseVO.success(systemPostService.getListByDeptId(deptId));
    }
}
