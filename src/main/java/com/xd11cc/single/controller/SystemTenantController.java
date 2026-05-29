package com.xd11cc.single.controller;

import com.xd11cc.single.config.annotation.OperateLog;
import com.xd11cc.single.entity.base.PageResult;
import com.xd11cc.single.entity.base.ResponseVO;
import com.xd11cc.single.entity.domain.SystemTenantDO;
import com.xd11cc.single.entity.vo.SystemTenantAddVO;
import com.xd11cc.single.entity.vo.SystemTenantQueryVO;
import com.xd11cc.single.entity.vo.SystemTenantUpdateVO;
import com.xd11cc.single.enums.OperateTypeEnum;
import com.xd11cc.single.service.ISystemTenantService;
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
 * @date 2026-05-28
 */
@Api(tags = "租户管理")
@Validated
@RestController
@RequestMapping("/system/tenant")
public class SystemTenantController {

    @Autowired
    private ISystemTenantService systemTenantService;

    @ApiOperation("新增租户")
    @PostMapping("/add")
    @PreAuthorize("@ss.hasPermission('system:tenant:add')")
    @OperateLog(module = "租户管理", operateType = OperateTypeEnum.ADD)
    public ResponseVO<Integer> add(@Valid @RequestBody SystemTenantAddVO vo) {
        int row = systemTenantService.add(vo);
        if (row > 0) {
            return ResponseVO.success(row, "新增成功");
        } else {
            return ResponseVO.fail("新增失败");
        }
    }

    @ApiOperation("批量删除")
    @GetMapping("/removeByIds/{ids}")
    @PreAuthorize("@ss.hasPermission('system:tenant:delete')")
    @OperateLog(module = "租户管理", operateType = OperateTypeEnum.DELETE)
    public ResponseVO<Integer> removeByIds(@PathVariable("ids") List<Long> ids) {
        int row = systemTenantService.deleteByIds(ids);
        if (row > 0) {
            return ResponseVO.success(row, "删除成功");
        } else {
            return ResponseVO.fail("删除失败");
        }
    }

    @ApiOperation("修改租户")
    @PostMapping("/modifyById")
    @PreAuthorize("@ss.hasPermission('system:tenant:update')")
    @OperateLog(module = "租户管理", operateType = OperateTypeEnum.UPDATE)
    public ResponseVO<Integer> modifyById(@Valid @RequestBody SystemTenantUpdateVO vo) {
        int row = systemTenantService.modifyById(vo);
        if (row > 0) {
            return ResponseVO.success(row, "更新成功");
        } else {
            return ResponseVO.fail("更新失败");
        }
    }

    @ApiOperation("租户详情")
    @GetMapping("/detail/{id}")
    public ResponseVO<SystemTenantDO> detail(@PathVariable("id") Long id) {
        return ResponseVO.success(systemTenantService.getDetail(id));
    }

    @ApiOperation("分页查询租户")
    @PostMapping("/page")
    public ResponseVO<PageResult<SystemTenantDO>> page(@Valid @RequestBody SystemTenantQueryVO queryVO) {
        return PageUtils.page(queryVO, () -> systemTenantService.getList(queryVO));
    }

    @ApiOperation("刷新租户缓存")
    @PostMapping("/refreshCache")
    @PreAuthorize("@ss.hasPermission('system:tenant:update')")
    public ResponseVO<Void> refreshCache() {
        systemTenantService.refreshCache();
        return ResponseVO.success();
    }
}
