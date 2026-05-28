package com.xd11cc.single.controller;

import com.xd11cc.single.entity.base.ResponseVO;
import com.xd11cc.single.entity.domain.SystemLoginLogDO;
import com.xd11cc.single.entity.vo.SystemLoginLogQueryVO;
import com.xd11cc.single.service.ISystemLoginLogService;
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
@RequestMapping("/system/loginLog")
@Api(tags = "登录日志")
@Validated
public class SystemLoginLogController {

    @Autowired
    private ISystemLoginLogService systemLoginLogService;

    @PostMapping("/page")
    @ApiOperation("登录日志分页")
    public ResponseVO<List<SystemLoginLogDO>> page(@RequestBody SystemLoginLogQueryVO queryVO) {
        return PageUtils.page(queryVO, () -> systemLoginLogService.getList(queryVO));
    }

    @GetMapping("/removeByIds/{ids}")
    @ApiOperation("批量删除登录日志")
    @PreAuthorize("@ss.hasPermission('system:loginLog:delete')")
    public ResponseVO<Integer> removeByIds(@PathVariable("ids") List<Long> ids) {
        int row = systemLoginLogService.deleteByIds(ids);
        if (row > 0) {
            return ResponseVO.success(row, "删除成功");
        } else {
            return ResponseVO.fail("删除失败");
        }
    }

    @PostMapping("/clean")
    @ApiOperation("清空登录日志")
    @PreAuthorize("@ss.hasPermission('system:loginLog:clean')")
    public ResponseVO<Void> clean() {
        systemLoginLogService.clean();
        return ResponseVO.success();
    }
}
