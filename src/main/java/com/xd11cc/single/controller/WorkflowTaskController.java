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
 * 我的待办/已办 + 任务操作。
 * <p>审批、驳回、转办、可退回节点查询。</p>
 *
 * @author xd11cc
 * @date 2026-08-18
 */
@RestController
@RequestMapping("/workflow")
@Api(tags = "工作流-任务")
@Validated
public class WorkflowTaskController {

    @Autowired
    private WorkflowService workflowService;

    @PostMapping("/task/approve")
    @ApiOperation("审批通过")
    public ResponseVO<Void> approveTask(@Valid @RequestBody WorkflowTaskApproveVO approveVO) {
        workflowService.approveTask(approveVO);
        return ResponseVO.success();
    }

    @PostMapping("/task/reject")
    @ApiOperation("驳回（退回到指定节点）")
    public ResponseVO<Void> rejectTask(@Valid @RequestBody WorkflowTaskRejectVO rejectVO) {
        workflowService.rejectTask(rejectVO);
        return ResponseVO.success();
    }

    @PostMapping("/task/delegate")
    @ApiOperation("转办")
    public ResponseVO<Void> delegateTask(@Valid @RequestBody WorkflowTaskDelegateVO delegateVO) {
        workflowService.delegateTask(delegateVO);
        return ResponseVO.success();
    }

    @GetMapping("/task/rejectable-nodes")
    @ApiOperation("获取当前任务可退回节点列表")
    public ResponseVO<List<WorkflowActivityVO>> getRejectableNodes(@RequestParam @NotBlank String taskId) {
        return ResponseVO.success(workflowService.getRejectableNodes(taskId));
    }

    @PostMapping("/task/todo")
    @ApiOperation("我的待办列表")
    public ResponseVO<PageResult<WorkflowTaskDetailVO>> getMyTodoTasks(@Valid @RequestBody WorkflowTaskQueryVO queryVO) {
        return ResponseVO.success(workflowService.getMyTodoTasks(queryVO));
    }

    @PostMapping("/task/done")
    @ApiOperation("我的已办列表")
    public ResponseVO<PageResult<WorkflowTaskDetailVO>> getMyDoneTasks(@Valid @RequestBody WorkflowTaskQueryVO queryVO) {
        return ResponseVO.success(workflowService.getMyDoneTasks(queryVO));
    }
}
