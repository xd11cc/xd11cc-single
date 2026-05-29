package com.xd11cc.single.controller;

import com.xd11cc.single.config.annotation.OperateLog;
import com.xd11cc.single.entity.base.PageResult;
import com.xd11cc.single.entity.base.ResponseVO;
import com.xd11cc.single.entity.domain.SystemNoticeDO;
import com.xd11cc.single.entity.vo.SystemNoticeAddVO;
import com.xd11cc.single.entity.vo.SystemNoticeQueryVO;
import com.xd11cc.single.entity.vo.SystemNoticeUpdateVO;
import com.xd11cc.single.enums.OperateTypeEnum;
import com.xd11cc.single.service.ISystemNoticeService;
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
 * @date 2026-05-29
 */
@RestController
@RequestMapping("/system/notice")
@Api(tags = "通知管理")
@Validated
public class SystemNoticeController {

    @Autowired
    private ISystemNoticeService systemNoticeService;

    @PostMapping("/add")
    @ApiOperation("新增通知")
    @PreAuthorize("@ss.hasPermission('system:notice:add')")
    @OperateLog(module = "通知管理", operateType = OperateTypeEnum.ADD)
    public ResponseVO<Integer> add(@Valid @RequestBody SystemNoticeAddVO addVO) {
        int row = systemNoticeService.add(addVO);
        if (row > 0) {
            return ResponseVO.success(row, "新增成功");
        } else {
            return ResponseVO.fail("新增失败");
        }
    }

    @PostMapping("/modifyById")
    @ApiOperation("修改通知")
    @PreAuthorize("@ss.hasPermission('system:notice:update')")
    @OperateLog(module = "通知管理", operateType = OperateTypeEnum.UPDATE)
    public ResponseVO<Integer> modifyById(@Valid @RequestBody SystemNoticeUpdateVO updateVO) {
        int row = systemNoticeService.modifyById(updateVO);
        if (row > 0) {
            return ResponseVO.success(row, "修改成功");
        } else {
            return ResponseVO.fail("修改失败");
        }
    }

    @GetMapping("/removeByIds/{ids}")
    @ApiOperation("删除通知")
    @PreAuthorize("@ss.hasPermission('system:notice:delete')")
    @OperateLog(module = "通知管理", operateType = OperateTypeEnum.DELETE)
    public ResponseVO<Integer> removeByIds(@PathVariable("ids") List<Long> ids) {
        int row = systemNoticeService.deleteByIds(ids);
        if (row > 0) {
            return ResponseVO.success(row, "删除成功");
        } else {
            return ResponseVO.fail("删除失败");
        }
    }

    @PostMapping("/page")
    @ApiOperation("通知分页")
    public ResponseVO<PageResult<SystemNoticeDO>> page(@Valid @RequestBody SystemNoticeQueryVO queryVO) {
        return PageUtils.page(queryVO, () -> systemNoticeService.getList(queryVO));
    }

    @GetMapping("/getById/{id}")
    @ApiOperation("通知详情")
    public ResponseVO<SystemNoticeDO> getById(@PathVariable("id") Long id) {
        return ResponseVO.success(systemNoticeService.getDetailById(id));
    }

    @PostMapping("/publish/{id}")
    @ApiOperation("发布通知")
    @PreAuthorize("@ss.hasPermission('system:notice:publish')")
    @OperateLog(module = "通知管理", operateType = OperateTypeEnum.UPDATE, operateDesc = "发布通知")
    public ResponseVO<Void> publish(@PathVariable("id") Long id) {
        systemNoticeService.publish(id);
        return ResponseVO.success(null, "发布成功");
    }

    @PostMapping("/revoke/{id}")
    @ApiOperation("撤回通知")
    @PreAuthorize("@ss.hasPermission('system:notice:publish')")
    @OperateLog(module = "通知管理", operateType = OperateTypeEnum.UPDATE, operateDesc = "撤回通知")
    public ResponseVO<Void> revoke(@PathVariable("id") Long id) {
        systemNoticeService.revoke(id);
        return ResponseVO.success(null, "撤回成功");
    }
}
