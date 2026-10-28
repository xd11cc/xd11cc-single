package com.xd11cc.single.controller;

import com.xd11cc.single.entity.base.PageResult;
import com.xd11cc.single.entity.base.ResponseVO;
import com.xd11cc.single.entity.vo.WorkflowInstanceDetailVO;
import com.xd11cc.single.entity.vo.WorkflowInstanceQueryVO;
import com.xd11cc.single.modules.workflow.WorkflowInstanceService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

/**
 * 流程实例生命周期管理。
 * <p>撤回、详情、我发起的流程列表。</p>
 *
 * <p>说明：发起流程（process/start）需与具体业务单据绑定，待业务模块接入后另行开放。</p>
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
    private WorkflowInstanceService workflowInstanceService;

    @PostMapping("/process/cancel")
    @ApiOperation("取消/撤回流程（仅发起人）")
    public ResponseVO<Void> cancelProcess(@RequestParam @NotBlank String processInstanceId,
                                          @RequestParam(required = false) String reason) {
        workflowInstanceService.cancelProcess(processInstanceId, reason);
        return ResponseVO.success();
    }

    @GetMapping("/process/{processInstanceId}")
    @ApiOperation("流程详情（含审批历史 + 当前活动任务）")
    public ResponseVO<WorkflowInstanceDetailVO> getProcessDetail(
            @PathVariable @NotBlank String processInstanceId) {
        return ResponseVO.success(workflowInstanceService.getProcessDetail(processInstanceId));
    }

    @PostMapping("/process/mine")
    @ApiOperation("我发起的流程列表")
    public ResponseVO<PageResult<WorkflowInstanceDetailVO>> getMyStartedProcesses(
            @Valid @RequestBody WorkflowInstanceQueryVO queryVO) {
        return ResponseVO.success(workflowInstanceService.getMyStartedProcesses(queryVO));
    }
}
