package com.xd11cc.single.controller;

import com.xd11cc.single.common.annotation.OperateLog;
import com.xd11cc.single.entity.base.PageResult;
import com.xd11cc.single.entity.base.ResponseVO;
import com.xd11cc.single.entity.domain.WorkflowCategoryDO;
import com.xd11cc.single.entity.vo.WorkflowCategoryAddVO;
import com.xd11cc.single.entity.vo.WorkflowCategoryQueryVO;
import com.xd11cc.single.entity.vo.WorkflowCategoryUpdateVO;
import com.xd11cc.single.enums.OperateTypeEnum;
import com.xd11cc.single.service.IWorkflowCategoryService;
import com.xd11cc.single.utils.PageUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
* @author xd11cc
* @date 2026-08-21
*/
@RestController
@RequestMapping("/workflow/category")
@Api(tags = "Flowable流程分类")
@Validated
public class WorkflowCategoryController {

    private final IWorkflowCategoryService workflowCategoryService;

    public WorkflowCategoryController(IWorkflowCategoryService workflowCategoryService) {
        this.workflowCategoryService = workflowCategoryService;
    }

    @ApiOperation("新增流程分类")
    @PostMapping("/add")
    @PreAuthorize("@ss.hasPermission('workflow:category:add')")
    @OperateLog(module = "流程分类", operateType = OperateTypeEnum.ADD)
    public ResponseVO<Integer> add(@Valid @RequestBody WorkflowCategoryAddVO addVO) {
        int row = workflowCategoryService.add(addVO);
        return row > 0 ? ResponseVO.success(row, "新增成功") : ResponseVO.fail("新增失败");
    }

    @ApiOperation("修改流程分类")
    @PostMapping("/modifyById")
    @PreAuthorize("@ss.hasPermission('workflow:category:update')")
    @OperateLog(module = "流程分类", operateType = OperateTypeEnum.UPDATE)
    public ResponseVO<Integer> modifyById(@Valid @RequestBody WorkflowCategoryUpdateVO updateVO) {
        int row = workflowCategoryService.modifyById(updateVO);
        return row > 0 ? ResponseVO.success(row, "修改成功") : ResponseVO.fail("修改失败");
    }

    @ApiOperation("删除流程分类")
    @GetMapping("/removeByIds/{ids}")
    @PreAuthorize("@ss.hasPermission('workflow:category:delete')")
    @OperateLog(module = "流程分类", operateType = OperateTypeEnum.DELETE)
    public ResponseVO<Integer> removeByIds(@PathVariable("ids") List<Long> ids) {
        int row = workflowCategoryService.deleteByIds(ids);
        return row > 0 ? ResponseVO.success(row, "删除成功") : ResponseVO.fail("删除失败");
    }

    @ApiOperation("流程分类分页")
    @PostMapping("/page")
    public ResponseVO<PageResult<WorkflowCategoryDO>> page(@Valid @RequestBody WorkflowCategoryQueryVO queryVO) {
        return PageUtils.page(queryVO, () -> workflowCategoryService.getList(queryVO));
    }

    @ApiOperation("流程分类列表")
    @GetMapping("/list")
    public ResponseVO<List<WorkflowCategoryDO>> list() {
        WorkflowCategoryQueryVO queryVO = new WorkflowCategoryQueryVO();
        queryVO.setStatus("0");
        return ResponseVO.success(workflowCategoryService.getList(queryVO));
    }

}
