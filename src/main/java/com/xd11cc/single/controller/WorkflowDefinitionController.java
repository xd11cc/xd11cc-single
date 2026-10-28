package com.xd11cc.single.controller;

import com.xd11cc.single.entity.base.ResponseVO;
import com.xd11cc.single.entity.vo.WorkflowDefinitionVersionVO;
import com.xd11cc.single.modules.workflow.WorkflowDefinitionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * 已部署流程定义治理接口。
 * <p>
 * 只覆盖 {@code ACT_RE_PROCDEF} 上的管理动作：挂起、激活、版本查询。
 * 草稿的编辑与部署已迁至 {@link WorkflowModelController}。
 * </p>
 *
 * @author xd11cc
 * @date 2026-08-18
 */
@RestController
@RequestMapping("/workflow")
@Api(tags = "工作流-流程定义治理")
@Validated
public class WorkflowDefinitionController {

    @Autowired
    private WorkflowDefinitionService workflowDefinitionService;

    @PostMapping("/definition/suspend")
    @ApiOperation("挂起流程定义")
    public ResponseVO<Void> suspendDefinition(@RequestParam @NotBlank String processDefinitionId) {
        workflowDefinitionService.suspendDefinition(processDefinitionId);
        return ResponseVO.success();
    }

    @PostMapping("/definition/activate")
    @ApiOperation("激活流程定义")
    public ResponseVO<Void> activateDefinition(@RequestParam @NotBlank String processDefinitionId) {
        workflowDefinitionService.activateDefinition(processDefinitionId);
        return ResponseVO.success();
    }

    @GetMapping("/definition/versions")
    @ApiOperation("查询流程定义版本详情")
    public ResponseVO<List<WorkflowDefinitionVersionVO>> getDefinitionVersions(
            @RequestParam @NotBlank String key) {
        return ResponseVO.success(workflowDefinitionService.getDefinitionVersions(key));
    }
}
