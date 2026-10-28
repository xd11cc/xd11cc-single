package com.xd11cc.single.modules.workflow.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xd11cc.single.common.context.TenantContextHolder;
import com.xd11cc.single.common.exception.ServiceException;
import com.xd11cc.single.entity.base.PageResult;
import com.xd11cc.single.entity.domain.WorkflowInstanceDO;
import com.xd11cc.single.entity.dto.WorkflowContextDTO;
import com.xd11cc.single.entity.vo.WorkflowInstanceDetailVO;
import com.xd11cc.single.entity.vo.WorkflowInstanceQueryVO;
import com.xd11cc.single.entity.vo.WorkflowTaskDetailVO;
import com.xd11cc.single.enums.SystemErrorEnum;
import com.xd11cc.single.enums.WorkflowStatusEnum;
import com.xd11cc.single.mapper.WorkflowInstanceMapper;
import com.xd11cc.single.modules.workflow.AbstractWorkflowHandler;
import com.xd11cc.single.modules.workflow.WorkflowEvent;
import com.xd11cc.single.modules.workflow.WorkflowHandlerFactory;
import com.xd11cc.single.modules.workflow.WorkflowInstanceService;
import com.xd11cc.single.modules.workflow.WorkflowQuerySupport;
import com.xd11cc.single.utils.SecurityUtils;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.task.api.Task;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 流程实例服务实现。
 *
 * @author xd11cc
 */
@Service
public class WorkflowInstanceServiceImpl implements WorkflowInstanceService {

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private HistoryService historyService;

    @Autowired
    private WorkflowInstanceMapper workflowInstanceMapper;

    @Autowired
    private WorkflowHandlerFactory handlerFactory;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private WorkflowQuerySupport querySupport;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelProcess(String processInstanceId, String reason) {
        WorkflowInstanceDO instance = querySupport.getInstanceByProcessId(processInstanceId);

        if (!instance.getStarterUserId().equals(SecurityUtils.getUserId())) {
            throw new ServiceException(SystemErrorEnum.TASK_NOT_ASSIGNEE);
        }
        // 注意：status 已是 String，必须用 equals 比较。
        // 若写成 != 会退化为引用比较，两个条件恒为 true，导致任何状态都无法取消流程。
        if (!Objects.equals(instance.getStatus(), WorkflowStatusEnum.RUNNING.getCode())
                && !Objects.equals(instance.getStatus(), WorkflowStatusEnum.REJECTED.getCode())) {
            throw new ServiceException(SystemErrorEnum.PROCESS_CANNOT_CANCEL);
        }

        runtimeService.deleteProcessInstance(processInstanceId,
                reason != null ? reason : "发起人撤回");

        instance.setStatus(WorkflowStatusEnum.CANCELLED.getCode());
        workflowInstanceMapper.updateById(instance);

        AbstractWorkflowHandler handler = handlerFactory.getHandler(instance.getProcessDefinitionKey());
        WorkflowContextDTO context = new WorkflowContextDTO();
        context.setProcessInstanceId(processInstanceId);
        context.setProcessDefinitionKey(instance.getProcessDefinitionKey());
        context.setBusinessKey(instance.getBusinessKey());
        context.setOperatorUserId(SecurityUtils.getUserId());
        context.setTenantId(instance.getTenantId());
        context.setComment(reason);
        handler.onProcessCancel(context);

        eventPublisher.publishEvent(WorkflowEvent.cancelled(this, processInstanceId,
                instance.getProcessDefinitionKey(), instance.getBusinessKey(),
                instance.getTenantId(), instance.getStarterUserId()));
    }

