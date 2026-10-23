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
 * 流程实例生命周期管理。
 * <p>发起、取消、详情、历史活动、运行时变量读写。</p>
 *
 * @author xd11cc
 * @date 2026-08-18
 */
@RestController
@RequestMapping("/workflow")
@Api(tags = "工作流-流程实例")
@Validated
public class WorkflowProcessController {

    @Autowired
    private WorkflowService workflowService;

    @PostMapping("/process/start")
    @ApiOperation("发起流程")
    public ResponseVO<String> startProcess(@Valid @RequestBody WorkflowStartVO startVO) {
        return ResponseVO.success(workflowService.startProcess(startVO));
    }

    @PostMapping("/process/cancel")
    @ApiOperation("取消/撤回流程（仅发起人）")
    public ResponseVO<Void> cancelProcess(@RequestParam @NotBlank String processInstanceId,
                                          @RequestParam(required = false) String reason) {
        workflowService.cancelProcess(processInstanceId, reason);
        return ResponseVO.success();
    }

    @GetMapping("/process/{processInstanceId}")
    @ApiOperation("流程详情（含审批历史 + 当前活动任务）")
    public ResponseVO<WorkflowInstanceDetailVO> getProcessDetail(
            @PathVariable @NotBlank String processInstanceId) {
        return ResponseVO.success(workflowService.getProcessDetail(processInstanceId));
    }

    @PostMapping("/process/activities")
    @ApiOperation("历史活动节点列表（流程轨迹，分页）")
    public ResponseVO<PageResult<WorkflowModelActivityVO>> getHistoricActivities(
            @RequestParam @NotBlank String processInstanceId,
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        return ResponseVO.success(workflowService.getHistoricActivities(processInstanceId, pageNo, pageSize));
    }

    @GetMapping("/process/{processInstanceId}/variables")
    @ApiOperation("查询运行时流程变量")
    public ResponseVO<List<WorkflowProcessVariableVO>> getProcessVariables(
            @PathVariable @NotBlank String processInstanceId) {
        return ResponseVO.success(workflowService.getProcessVariables(processInstanceId));
    }

    @PostMapping("/process/{processInstanceId}/variables")
    @ApiOperation("更新运行时流程变量")
    public ResponseVO<Void> updateProcessVariables(@PathVariable @NotBlank String processInstanceId,
                                                   @RequestBody java.util.Map<String, Object> variables) {
        workflowService.updateProcessVariables(processInstanceId, variables);
        return ResponseVO.success();
    }

    @PostMapping("/process/mine")
    @ApiOperation("我发起的流程列表")
    public ResponseVO<PageResult<WorkflowInstanceDetailVO>> getMyStartedProcesses(
            @Valid @RequestBody WorkflowInstanceQueryVO queryVO) {
        return ResponseVO.success(workflowService.getMyStartedProcesses(queryVO));
    }

    @PostMapping("/process/page")
    @ApiOperation("流程实例分页（管理员视角，支持按发起人/状态/流程定义筛选）")
    public ResponseVO<PageResult<WorkflowInstanceDetailVO>> getProcessPage(
            @Valid @RequestBody WorkflowInstanceQueryVO queryVO) {
        return ResponseVO.success(workflowService.getProcessPage(queryVO));
    }
}
