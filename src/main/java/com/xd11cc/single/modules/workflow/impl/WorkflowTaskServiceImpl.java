package com.xd11cc.single.modules.workflow.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xd11cc.single.common.context.TenantContextHolder;
import com.xd11cc.single.common.exception.ServiceException;
import com.xd11cc.single.entity.base.PageResult;
import com.xd11cc.single.entity.domain.SystemUserRoleDO;
import com.xd11cc.single.entity.domain.WorkflowInstanceDO;
import com.xd11cc.single.entity.dto.WorkflowContextDTO;
import com.xd11cc.single.entity.vo.WorkflowActivityVO;
import com.xd11cc.single.entity.vo.WorkflowTaskApproveVO;
import com.xd11cc.single.entity.vo.WorkflowTaskDelegateVO;
import com.xd11cc.single.entity.vo.WorkflowTaskDetailVO;
import com.xd11cc.single.entity.vo.WorkflowTaskQueryVO;
import com.xd11cc.single.entity.vo.WorkflowTaskRejectVO;
import com.xd11cc.single.enums.SystemErrorEnum;
import com.xd11cc.single.enums.WorkflowStatusEnum;
import com.xd11cc.single.mapper.SystemUserRoleMapper;
import com.xd11cc.single.mapper.WorkflowInstanceMapper;
import com.xd11cc.single.modules.workflow.AbstractWorkflowHandler;
import com.xd11cc.single.modules.workflow.WorkflowEvent;
import com.xd11cc.single.modules.workflow.WorkflowHandlerFactory;
import com.xd11cc.single.modules.workflow.WorkflowQuerySupport;
import com.xd11cc.single.modules.workflow.WorkflowTaskService;
import com.xd11cc.single.utils.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.flowable.task.api.TaskQuery;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 任务服务实现。
 *
 * @author xd11cc
 */
@Slf4j
@Service
public class WorkflowTaskServiceImpl implements WorkflowTaskService {

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
    private SystemUserRoleMapper systemUserRoleMapper;

    @Autowired
    private WorkflowQuerySupport querySupport;

    // ==================== 审批动作 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approveTask(WorkflowTaskApproveVO approveVO) {
        Task task = getAndValidateTask(approveVO.getTaskId());

        String processInstanceId = task.getProcessInstanceId();
        WorkflowInstanceDO instance = querySupport.getInstanceByProcessId(processInstanceId);
        AbstractWorkflowHandler handler = handlerFactory.getHandler(instance.getProcessDefinitionKey());

        if (approveVO.getComment() != null) {
            taskService.addComment(task.getId(), processInstanceId, "approve", approveVO.getComment());
        }

        Map<String, Object> variables = approveVO.getVariables() != null
                ? approveVO.getVariables() : Collections.emptyMap();
        taskService.complete(task.getId(), variables);

        WorkflowContextDTO context = buildContext(task, instance);
        context.setComment(approveVO.getComment());
        context.setVariables(approveVO.getVariables());
        handler.onTaskApprove(context);

