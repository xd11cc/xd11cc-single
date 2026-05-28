package com.xd11cc.single.controller;

import com.xd11cc.single.entity.base.ResponseVO;
import com.xd11cc.single.entity.domain.SystemUserDO;
import com.xd11cc.single.entity.vo.SystemUserAddVO;
import com.xd11cc.single.entity.vo.SystemUserChangePasswordVO;
import com.xd11cc.single.entity.vo.SystemUserDetailVO;
import com.xd11cc.single.entity.vo.SystemUserQueryVO;
import com.xd11cc.single.entity.vo.SystemUserUpdateVO;
import com.xd11cc.single.service.ISystemUserService;
import com.xd11cc.single.utils.PageUtils;
import com.xd11cc.single.utils.SecurityUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * @Author: xd11cc
 * @Date: 2025/7/9 16:38
 **/
@RestController
@RequestMapping("/system/user")
@Api(tags = "用户管理")
@Validated
public class SystemUserController {

    @Autowired
    private ISystemUserService systemUserService;

    @PostMapping("/add")
    @ApiOperation("新增用户")
    @PreAuthorize("@ss.hasPermission('system:user:add')")
    public ResponseVO<Integer> add(@Valid @RequestBody SystemUserAddVO systemUserAddVO) {
        int row = systemUserService.add(systemUserAddVO);
        if (row > 0) {
            return ResponseVO.success(row, "新增成功");
        } else {
            return ResponseVO.fail("新增失败");
        }
    }

    @GetMapping("/removeByIds/{ids}")
    @ApiOperation("删除用户")
    @PreAuthorize("@ss.hasPermission('system:user:delete')")
    public ResponseVO<Integer> removeByIds(@PathVariable("ids") List<Long> ids) {
        int row = systemUserService.deleteByIds(ids);
        if (row > 0) {
            return ResponseVO.success(row, "删除成功");
        } else {
            return ResponseVO.fail("删除失败");
        }
    }

    @PostMapping("/modifyById")
    @ApiOperation("修改用户")
    @PreAuthorize("@ss.hasPermission('system:user:update')")
    public ResponseVO<Integer> modifyById(@Valid @RequestBody SystemUserUpdateVO systemUserUpdateVO) {
        int row = systemUserService.modifyById(systemUserUpdateVO);
        if (row > 0) {
            return ResponseVO.success(row, "修改成功");
        } else {
            return ResponseVO.fail("修改失败");
        }
    }

    @PostMapping("/page")
    @ApiOperation("用户分页")
    public ResponseVO<List<SystemUserDO>> page(@Valid @RequestBody SystemUserQueryVO systemUserQueryVO) {
        return PageUtils.page(systemUserQueryVO, () -> systemUserService.getList(systemUserQueryVO));
    }

    @PostMapping("/list")
    @ApiOperation("用户列表")
    public ResponseVO<List<SystemUserDO>> list(@RequestBody SystemUserQueryVO systemUserQueryVO) {
        return ResponseVO.success(systemUserService.getList(systemUserQueryVO));
    }

    @GetMapping("/getById/{id}")
    @ApiOperation("根据id查询用户")
    public ResponseVO<SystemUserDetailVO> getById(@PathVariable("id") Long id) {
        return ResponseVO.success(systemUserService.getDetailById(id));
    }

    @PostMapping("/resetPassword/{id}")
    @ApiOperation("重置密码")
    @PreAuthorize("@ss.hasPermission('system:user:resetPassword')")
    public ResponseVO<Integer> resetPassword(@PathVariable("id") Long id, @RequestParam("newPassword") String newPassword) {
        int row = systemUserService.resetPassword(id, newPassword);
        if (row > 0) {
            return ResponseVO.success(row, "重置成功");
        } else {
            return ResponseVO.fail("重置失败");
        }
    }

    @PostMapping("/changePassword")
    @ApiOperation("修改密码")
    public ResponseVO<Integer> changePassword(@Valid @RequestBody SystemUserChangePasswordVO changePasswordVO) {
        int row = systemUserService.changePassword(SecurityUtils.getUserId(),
                changePasswordVO.getOldPassword(), changePasswordVO.getNewPassword());
        if (row > 0) {
            return ResponseVO.success(row, "修改成功");
        } else {
            return ResponseVO.fail("修改失败");
        }
    }
}
