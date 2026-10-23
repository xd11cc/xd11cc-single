package com.xd11cc.single.controller;

import com.xd11cc.single.config.annotation.OperateLog;
import com.xd11cc.single.entity.base.PageResult;
import com.xd11cc.single.entity.base.ResponseVO;
import com.xd11cc.single.entity.domain.WorkflowExpressionConfigDO;
import com.xd11cc.single.entity.vo.WorkflowExpressionConfigAddVO;
import com.xd11cc.single.entity.vo.WorkflowExpressionConfigQueryVO;
import com.xd11cc.single.entity.vo.WorkflowExpressionConfigUpdateVO;
import com.xd11cc.single.enums.OperateTypeEnum;
import com.xd11cc.single.service.IWorkflowExpressionConfigService;
import com.xd11cc.single.utils.PageUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Api(tags = "流程表达式配置")
@Validated
@RestController
@RequestMapping("/workflow/expression-config")
public class WorkflowExpressionConfigController {

    @Autowired
    private IWorkflowExpressionConfigService workflowExpressionConfigService;

    @ApiOperation("新增流程表达式配置")
    @PostMapping("/add")
    @PreAuthorize("@ss.hasPermission('workflow:expression-config:add')")
    @OperateLog(module = "流程表达式配置", operateType = OperateTypeEnum.ADD)
    public ResponseVO<Integer> add(@Valid @RequestBody WorkflowExpressionConfigAddVO workflowExpressionConfigAddVO) {
        int row = workflowExpressionConfigService.add(workflowExpressionConfigAddVO);
        if (row > 0) {
            return ResponseVO.success(row, "新增成功");
        } else {
            return ResponseVO.fail("新增失败");
        }
    }

    @ApiOperation("修改流程表达式配置")
    @PostMapping("/modifyById")
    @PreAuthorize("@ss.hasPermission('workflow:expression-config:update')")
    @OperateLog(module = "流程表达式配置", operateType = OperateTypeEnum.UPDATE)
    public ResponseVO<Integer> modifyById(@Valid @RequestBody WorkflowExpressionConfigUpdateVO workflowExpressionConfigUpdateVO) {
        int row = workflowExpressionConfigService.modifyById(workflowExpressionConfigUpdateVO);
        if (row > 0) {
            return ResponseVO.success(row, "修改成功");
        } else {
            return ResponseVO.fail("修改失败");
        }
    }

    @ApiOperation("删除流程表达式配置")
    @GetMapping("/removeByIds/{ids}")
    @PreAuthorize("@ss.hasPermission('workflow:expression-config:delete')")
    @OperateLog(module = "流程表达式配置", operateType = OperateTypeEnum.DELETE)
    public ResponseVO<Integer> removeByIds(@PathVariable("ids") List<Long> ids) {
        int row = workflowExpressionConfigService.deleteByIds(ids);
        if (row > 0) {
            return ResponseVO.success(row, "删除成功");
        } else {
            return ResponseVO.fail("删除失败");
        }
    }

    @ApiOperation("流程表达式配置分页")
    @PostMapping("/page")
    public ResponseVO<PageResult<WorkflowExpressionConfigDO>> page(
            @RequestBody WorkflowExpressionConfigQueryVO workflowExpressionConfigQueryVO) {
        return PageUtils.page(workflowExpressionConfigQueryVO,
                () -> workflowExpressionConfigService.getList(workflowExpressionConfigQueryVO));
    }

    @ApiOperation("流程表达式配置列表")
    @GetMapping("/list")
    public ResponseVO<List<WorkflowExpressionConfigDO>> list() {
        return ResponseVO.success(workflowExpressionConfigService.list());
    }

    @ApiOperation("按使用场景查询流程表达式配置列表")
    @GetMapping("/options")
    public ResponseVO<List<WorkflowExpressionConfigDO>> options(@RequestParam(required = false) String expressionType) {
        return ResponseVO.success(workflowExpressionConfigService.getOptions(expressionType));
    }
}