        // 检查流程是否结束
        ProcessInstance running = runtimeService.createProcessInstanceQuery()
                .processInstanceId(processInstanceId).singleResult();
        if (running == null) {
            instance.setStatus(WorkflowStatusEnum.COMPLETED.getCode());
            workflowInstanceMapper.updateById(instance);
            handler.onProcessComplete(context);
            eventPublisher.publishEvent(WorkflowEvent.completed(this, processInstanceId,
                    instance.getProcessDefinitionKey(), instance.getBusinessKey(),
                    instance.getTenantId(), instance.getStarterUserId()));
        } else {
            List<Task> nextTasks = taskService.createTaskQuery()
                    .processInstanceId(processInstanceId).list();
            for (Task nextTask : nextTasks) {
                Long targetUserId = parseUserId(nextTask.getAssignee());
                if (targetUserId != null) {
                    eventPublisher.publishEvent(WorkflowEvent.approved(this, processInstanceId,
                            instance.getProcessDefinitionKey(), instance.getBusinessKey(),
                            instance.getTenantId(), targetUserId));
                }
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rejectTask(WorkflowTaskRejectVO rejectVO) {
        Task task = getAndValidateTask(rejectVO.getTaskId());

        String processInstanceId = task.getProcessInstanceId();
        WorkflowInstanceDO instance = querySupport.getInstanceByProcessId(processInstanceId);
        AbstractWorkflowHandler handler = handlerFactory.getHandler(instance.getProcessDefinitionKey());

        taskService.addComment(task.getId(), processInstanceId, "reject",
                rejectVO.getComment() != null ? rejectVO.getComment() : "驳回");

        runtimeService.createChangeActivityStateBuilder()
                .processInstanceId(processInstanceId)
                .moveActivityIdTo(task.getTaskDefinitionKey(), rejectVO.getTargetActivityId())
                .changeState();

        if (isStartActivity(task, rejectVO.getTargetActivityId())) {
            instance.setStatus(WorkflowStatusEnum.REJECTED.getCode());
            workflowInstanceMapper.updateById(instance);
        }

        WorkflowContextDTO context = buildContext(task, instance);
        context.setComment(rejectVO.getComment());
        handler.onTaskReject(context);

        List<Task> targetTasks = taskService.createTaskQuery()
                .processInstanceId(processInstanceId).list();
        for (Task targetTask : targetTasks) {
            Long targetUserId = parseUserId(targetTask.getAssignee());
            if (targetUserId != null) {
                eventPublisher.publishEvent(WorkflowEvent.rejected(this, processInstanceId,
                        instance.getProcessDefinitionKey(), instance.getBusinessKey(),
                        instance.getTenantId(), targetUserId));
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delegateTask(WorkflowTaskDelegateVO delegateVO) {
        // 先校验目标用户ID，避免“引擎已改派、接口却报错”的不一致状态
        Long targetUserId = parseUserId(delegateVO.getTargetUserId());
        if (targetUserId == null) {
            throw new ServiceException(SystemErrorEnum.DELEGATE_TARGET_USER_INVALID,
                    new Object[]{delegateVO.getTargetUserId()});
        }

        Task task = getAndValidateTask(delegateVO.getTaskId());

        String processInstanceId = task.getProcessInstanceId();
        WorkflowInstanceDO instance = querySupport.getInstanceByProcessId(processInstanceId);

        if (delegateVO.getComment() != null) {
            taskService.addComment(task.getId(), processInstanceId, "delegate", delegateVO.getComment());
        }

        taskService.setAssignee(task.getId(), delegateVO.getTargetUserId());

        AbstractWorkflowHandler handler = handlerFactory.getHandler(instance.getProcessDefinitionKey());
        WorkflowContextDTO context = buildContext(task, instance);
        context.setComment(delegateVO.getComment());
        handler.onTaskDelegate(context);

        eventPublisher.publishEvent(WorkflowEvent.delegated(this, processInstanceId,
                instance.getProcessDefinitionKey(), instance.getBusinessKey(),
                instance.getTenantId(), targetUserId));
    }

    // ==================== 查询 ====================

    @Override
    public List<WorkflowActivityVO> getRejectableNodes(String taskId) {
        Task task = getAndValidateTask(taskId);
        String processInstanceId = task.getProcessInstanceId();
        WorkflowInstanceDO instance = querySupport.getInstanceByProcessId(processInstanceId);
        AbstractWorkflowHandler handler = handlerFactory.getHandler(instance.getProcessDefinitionKey());

        List<HistoricTaskInstance> historyTasks = historyService.createHistoricTaskInstanceQuery()
                .processInstanceId(processInstanceId)
                .finished()
                .orderByHistoricTaskInstanceEndTime().asc()
                .list();

        LinkedHashMap<String, WorkflowActivityVO> nodeMap = new LinkedHashMap<>();
        String startActivityId = resolveStartActivityId(processInstanceId, handler);
        for (HistoricTaskInstance ht : historyTasks) {
            String actId = ht.getTaskDefinitionKey();
            if (!nodeMap.containsKey(actId) && !actId.equals(task.getTaskDefinitionKey())) {
                WorkflowActivityVO vo = new WorkflowActivityVO();
                vo.setActivityId(actId);
                vo.setActivityName(ht.getName());
                vo.setIsStartNode(actId.equals(startActivityId));
                nodeMap.put(actId, vo);
            }
        }

        return new ArrayList<>(nodeMap.values());
    }

    @Override
    public PageResult<WorkflowTaskDetailVO> getMyTodoTasks(WorkflowTaskQueryVO queryVO) {
        String userId = String.valueOf(SecurityUtils.getUserId());
        String tenantId = String.valueOf(TenantContextHolder.getTenantId());

        long count = buildMyTodoTaskQuery(userId, tenantId, queryVO).count();
        if (count == 0) {
            return PageResult.empty();
        }

        List<Task> tasks = buildMyTodoTaskQuery(userId, tenantId, queryVO)
                .orderByTaskCreateTime().desc()
                .listPage((queryVO.getCurrentPage() - 1) * queryVO.getPageSize(), queryVO.getPageSize());

        List<WorkflowTaskDetailVO> list = tasks.stream()
                .map(querySupport::convertToTaskDetailVO)
                .collect(Collectors.toList());

        return new PageResult<>(list, count);
    }

    @Override
    public PageResult<WorkflowTaskDetailVO> getMyDoneTasks(WorkflowTaskQueryVO queryVO) {
        String userId = String.valueOf(SecurityUtils.getUserId());
        String tenantId = String.valueOf(TenantContextHolder.getTenantId());

        long count = historyService.createHistoricTaskInstanceQuery()
                .taskAssignee(userId)
                .taskTenantId(tenantId)
                .finished()
                .count();
        if (count == 0) {
            return PageResult.empty();
        }

        List<HistoricTaskInstance> tasks = historyService.createHistoricTaskInstanceQuery()
                .taskAssignee(userId)
                .taskTenantId(tenantId)
                .finished()
                .orderByHistoricTaskInstanceEndTime().desc()
                .listPage((queryVO.getCurrentPage() - 1) * queryVO.getPageSize(), queryVO.getPageSize());

        List<WorkflowTaskDetailVO> list = tasks.stream()
                .map(querySupport::convertHistoricToTaskDetailVO)
                .collect(Collectors.toList());

        // 补齐每条已办任务的审批意见：按流程实例批量取，避免逐条查询
        Map<String, Map<String, org.flowable.engine.task.Comment>> commentCache = new HashMap<>();
        for (WorkflowTaskDetailVO vo : list) {
            Map<String, org.flowable.engine.task.Comment> byTask = commentCache.computeIfAbsent(
                    vo.getProcessInstanceId(), querySupport::getLatestCommentsByTask);
            org.flowable.engine.task.Comment comment = byTask.get(vo.getTaskId());
            if (comment != null) {
                vo.setComment(comment.getFullMessage());
                vo.setCommentType(comment.getType());
            }
        }

        return new PageResult<>(list, count);
    }

    // ==================== 私有方法 ====================

    /**
     * 构造“我的待办”查询。
     * <p>
     * 待办必须同时覆盖三种归属方式，否则任务会彻底无人可见、流程永久卡死：
     * <ol>
     *     <li>直接分配：{@code assignee = 当前用户}</li>
     *     <li>候选人：{@code candidateUsers} 含当前用户（引擎侧落在 ACT_RU_IDENTITYLINK）</li>
     *     <li>候选组：{@code candidateGroups} 含当前用户所属角色/用户组</li>
     * </ol>
     * 其中 1、2 由 {@code taskCandidateOrAssigned} 覆盖；候选组需单独用 OR 条件并入，
     * 因为 Flowable 的 candidateGroup 匹配的是 IDENTITYLINK 中的 GROUP_ID_，
     * 而本项目以“角色 ID”充当用户组标识。
     * </p>
     */
    private TaskQuery buildMyTodoTaskQuery(String userId, String tenantId, WorkflowTaskQueryVO queryVO) {
        List<String> groupIds = resolveUserGroupIds();
        TaskQuery query = taskService.createTaskQuery();
        if (groupIds.isEmpty()) {
            // 无角色：直接分配 + 候选人，两者由 Flowable 内部以 OR 组合
            query.taskCandidateOrAssigned(userId);
        } else {
            // 注意：taskCandidateOrAssigned 内部已自带 OR，不能再嵌进 or()/endOr()，
            // 否则会生成 "AND (assignee = ? OR (candidate ...))" 之类的错误优先级，
            // 导致候选人任务查不出来。这里展开成扁平的三路 OR。
            query.or()
                    .taskAssignee(userId)
                    .taskCandidateUser(userId)
                    .taskCandidateGroupIn(groupIds)
                    .endOr();
        }
        query.taskTenantId(tenantId);
        if (StringUtils.isNotBlank(queryVO.getProcessDefinitionKey())) {
            query.processDefinitionKey(queryVO.getProcessDefinitionKey());
        }
        if (StringUtils.isNotBlank(queryVO.getTitle())) {
            query.taskNameLike("%" + queryVO.getTitle() + "%");
        }
        return query;
    }

    /**
     * 取当前用户所属的角色 ID 集合，作为候选组标识。
     * <p>登录态中的 {@code roleIds} 未必被填充，这里直接从用户角色关系表读取，保证候选组可用。</p>
     */
    private List<String> resolveUserGroupIds() {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            return Collections.emptyList();
        }
        List<SystemUserRoleDO> userRoles = systemUserRoleMapper.selectList(
                new LambdaQueryWrapper<SystemUserRoleDO>()
                        .eq(SystemUserRoleDO::getUserId, userId));
        if (userRoles == null || userRoles.isEmpty()) {
            return Collections.emptyList();
        }
        return userRoles.stream()
                .map(SystemUserRoleDO::getRoleId)
                .filter(Objects::nonNull)
                .map(String::valueOf)
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * 解析流程的发起节点 ID。
     * <p>优先取 Handler 声明；Handler 未声明（兜底对象）时按历史中最早的用户任务推断。</p>
     */
    private String resolveStartActivityId(String processInstanceId, AbstractWorkflowHandler handler) {
        String declared = handler.getStartActivityId();
        if (declared != null && !declared.isEmpty()) {
            return declared;
        }
        List<HistoricTaskInstance> firstTask = historyService.createHistoricTaskInstanceQuery()
                .processInstanceId(processInstanceId)
                .orderByHistoricTaskInstanceStartTime().asc()
                .listPage(0, 1);
        return firstTask.isEmpty() ? null : firstTask.get(0).getTaskDefinitionKey();
    }

    private Task getAndValidateTask(String taskId) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            throw new ServiceException(SystemErrorEnum.TASK_NOT_FOUND);
        }
        String currentUserId = String.valueOf(SecurityUtils.getUserId());
        if (!currentUserId.equals(task.getAssignee()) && !isCandidateOf(task, currentUserId)) {
            throw new ServiceException(SystemErrorEnum.TASK_NOT_ASSIGNEE);
        }
        return task;
    }

    /**
     * 判断当前用户是否为该任务的候选人/候选组成员。
     * <p>
     * 仅校验 assignee 会让“候选人任务”无人可办：任务 assignee 为空、归属记录在
     * ACT_RU_IDENTITYLINK，此处必须一并认可，否则候选人即使看到任务也无法审批。
     * </p>
     */
    private boolean isCandidateOf(Task task, String userId) {
        List<String> groupIds = resolveUserGroupIds();
        TaskQuery query = taskService.createTaskQuery().taskId(task.getId());
        if (groupIds.isEmpty()) {
            query.taskCandidateOrAssigned(userId);
        } else {
            query.or()
                    .taskAssignee(userId)
                    .taskCandidateUser(userId)
                    .taskCandidateGroupIn(groupIds)
                    .endOr();
        }
        return !query.list().isEmpty();
    }

    /**
     * 判断目标节点是否为流程的发起节点（退回发起人）。
     * <p>
     * 优先采用 Handler 声明的 {@code startActivityId}；当业务方未实现 Handler
     * （工厂返回兜底对象，其 {@code getStartActivityId()} 为 null）时，
     * 退化为按 BPMN 结构推断：发起节点即“流程启动后第一个产生用户任务的节点”。
     * 这样即使没有 Handler，退回到发起人也能正确回写“已驳回”状态。
     * </p>
     */
    private boolean isStartActivity(Task task, String targetActivityId) {
        if (targetActivityId == null || targetActivityId.isEmpty()) {
            return false;
        }
        String processDefinitionKey = querySupport.getInstanceByProcessId(
                task.getProcessInstanceId()).getProcessDefinitionKey();
        AbstractWorkflowHandler handler = handlerFactory.getHandler(processDefinitionKey);
        String startActivityId = resolveStartActivityId(task.getProcessInstanceId(), handler);
        return targetActivityId.equals(startActivityId);
    }

    /**
     * 安全解析用户 ID。
     * <p>
     * BPMN 里的 assignee 可能配置成用户名或表达式结果，未必是数字。
     * 直接 {@code Long.valueOf} 会抛 NumberFormatException 变成 500，
     * 而事件通知属于“尽力而为”的旁路逻辑，解析失败应跳过而不是中断主流程。
     * </p>
     *
     * @return 解析成功返回用户 ID，非数字或为空返回 {@code null}
     */
    private Long parseUserId(String rawUserId) {
        if (StringUtils.isBlank(rawUserId)) {
            return null;
        }
        String trimmed = rawUserId.trim();
        if (!StringUtils.isNumeric(trimmed)) {
            log.debug("[工作流] 用户ID非数字，跳过事件通知：{}", rawUserId);
            return null;
        }
        try {
            return Long.valueOf(trimmed);
        } catch (NumberFormatException e) {
            log.debug("[工作流] 用户ID超出范围，跳过事件通知：{}", rawUserId);
            return null;
        }
    }

    private WorkflowContextDTO buildContext(Task task, WorkflowInstanceDO instance) {
        WorkflowContextDTO context = new WorkflowContextDTO();
        context.setProcessInstanceId(task.getProcessInstanceId());
        context.setProcessDefinitionKey(instance.getProcessDefinitionKey());
        context.setBusinessKey(instance.getBusinessKey());
        context.setTaskId(task.getId());
        context.setTaskDefinitionKey(task.getTaskDefinitionKey());
        context.setOperatorUserId(SecurityUtils.getUserId());
        context.setOperatorUsername(SecurityUtils.getUsername());
        context.setTenantId(instance.getTenantId());
        return context;
    }
}
