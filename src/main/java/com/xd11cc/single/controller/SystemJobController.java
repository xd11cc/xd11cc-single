package com.xd11cc.single.controller;

import com.xd11cc.single.config.annotation.OperateLog;
import com.xd11cc.single.entity.base.PageResult;
import com.xd11cc.single.entity.base.ResponseVO;
import com.xd11cc.single.entity.domain.SystemJobDO;
import com.xd11cc.single.entity.vo.SystemJobAddVO;
import com.xd11cc.single.entity.vo.SystemJobQueryVO;
import com.xd11cc.single.entity.vo.SystemJobUpdateVO;
import com.xd11cc.single.enums.OperateTypeEnum;
import com.xd11cc.single.service.ISystemJobService;
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
 * @date 2026-06-11
 */
@Api(tags = "系统任务管理")
@Validated
@RestController
@RequestMapping("/system/job")
public class SystemJobController {

    @Autowired
    private ISystemJobService systemJobService;

    @ApiOperation("新增任务")
    @PostMapping("/add")
    @PreAuthorize("@ss.hasPermission('system:job:add')")
    @OperateLog(module = "系统任务", operateType = OperateTypeEnum.ADD)
    public ResponseVO<Integer> add(@Valid @RequestBody SystemJobAddVO addVO) {
        int row = systemJobService.add(addVO);
        if (row > 0) {
            return ResponseVO.success(row, "新增成功");
        } else {
            return ResponseVO.fail("新增失败");
        }
    }

    @ApiOperation("批量删除")
    @GetMapping("/removeByIds/{ids}")
    @PreAuthorize("@ss.hasPermission('system:job:delete')")
    @OperateLog(module = "系统任务", operateType = OperateTypeEnum.DELETE)
    public ResponseVO<Integer> removeByIds(@PathVariable("ids") List<Long> ids) {
        int row = systemJobService.deleteByIds(ids);
        if (row > 0) {
            return ResponseVO.success(row, "删除成功");
        } else {
            return ResponseVO.fail("删除失败");
        }
    }

    @ApiOperation("更新任务")
    @PostMapping("/modifyById")
    @PreAuthorize("@ss.hasPermission('system:job:update')")
    @OperateLog(module = "系统任务", operateType = OperateTypeEnum.UPDATE)
    public ResponseVO<Integer> modifyById(@Valid @RequestBody SystemJobUpdateVO updateVO) {
        int row = systemJobService.modifyById(updateVO);
        if (row > 0) {
            return ResponseVO.success(row, "更新成功");
        } else {
            return ResponseVO.fail("更新失败");
        }
    }

    @ApiOperation("分页查询任务")
    @PostMapping("/page")
    public ResponseVO<PageResult<SystemJobDO>> page(@Valid @RequestBody SystemJobQueryVO queryVO) {
        return PageUtils.page(queryVO, () -> systemJobService.getList(queryVO));
    }

    @ApiOperation("修改任务状态")
    @GetMapping("/changeStatus/{id}/{status}")
    @PreAuthorize("@ss.hasPermission('system:job:update')")
    @OperateLog(module = "系统任务", operateType = OperateTypeEnum.UPDATE)
    public ResponseVO<Integer> changeStatus(@PathVariable("id") Long id, @PathVariable("status") String status) {
        int row = systemJobService.changeStatus(id, status);
        if (row > 0) {
            return ResponseVO.success(row, "操作成功");
        } else {
            return ResponseVO.fail("操作失败");
        }
    }

    @ApiOperation("执行一次任务")
    @GetMapping("/runOnce/{id}")
    @PreAuthorize("@ss.hasPermission('system:job:update')")
    @OperateLog(module = "系统任务", operateType = OperateTypeEnum.OTHER)
    public ResponseVO<Void> runOnce(@PathVariable("id") Long id) {
        systemJobService.runOnce(id);
        return ResponseVO.success();
    }
}
