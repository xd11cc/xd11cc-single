package com.xd11cc.single.controller;

import com.xd11cc.single.config.annotation.OperateLog;
import com.xd11cc.single.entity.base.ResponseVO;
import com.xd11cc.single.entity.domain.SystemConfigDO;
import com.xd11cc.single.entity.vo.SystemConfigAddVO;
import com.xd11cc.single.entity.vo.SystemConfigQueryVO;
import com.xd11cc.single.entity.vo.SystemConfigUpdateVO;
import com.xd11cc.single.enums.OperateTypeEnum;
import com.xd11cc.single.service.ISystemConfigService;
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
 * @date 2026-05-20
 */
@Api(tags = "系统配置")
@Validated
@RestController
@RequestMapping("/system/config")
public class SystemConfigController {

    @Autowired
    private ISystemConfigService systemConfigService;

    @ApiOperation("新增配置")
    @PostMapping("/add")
    @PreAuthorize("@ss.hasPermission('system:config:add')")
    @OperateLog(module = "系统配置", operateType = OperateTypeEnum.ADD)
    public ResponseVO<Integer> add(@Valid @RequestBody SystemConfigAddVO systemConfigAddVO) {
        int row = systemConfigService.add(systemConfigAddVO);
        if (row > 0) {
            return ResponseVO.success(row, "新增成功");
        } else {
            return ResponseVO.fail("新增失败");
        }
    }

    @ApiOperation("批量删除")
    @GetMapping("/removeByIds/{ids}")
    @PreAuthorize("@ss.hasPermission('system:config:delete')")
    @OperateLog(module = "系统配置", operateType = OperateTypeEnum.DELETE)
    public ResponseVO<Integer> removeByIds(@PathVariable("ids") List<Long> ids) {
        int row = systemConfigService.deleteByIds(ids);
        if (row > 0) {
            return ResponseVO.success(row, "删除成功");
        } else {
            return ResponseVO.fail("删除失败");
        }
    }

    @ApiOperation("更新配置")
    @PostMapping("/modifyById")
    @PreAuthorize("@ss.hasPermission('system:config:update')")
    @OperateLog(module = "系统配置", operateType = OperateTypeEnum.UPDATE)
    public ResponseVO<Integer> modifyById(@Valid @RequestBody SystemConfigUpdateVO systemConfigUpdateVO) {
        int row = systemConfigService.modifyById(systemConfigUpdateVO);
        if (row > 0) {
            return ResponseVO.success(row, "更新成功");
        } else {
            return ResponseVO.fail("更新失败");
        }
    }

    @ApiOperation("分页查询配置")
    @PostMapping("/page")
    public ResponseVO<List<SystemConfigDO>> page(@Valid @RequestBody SystemConfigQueryVO systemConfigQueryVO) {
        return PageUtils.page(systemConfigQueryVO, () -> systemConfigService.getList(systemConfigQueryVO));
    }

    @ApiOperation("根据配置键获取配置值")
    @GetMapping("/getConfig/{configKey}")
    public ResponseVO<String> getConfig(@PathVariable("configKey") String configKey) {
        return ResponseVO.success(systemConfigService.getConfig(configKey));
    }
}
