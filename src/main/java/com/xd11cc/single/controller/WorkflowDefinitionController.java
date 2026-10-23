package com.xd11cc.single.controller;

import com.xd11cc.single.entity.base.PageResult;
import com.xd11cc.single.entity.base.ResponseVO;
import com.xd11cc.single.entity.vo.*;
import com.xd11cc.single.service.WorkflowService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * 流程定义管理 + 设计器相关接口。
 * <p>按职责拆分：定义 CRUD（草稿/部署/挂起）、设计器导入导出、缩略图。</p>
 *
 * @author xd11cc
 * @date 2026-08-18
 */
@RestController
@RequestMapping("/workflow")
@Api(tags = "工作流-流程定义管理")
@Validated
public class WorkflowDefinitionController {

    @Autowired
    private WorkflowService workflowService;

    // ==================== 草稿 CRUD ====================

    @PostMapping("/draft/save")
    @ApiOperation("保存流程草稿（新建或更新）")
    public ResponseVO<WorkflowModelVO> saveDraft(@Valid @RequestBody WorkflowDraftSaveVO saveVO) {
        return ResponseVO.success(workflowService.saveDraft(saveVO));
    }

    @GetMapping("/draft/{modelId}")
    @ApiOperation("草稿详情")
    public ResponseVO<WorkflowModelVO> getDraftDetail(@PathVariable @NotBlank String modelId) {
        return ResponseVO.success(workflowService.getModelDetail(modelId));
    }

    @DeleteMapping("/draft/{modelId}")
    @ApiOperation("删除草稿（仅未部署可删）")
    public ResponseVO<Void> removeDraft(@PathVariable @NotBlank String modelId) {
        workflowService.removeDraft(modelId);
        return ResponseVO.success();
    }

    @PostMapping("/draft/deploy")
    @ApiOperation("部署草稿为正式流程定义")
    public ResponseVO<String> deployDraft(@Valid @RequestBody WorkflowDraftDeployVO deployVO) {
        return ResponseVO.success(workflowService.deployDraft(deployVO));
    }

    // ==================== 流程定义列表/管理 ====================

    @PostMapping("/definition/page")
    @ApiOperation("流程定义分页列表（含草稿 + 已部署）")
    public ResponseVO<PageResult<WorkflowModelVO>> getDefinitionPage(
            @Valid @RequestBody WorkflowDraftQueryVO queryVO) {
        return ResponseVO.success(workflowService.getModelPage(queryVO));
    }

    @PostMapping("/definition/suspend")
    @ApiOperation("挂起流程定义")
    public ResponseVO<Void> suspendDefinition(@RequestParam @NotBlank String processDefinitionId) {
        workflowService.suspendDefinition(processDefinitionId);
        return ResponseVO.success();
    }

    @PostMapping("/definition/activate")
    @ApiOperation("激活流程定义")
    public ResponseVO<Void> activateDefinition(@RequestParam @NotBlank String processDefinitionId) {
        workflowService.activateDefinition(processDefinitionId);
        return ResponseVO.success();
    }

    @GetMapping("/definition/versions")
    @ApiOperation("查询流程定义版本详情")
    public ResponseVO<List<WorkflowDefinitionVersionVO>> getDefinitionVersions(
            @RequestParam @NotBlank String key) {
        return ResponseVO.success(workflowService.getDefinitionVersions(key));
    }

    @PostMapping("/definition/copy")
    @ApiOperation("复制流程定义为新草稿")
    public ResponseVO<String> copyDefinition(@Valid @RequestBody WorkflowCopyVO copyVO) {
        return ResponseVO.success(workflowService.copyDefinition(copyVO));
    }
}
