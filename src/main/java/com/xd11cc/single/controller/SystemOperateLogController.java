package com.xd11cc.single.controller;

import com.xd11cc.single.entity.base.ResponseVO;
import com.xd11cc.single.entity.domain.SystemOperateLogDO;
import com.xd11cc.single.entity.vo.SystemOperateLogQueryVO;
import com.xd11cc.single.service.ISystemOperateLogService;
import com.xd11cc.single.utils.PageUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author xd11cc
 * @date 2026-05-28
 */
@RestController
@RequestMapping("/system/operateLog")
@Api(tags = "操作日志")
@Validated
public class SystemOperateLogController {

    @Autowired
    private ISystemOperateLogService systemOperateLogService;

    @PostMapping("/page")
    @ApiOperation("操作日志分页")
    public ResponseVO<List<SystemOperateLogDO>> page(@RequestBody SystemOperateLogQueryVO queryVO) {
        return PageUtils.page(queryVO, () -> systemOperateLogService.getList(queryVO));
    }

    @GetMapping("/removeByIds/{ids}")
    @ApiOperation("批量删除操作日志")
    @PreAuthorize("@ss.hasPermission('system:operateLog:delete')")
    public ResponseVO<Integer> removeByIds(@PathVariable("ids") List<Long> ids) {
        int row = systemOperateLogService.deleteByIds(ids);
        if (row > 0) {
            return ResponseVO.success(row, "删除成功");
        } else {
            return ResponseVO.fail("删除失败");
        }
    }

    @PostMapping("/clean")
    @ApiOperation("清空操作日志")
    @PreAuthorize("@ss.hasPermission('system:operateLog:clean')")
    public ResponseVO<Void> clean() {
        systemOperateLogService.clean();
        return ResponseVO.success();
    }
}
