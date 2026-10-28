package com.xd11cc.single.controller;

import com.xd11cc.single.entity.base.PageResult;
import com.xd11cc.single.entity.base.ResponseVO;
import com.xd11cc.single.entity.vo.*;
import com.xd11cc.single.modules.workflow.WorkflowModelService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

/**
 * 流程模型（草稿）接口。
 * <p>
 * 覆盖设计器侧的生命周期：保存草稿、草稿详情、删除草稿、部署为正式定义，
 * 以及模型分页列表。
 * </p>
 *
 * <p>
 * <b>注意路径错位（历史遗留）</b>：模型分页接口挂在 {@code /workflow/definition/page}
 * 而非 {@code /workflow/model/page}。因为前端已依赖该路径，本次拆分刻意保持 URL 不变，
 * 只把实现迁到 {@link WorkflowModelService#getModelPage}。后续若要做 API 规范化，
 * 需前后端同步调整。
 * </p>
 *
 * @author xd11cc
 */
@RestController
@RequestMapping("/workflow")
@Api(tags = "工作流-流程模型")
@Validated
public class WorkflowModelController {

    @Autowired
    private WorkflowModelService workflowModelService;

    // ==================== 草稿 CRUD ====================

    @PostMapping("/draft/save")
    @ApiOperation("保存流程草稿（新建或更新）")
    public ResponseVO<WorkflowModelVO> saveDraft(@Valid @RequestBody WorkflowDraftSaveVO saveVO) {
        return ResponseVO.success(workflowModelService.saveDraft(saveVO));
    }

    @GetMapping("/draft/{modelId}")
    @ApiOperation("草稿详情")
    public ResponseVO<WorkflowModelVO> getDraftDetail(@PathVariable @NotBlank String modelId) {
        return ResponseVO.success(workflowModelService.getModelDetail(modelId));
    }

    @DeleteMapping("/draft/{modelId}")
    @ApiOperation("删除草稿（仅未部署可删）")
    public ResponseVO<Void> removeDraft(@PathVariable @NotBlank String modelId) {
        workflowModelService.removeDraft(modelId);
        return ResponseVO.success();
    }

    @PostMapping("/draft/deploy")
    @ApiOperation("部署草稿为正式流程定义")
    public ResponseVO<String> deployDraft(@Valid @RequestBody WorkflowDraftDeployVO deployVO) {
        return ResponseVO.success(workflowModelService.deployDraft(deployVO));
    }

    // ==================== 模型列表 ====================

    /**
     * 模型分页列表。
     * <p>路径沿用 {@code /definition/page}，但数据来自模型（草稿 + 已部署）。</p>
     */
    @PostMapping("/definition/page")
    @ApiOperation("模型分页列表（含草稿 + 已部署）")
    public ResponseVO<PageResult<WorkflowModelVO>> getDefinitionPage(
            @Valid @RequestBody WorkflowDraftQueryVO queryVO) {
        return ResponseVO.success(workflowModelService.getModelPage(queryVO));
    }
}
