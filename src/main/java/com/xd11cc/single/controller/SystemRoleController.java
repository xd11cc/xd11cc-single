package com.xd11cc.single.controller;

import com.xd11cc.single.config.annotation.OperateLog;
import com.xd11cc.single.entity.base.ResponseVO;
import com.xd11cc.single.entity.domain.SystemRoleDO;
import com.xd11cc.single.entity.vo.SystemRoleAddVO;
import com.xd11cc.single.entity.vo.SystemRoleQueryVO;
import com.xd11cc.single.entity.vo.SystemRoleUpdateVO;
import com.xd11cc.single.enums.OperateTypeEnum;
import com.xd11cc.single.service.ISystemRoleDeptService;
import com.xd11cc.single.service.ISystemRoleService;
import com.xd11cc.single.utils.PageUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Set;

/**
 * @author xd11cc
 * @date 2026-05-27
 */
@Validated
@RestController
@RequestMapping("/system/role")
@Api(tags = "角色管理")
public class SystemRoleController {

    @Autowired
    private ISystemRoleService systemRoleService;

    @Autowired
    private ISystemRoleDeptService systemRoleDeptService;

    @ApiOperation("新增角色")
    @PostMapping("/add")
    @PreAuthorize("@ss.hasPermission('system:role:add')")
    @OperateLog(module = "角色管理", operateType = OperateTypeEnum.ADD)
    public ResponseVO<Integer> add(@Valid @RequestBody SystemRoleAddVO systemRoleAddVO) {
        int row = systemRoleService.add(systemRoleAddVO);
        if (row > 0) {
            return ResponseVO.success(row, "新增成功");
        } else {
            return ResponseVO.fail("新增失败");
        }
    }

    @ApiOperation("删除角色")
    @GetMapping("/removeByIds/{ids}")
    @PreAuthorize("@ss.hasPermission('system:role:delete')")
    @OperateLog(module = "角色管理", operateType = OperateTypeEnum.DELETE)
    public ResponseVO<Integer> removeByIds(@PathVariable("ids") List<Long> ids) {
        int row = systemRoleService.deleteByIds(ids);
        if (row > 0) {
            return ResponseVO.success(row, "删除成功");
        } else {
            return ResponseVO.fail("删除失败");
        }
    }

    @ApiOperation("修改角色")
    @PostMapping("/modifyById")
    @PreAuthorize("@ss.hasPermission('system:role:update')")
    @OperateLog(module = "角色管理", operateType = OperateTypeEnum.UPDATE)
    public ResponseVO<Integer> modifyById(@Valid @RequestBody SystemRoleUpdateVO systemRoleUpdateVO) {
        int row = systemRoleService.modifyById(systemRoleUpdateVO);
        if (row > 0) {
            return ResponseVO.success(row, "修改成功");
        } else {
            return ResponseVO.fail("修改失败");
        }
    }

    @ApiOperation("角色分页")
    @PostMapping("/page")
    public ResponseVO<List<SystemRoleDO>> page(@Valid @RequestBody SystemRoleQueryVO systemRoleQueryVO) {
        return PageUtils.page(systemRoleQueryVO, () -> systemRoleService.getList(systemRoleQueryVO));
    }

    @ApiOperation("角色列表")
    @PostMapping("/list")
    public ResponseVO<List<SystemRoleDO>> list(@RequestBody SystemRoleQueryVO systemRoleQueryVO) {
        return ResponseVO.success(systemRoleService.getList(systemRoleQueryVO));
    }

    @ApiOperation("查询角色已分配的菜单id列表")
    @GetMapping("/menuIds/{roleId}")
    public ResponseVO<List<Long>> getMenuIds(@PathVariable("roleId") Long roleId) {
        return ResponseVO.success(systemRoleService.getMenuIdsByRoleId(roleId));
    }

    @ApiOperation("查询角色已分配的部门id列表")
    @GetMapping("/deptIds/{roleId}")
    public ResponseVO<Set<Long>> getDeptIds(@PathVariable("roleId") Long roleId) {
        return ResponseVO.success(systemRoleDeptService.getDeptIdsByRoleId(roleId));
    }
}
