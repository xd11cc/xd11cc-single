package com.xd11cc.single.controller;

import com.xd11cc.single.config.annotation.OperateLog;
import com.xd11cc.single.entity.base.PageResult;
import com.xd11cc.single.entity.base.ResponseVO;
import com.xd11cc.single.entity.domain.SystemJobLogDO;
import com.xd11cc.single.entity.vo.SystemJobLogQueryVO;
import com.xd11cc.single.enums.OperateTypeEnum;
import com.xd11cc.single.service.ISystemJobLogService;
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
@Api(tags = "系统任务日志")
@Validated
@RestController
@RequestMapping("/system/jobLog")
public class SystemJobLogController {

    @Autowired
    private ISystemJobLogService systemJobLogService;

    @ApiOperation("分页查询任务日志")
    @PostMapping("/page")
    public ResponseVO<PageResult<SystemJobLogDO>> page(@Valid @RequestBody SystemJobLogQueryVO queryVO) {
        return PageUtils.page(queryVO, () -> systemJobLogService.getList(queryVO));
    }

    @ApiOperation("批量删除日志")
    @GetMapping("/removeByIds/{ids}")
    @PreAuthorize("@ss.hasPermission('system:job:delete')")
    @OperateLog(module = "系统任务日志", operateType = OperateTypeEnum.DELETE)
    public ResponseVO<Integer> removeByIds(@PathVariable("ids") List<Long> ids) {
        int row = systemJobLogService.deleteByIds(ids);
        if (row > 0) {
            return ResponseVO.success(row, "删除成功");
        } else {
            return ResponseVO.fail("删除失败");
        }
    }
}