    @Override
    public WorkflowInstanceDetailVO getProcessDetail(String processInstanceId) {
        WorkflowInstanceDO instance = querySupport.getInstanceByProcessId(processInstanceId);

        WorkflowInstanceDetailVO detail = convertToInstanceDetailVO(instance);

        // 查询完整审批历史（任务级）
        List<HistoricTaskInstance> historyTasks = historyService.createHistoricTaskInstanceQuery()
                .processInstanceId(processInstanceId)
                .orderByHistoricTaskInstanceStartTime().asc()
                .list();

        // 审批意见必须先按流程实例整体取出再按 taskId 分组
        Map<String, org.flowable.engine.task.Comment> latestCommentByTask =
                querySupport.getLatestCommentsByTask(processInstanceId);

        List<WorkflowTaskDetailVO> taskHistory = historyTasks.stream()
                .map(ht -> {
                    WorkflowTaskDetailVO vo = querySupport.convertHistoricToTaskDetailVO(ht);
                    org.flowable.engine.task.Comment lastComment = latestCommentByTask.get(ht.getId());
                    if (lastComment != null) {
                        vo.setComment(lastComment.getFullMessage());
                        vo.setCommentType(lastComment.getType());
                    }
                    return vo;
                })
                .collect(Collectors.toList());
        detail.setTaskHistory(taskHistory);

        // 当前活动任务
        List<Task> currentTasks = taskService.createTaskQuery()
                .processInstanceId(processInstanceId).list();
        detail.setCurrentTasks(currentTasks.stream()
                .map(querySupport::convertToTaskDetailVO)
                .collect(Collectors.toList()));

        // 流程结束时间
        HistoricProcessInstance historicProcess = historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(processInstanceId).singleResult();
        if (historicProcess != null) {
            detail.setEndTime(historicProcess.getEndTime());
        }

        return detail;
    }

    @Override
    public PageResult<WorkflowInstanceDetailVO> getMyStartedProcesses(WorkflowInstanceQueryVO queryVO) {
        Long userId = SecurityUtils.getUserId();
        Long tenantId = TenantContextHolder.getTenantId();

        LambdaQueryWrapper<WorkflowInstanceDO> wrapper = new LambdaQueryWrapper<WorkflowInstanceDO>()
                .eq(WorkflowInstanceDO::getStarterUserId, userId)
                .eq(WorkflowInstanceDO::getTenantId, tenantId)
                .eq(queryVO.getStatus() != null, WorkflowInstanceDO::getStatus, queryVO.getStatus())
                .like(queryVO.getTitle() != null, WorkflowInstanceDO::getTitle, queryVO.getTitle())
                .orderByDesc(WorkflowInstanceDO::getCreateTime);

        return selectInstancePage(wrapper, queryVO);
    }

    /**
     * 统一分页查询：走 PageHelper（与项目其他模块的 PageUtils 同一套机制），
     * 不再手工拼接 {@code LIMIT}，避免分页逻辑散落和 SQL 注入风险。
     */
    private PageResult<WorkflowInstanceDetailVO> selectInstancePage(
            LambdaQueryWrapper<WorkflowInstanceDO> wrapper, WorkflowInstanceQueryVO queryVO) {
        try {
            PageHelper.startPage(queryVO.getCurrentPage(), queryVO.getPageSize());
            List<WorkflowInstanceDO> instances = workflowInstanceMapper.selectList(wrapper);
            PageInfo<WorkflowInstanceDO> pageInfo = new PageInfo<>(instances);
            List<WorkflowInstanceDetailVO> list = pageInfo.getList().stream()
                    .map(this::convertToInstanceDetailVO)
                    .collect(Collectors.toList());
            return new PageResult<>(list, pageInfo.getTotal());
        } finally {
            PageHelper.clearPage();
        }
    }

    private WorkflowInstanceDetailVO convertToInstanceDetailVO(WorkflowInstanceDO instance) {
        WorkflowInstanceDetailVO detail = new WorkflowInstanceDetailVO();
        detail.setProcessInstanceId(instance.getProcessInstanceId());
        detail.setProcessDefinitionKey(instance.getProcessDefinitionKey());
        detail.setBusinessKey(instance.getBusinessKey());
        detail.setTitle(instance.getTitle());
        detail.setStatus(instance.getStatus());
        detail.setStarterUserId(instance.getStarterUserId());
        detail.setStarterUsername(instance.getStarterUsername());
        detail.setCreateTime(instance.getCreateTime());
        return detail;
    }
}
