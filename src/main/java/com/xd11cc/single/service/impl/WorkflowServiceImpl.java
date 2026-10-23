package com.xd11cc.single.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xd11cc.single.common.exception.ServiceException;
import com.xd11cc.single.modules.workflow.AbstractWorkflowHandler;
import com.xd11cc.single.modules.workflow.WorkflowEvent;
import com.xd11cc.single.modules.workflow.WorkflowHandlerFactory;
import com.xd11cc.single.entity.base.PageResult;
import com.xd11cc.single.entity.domain.SystemUserRoleDO;
import com.xd11cc.single.entity.domain.WorkflowInstanceDO;
import com.xd11cc.single.entity.dto.WorkflowContextDTO;
import com.xd11cc.single.entity.vo.*;
import com.xd11cc.single.enums.SystemErrorEnum;
import com.xd11cc.single.enums.WorkflowStatusEnum;
import com.xd11cc.single.mapper.SystemUserRoleMapper;
import com.xd11cc.single.mapper.WorkflowInstanceMapper;
import com.xd11cc.single.service.WorkflowService;
import com.xd11cc.single.service.WorkflowExpressionResolver;
import com.xd11cc.single.utils.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.flowable.bpmn.converter.BpmnXMLConverter;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.Process;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.Model;
import org.flowable.engine.repository.ModelQuery;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.flowable.task.api.TaskQuery;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author xd11cc
 * @date 2026-08-18
 */
@Slf4j
@Service
public class WorkflowServiceImpl implements WorkflowService {

    @Autowired
    private RepositoryService repositoryService;
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
    private WorkflowExpressionResolver expressionResolver;
    @Autowired
    private SystemUserRoleMapper systemUserRoleMapper;

    // ==================== 流程定义管理 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WorkflowModelVO saveDraft(WorkflowDraftSaveVO saveVO) {
        Long userId = SecurityUtils.getUserId();
        Long tenantId = SecurityUtils.getTenantId();

        Model model;
        if (saveVO.getModelId() != null && !saveVO.getModelId().isEmpty()) {
            model = repositoryService.getModel(saveVO.getModelId());
            if (model == null || !tenantId.toString().equals(model.getTenantId())) {
                throw new ServiceException(SystemErrorEnum.PROCESS_DEFINITION_NOT_FOUND);
            }
        } else {
            model = repositoryService.newModel();
            model.setTenantId(String.valueOf(tenantId));
        }

        String key = saveVO.getKey();
        if (key == null || key.isEmpty()) {
            key = model.getKey();
        }
        if (key == null || key.isEmpty()) {
            key = "flow_" + System.currentTimeMillis();
        }

        model.setName(saveVO.getName());
        model.setKey(key);
        model.setCategory(saveVO.getCategory());
        // Flowable Model 没有 description 字段，用 metaInfo 存描述信息
        if (saveVO.getDescription() != null) {
            model.setMetaInfo(saveVO.getDescription());
        }

        repositoryService.saveModel(model);

        String resolvedEditorSource = expressionResolver.resolve(saveVO.getEditorSource(), false);
        byte[] xmlBytes = resolvedEditorSource.getBytes(StandardCharsets.UTF_8);
        repositoryService.addModelEditorSource(model.getId(), xmlBytes);
        if (saveVO.getEditorSourceExtraValue() != null) {
            repositoryService.addModelEditorSourceExtra(model.getId(),
                    saveVO.getEditorSourceExtraValue().getBytes(StandardCharsets.UTF_8));
        }

        return toModelVO(model);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String deployDraft(WorkflowDraftDeployVO deployVO) {
        Long tenantId = SecurityUtils.getTenantId();

        Model model = repositoryService.getModel(deployVO.getModelId());
        if (model == null || !tenantId.toString().equals(model.getTenantId())) {
            throw new ServiceException(SystemErrorEnum.PROCESS_DEFINITION_NOT_FOUND);
        }
        if (model.getDeploymentId() != null) {
            throw new ServiceException(SystemErrorEnum.PROCESS_ALREADY_COMPLETED);
        }

        // 校验 Model.key 与 BPMN 中的 processId 一致
        validateBpmnProcessId(model);

        List<ProcessDefinition> currentActiveDefinitions = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey(model.getKey())
                .processDefinitionTenantId(model.getTenantId())
                .active().list();
        if (currentActiveDefinitions.size() > 1) {
            throw new ServiceException(SystemErrorEnum.MULTIPLE_ACTIVE_DEFINITION);
        }

        byte[] editorSource = repositoryService.getModelEditorSource(model.getId());
        String resolvedXml = expressionResolver.resolve(new String(editorSource, StandardCharsets.UTF_8), true);
        editorSource = resolvedXml.getBytes(StandardCharsets.UTF_8);
        repositoryService.addModelEditorSource(model.getId(), editorSource);
        Deployment deployment = repositoryService.createDeployment()
                .name(model.getName())
                .category(model.getCategory())
                .addBytes(model.getKey() + ".bpmn20.xml", editorSource)
                .tenantId(model.getTenantId())
                .deploy();

        ProcessDefinition newDefinition = repositoryService.createProcessDefinitionQuery()
                .deploymentId(deployment.getId()).singleResult();
        for (ProcessDefinition oldDefinition : currentActiveDefinitions) {
            if (newDefinition == null || !oldDefinition.getId().equals(newDefinition.getId())) {
                repositoryService.suspendProcessDefinitionById(oldDefinition.getId(), false, null);
            }
        }

        // 回填 deploymentId 到 Model
        model.setDeploymentId(deployment.getId());
        repositoryService.saveModel(model);

        return deployment.getId();
    }

    @Override
    public PageResult<WorkflowModelVO> getModelPage(WorkflowDraftQueryVO queryVO) {
        String tenantId = String.valueOf(SecurityUtils.getTenantId());

        ModelQuery modelQuery = repositoryService.createModelQuery()
                .modelTenantId(tenantId)
                .orderByCreateTime().desc();

        if (queryVO.getName() != null && !queryVO.getName().isEmpty()) {
            modelQuery.modelNameLike("%" + queryVO.getName() + "%");
        }
        if (queryVO.getKey() != null && !queryVO.getKey().isEmpty()) {
            modelQuery.modelKey(queryVO.getKey());
        }
        if (queryVO.getCategory() != null && !queryVO.getCategory().isEmpty()) {
            modelQuery.modelCategoryLike("%" + queryVO.getCategory() + "%");
        }
        if (queryVO.getStatus() != null) {
            if ("DRAFT".equals(queryVO.getStatus())) {
                modelQuery.notDeployed();
            } else {
                modelQuery.deployed();
            }
        }

        long count = modelQuery.count();
        if (count == 0) {
            return PageResult.empty();
        }

        List<Model> models = modelQuery
                .listPage((queryVO.getCurrentPage() - 1) * queryVO.getPageSize(), queryVO.getPageSize());

        List<WorkflowModelVO> list = models.stream()
                .map(this::toModelVO)
                .collect(Collectors.toList());

        return new PageResult<>(list, count);
    }

    @Override
    public WorkflowModelVO getModelDetail(String modelId) {
        Long tenantId = SecurityUtils.getTenantId();
        Model model = repositoryService.getModel(modelId);
        if (model == null || !tenantId.toString().equals(model.getTenantId())) {
            throw new ServiceException(SystemErrorEnum.PROCESS_DEFINITION_NOT_FOUND);
        }
        WorkflowModelVO vo = toModelVO(model);
        byte[] editorSource = repositoryService.getModelEditorSource(modelId);
        byte[] editorExtra = repositoryService.getModelEditorSourceExtra(modelId);
        vo.setEditorSource(editorSource == null ? null : new String(editorSource, StandardCharsets.UTF_8));
        vo.setEditorSourceExtraValue(editorExtra == null ? null : new String(editorExtra, StandardCharsets.UTF_8));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeDraft(String modelId) {
        Long tenantId = SecurityUtils.getTenantId();
        Model model = repositoryService.getModel(modelId);
        if (model == null || !tenantId.toString().equals(model.getTenantId())) {
            throw new ServiceException(SystemErrorEnum.PROCESS_DEFINITION_NOT_FOUND);
        }
        if (model.getDeploymentId() != null) {
            throw new ServiceException(SystemErrorEnum.PROCESS_ALREADY_COMPLETED);
        }
        repositoryService.deleteModel(modelId);
    }

    @Override
    public void suspendDefinition(String processDefinitionId) {
        ProcessDefinition definition = getTenantProcessDefinition(processDefinitionId);
        repositoryService.suspendProcessDefinitionById(definition.getId(), false, null);
    }

    @Override
    public void activateDefinition(String processDefinitionId) {
        ProcessDefinition target = getTenantProcessDefinition(processDefinitionId);
        List<ProcessDefinition> activeDefinitions = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey(target.getKey())
                .processDefinitionTenantId(target.getTenantId())
                .active().list();
        for (ProcessDefinition definition : activeDefinitions) {
            if (!definition.getId().equals(target.getId())) {
                repositoryService.suspendProcessDefinitionById(definition.getId(), false, null);
            }
        }
        repositoryService.activateProcessDefinitionById(target.getId(), false, null);
    }

    @Override
    public List<WorkflowDefinitionVersionVO> getDefinitionVersions(String key) {
        String tenantId = String.valueOf(SecurityUtils.getTenantId());
        List<ProcessDefinition> definitions = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey(key)
                .processDefinitionTenantId(tenantId)
                .orderByProcessDefinitionVersion().desc()
                .list();
        long activeCount = definitions.stream().filter(definition -> !definition.isSuspended()).count();
        return definitions.stream().map(definition -> {
            WorkflowDefinitionVersionVO vo = new WorkflowDefinitionVersionVO();
            vo.setProcessDefinitionId(definition.getId());
            vo.setDeploymentId(definition.getDeploymentId());
            vo.setKey(definition.getKey());
            vo.setName(definition.getName());
            vo.setCategory(definition.getCategory());
            vo.setVersion(definition.getVersion());
            vo.setSuspended(definition.isSuspended());
            vo.setStatus(definition.isSuspended() ? "SUSPENDED" : (activeCount > 1 ? "CONFLICT" : "ACTIVE"));
            Deployment deployment = repositoryService.createDeploymentQuery()
                    .deploymentId(definition.getDeploymentId()).singleResult();
            if (deployment != null) vo.setDeployTime(deployment.getDeploymentTime());
            return vo;
        }).collect(Collectors.toList());
    }

    private ProcessDefinition getTenantProcessDefinition(String processDefinitionId) {
        ProcessDefinition definition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionId(processDefinitionId)
                .processDefinitionTenantId(String.valueOf(SecurityUtils.getTenantId()))
                .singleResult();
        if (definition == null) throw new ServiceException(SystemErrorEnum.PROCESS_DEFINITION_NOT_FOUND);
        return definition;
    }
    // ==================== 流程实例生命周期 ====================
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelProcess(String processInstanceId, String reason) {
        WorkflowInstanceDO instance = getInstanceByProcessId(processInstanceId);

        if (!instance.getStarterUserId().equals(SecurityUtils.getUserId())) {
            throw new ServiceException(SystemErrorEnum.TASK_NOT_ASSIGNEE);
        }
        if (instance.getStatus() != WorkflowStatusEnum.RUNNING.getCode()
                && instance.getStatus() != WorkflowStatusEnum.REJECTED.getCode()) {
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
        WorkflowInstanceDO instance = getInstanceByProcessId(processInstanceId);

        WorkflowInstanceDetailVO detail = convertToInstanceDetailVO(instance);

        // 查询完整审批历史（任务级）
        List<HistoricTaskInstance> historyTasks = historyService.createHistoricTaskInstanceQuery()
                .processInstanceId(processInstanceId)
                .orderByHistoricTaskInstanceStartTime().asc()
                .list();

        // 审批意见必须先按流程实例整体取出再按 taskId 分组：
        // taskService.getTaskComments(taskId) 只查运行时注释，流程结束或任务已删除后取不到历史意见。
        Map<String, org.flowable.engine.task.Comment> latestCommentByTask =
                getLatestCommentsByTask(processInstanceId);

        List<WorkflowTaskDetailVO> taskHistory = historyTasks.stream()
                .map(ht -> {
                    WorkflowTaskDetailVO vo = convertHistoricToTaskDetailVO(ht);
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
                .map(this::convertToTaskDetailVO)
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
        Long tenantId = SecurityUtils.getTenantId();

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

    // ==================== 任务操作 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approveTask(WorkflowTaskApproveVO approveVO) {
        Task task = getAndValidateTask(approveVO.getTaskId());

        String processInstanceId = task.getProcessInstanceId();
        WorkflowInstanceDO instance = getInstanceByProcessId(processInstanceId);
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
        WorkflowInstanceDO instance = getInstanceByProcessId(processInstanceId);
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
            throw new ServiceException(SystemErrorEnum.DELEGATE_TARGET_USER_INVALID, new Object[]{delegateVO.getTargetUserId()});
        }

        Task task = getAndValidateTask(delegateVO.getTaskId());

        String processInstanceId = task.getProcessInstanceId();
        WorkflowInstanceDO instance = getInstanceByProcessId(processInstanceId);

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

    @Override
    public List<WorkflowActivityVO> getRejectableNodes(String taskId) {
        Task task = getAndValidateTask(taskId);
        String processInstanceId = task.getProcessInstanceId();
        WorkflowInstanceDO instance = getInstanceByProcessId(processInstanceId);
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

    @Override
    public PageResult<WorkflowTaskDetailVO> getMyTodoTasks(WorkflowTaskQueryVO queryVO) {
        String userId = String.valueOf(SecurityUtils.getUserId());
        String tenantId = String.valueOf(SecurityUtils.getTenantId());

        long count = buildMyTodoTaskQuery(userId, tenantId, queryVO).count();
        if (count == 0) {
            return PageResult.empty();
        }

        List<Task> tasks = buildMyTodoTaskQuery(userId, tenantId, queryVO)
                .orderByTaskCreateTime().desc()
                .listPage((queryVO.getCurrentPage() - 1) * queryVO.getPageSize(), queryVO.getPageSize());

        List<WorkflowTaskDetailVO> list = tasks.stream()
                .map(this::convertToTaskDetailVO)
                .collect(Collectors.toList());

        return new PageResult<>(list, count);
    }

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

    @Override
    public PageResult<WorkflowTaskDetailVO> getMyDoneTasks(WorkflowTaskQueryVO queryVO) {
        String userId = String.valueOf(SecurityUtils.getUserId());
        String tenantId = String.valueOf(SecurityUtils.getTenantId());

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
                .map(this::convertHistoricToTaskDetailVO)
                .collect(Collectors.toList());

        // 补齐每条已办任务的审批意见：按流程实例批量取，避免逐条查询
        Map<String, Map<String, org.flowable.engine.task.Comment>> commentCache = new HashMap<>();
        for (WorkflowTaskDetailVO vo : list) {
            Map<String, org.flowable.engine.task.Comment> byTask = commentCache.computeIfAbsent(
                    vo.getProcessInstanceId(), this::getLatestCommentsByTask);
            org.flowable.engine.task.Comment comment = byTask.get(vo.getTaskId());
            if (comment != null) {
                vo.setComment(comment.getFullMessage());
                vo.setCommentType(comment.getType());
            }
        }

        return new PageResult<>(list, count);
    }

    // ==================== 私有方法 ====================

    private void validateBpmnProcessId(Model model) {
        byte[] editorSource = repositoryService.getModelEditorSource(model.getId());
        if (editorSource == null || editorSource.length == 0) {
            throw new ServiceException(SystemErrorEnum.PROCESS_RESOURCE_READ_FAILED);
        }
        BpmnModel bpmnModel;
        try {
            BpmnXMLConverter converter = new BpmnXMLConverter();
            javax.xml.stream.XMLInputFactory factory = javax.xml.stream.XMLInputFactory.newInstance();
            javax.xml.stream.XMLStreamReader reader = factory.createXMLStreamReader(
                    new ByteArrayInputStream(editorSource));
            bpmnModel = converter.convertToBpmnModel(reader);
        } catch (Exception e) {
            // 只有“解析失败”这一种情况才包装，避免把下方的业务校验异常一并吞掉
            throw new ServiceException(SystemErrorEnum.DEPLOY_FAILED, e);
        }

        List<Process> processes = bpmnModel.getProcesses();
        if (processes.isEmpty() || processes.size() > 1) {
            throw new ServiceException(SystemErrorEnum.BPMN_PROCESS_COUNT_INVALID,
                    new Object[]{processes.size()});
        }
        String bpmnProcessId = processes.get(0).getId();
        if (!model.getKey().equals(bpmnProcessId)) {
            throw new ServiceException(SystemErrorEnum.BPMN_PROCESS_ID_MISMATCH,
                    new Object[]{model.getKey(), bpmnProcessId});
        }
    }

    private WorkflowModelVO toModelVO(Model model) {
        WorkflowModelVO vo = new WorkflowModelVO();
        vo.setId(model.getId());
        vo.setName(model.getName());
        vo.setKey(model.getKey());
        vo.setCategory(model.getCategory());
        vo.setDeploymentId(model.getDeploymentId());
        vo.setVersion(model.getVersion());
        fillDefinitionStatus(vo, model);
        vo.setCreateTime(model.getCreateTime());
        vo.setLastUpdateTime(model.getLastUpdateTime());
        // Flowable Model 无 description 字段，描述信息存于 metaInfo
        vo.setDescription(model.getMetaInfo());
        return vo;
    }

    private void fillDefinitionStatus(WorkflowModelVO vo, Model model) {
        if (model.getDeploymentId() == null) {
            vo.setStatus("DRAFT");
            vo.setSuspended(false);
            return;
        }

        ProcessDefinition definition = repositoryService.createProcessDefinitionQuery()
                .deploymentId(model.getDeploymentId())
                .processDefinitionTenantId(model.getTenantId())
                .singleResult();
        if (definition == null) {
            vo.setStatus("CONFLICT");
            return;
        }

        vo.setProcessDefinitionId(definition.getId());
        vo.setVersion(definition.getVersion());
        vo.setSuspended(definition.isSuspended());
        Deployment deployment = repositoryService.createDeploymentQuery()
                .deploymentId(model.getDeploymentId())
                .singleResult();
        if (deployment != null) vo.setDeployTime(deployment.getDeploymentTime());

        long activeCount = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey(definition.getKey())
                .processDefinitionTenantId(definition.getTenantId())
                .active()
                .count();
        if (definition.isSuspended()) {
            vo.setStatus("SUSPENDED");
        } else if (activeCount > 1) {
            vo.setStatus("CONFLICT");
        } else {
            vo.setStatus("ACTIVE");
        }
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
        String processDefinitionKey = getInstanceByProcessId(
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

    private WorkflowInstanceDO getInstanceByProcessId(String processInstanceId) {
        WorkflowInstanceDO instance = workflowInstanceMapper.selectOne(
                new LambdaQueryWrapper<WorkflowInstanceDO>()
                        .eq(WorkflowInstanceDO::getProcessInstanceId, processInstanceId));
        if (instance == null) {
            throw new ServiceException(SystemErrorEnum.PROCESS_INSTANCE_NOT_FOUND);
        }
        return instance;
    }

    /**
     * 按流程实例取回全部审批意见，并按 taskId 归并出“最后一条”。
     * <p>
     * 不能对每个历史任务单独调用 {@code taskService.getTaskComments(taskId)}：
     * 该方法只查运行时注释表，流程结束后或任务被删除后返回空列表，
     * 会导致审批意见在流程详情中永远为 null。
     * </p>
     */
    private Map<String, org.flowable.engine.task.Comment> getLatestCommentsByTask(String processInstanceId) {
        List<org.flowable.engine.task.Comment> comments =
                taskService.getProcessInstanceComments(processInstanceId);
        Map<String, org.flowable.engine.task.Comment> latest = new HashMap<>();
        if (comments == null || comments.isEmpty()) {
            return latest;
        }
        // 按时间正序覆盖，保证最终留下的是最新一条
        comments.stream()
                .filter(comment -> comment.getTaskId() != null)
                .sorted(Comparator.comparing(org.flowable.engine.task.Comment::getTime,
                        Comparator.nullsFirst(Comparator.naturalOrder())))
                .forEach(comment -> latest.put(comment.getTaskId(), comment));
        return latest;
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

    private WorkflowTaskDetailVO convertToTaskDetailVO(Task task) {
        WorkflowTaskDetailVO vo = new WorkflowTaskDetailVO();
        vo.setTaskId(task.getId());
        vo.setTaskName(task.getName());
        vo.setTaskDefinitionKey(task.getTaskDefinitionKey());
        vo.setAssignee(task.getAssignee());
        vo.setProcessInstanceId(task.getProcessInstanceId());
        vo.setCreateTime(task.getCreateTime());

        WorkflowInstanceDO instance = workflowInstanceMapper.selectOne(
                new LambdaQueryWrapper<WorkflowInstanceDO>()
                        .eq(WorkflowInstanceDO::getProcessInstanceId, task.getProcessInstanceId()));
        if (instance != null) {
            vo.setProcessDefinitionKey(instance.getProcessDefinitionKey());
            vo.setBusinessKey(instance.getBusinessKey());
            vo.setTitle(instance.getTitle());
        }
        return vo;
    }

    private WorkflowTaskDetailVO convertHistoricToTaskDetailVO(HistoricTaskInstance ht) {
        WorkflowTaskDetailVO vo = new WorkflowTaskDetailVO();
        vo.setTaskId(ht.getId());
        vo.setTaskName(ht.getName());
        vo.setTaskDefinitionKey(ht.getTaskDefinitionKey());
        vo.setAssignee(ht.getAssignee());
        vo.setProcessInstanceId(ht.getProcessInstanceId());
        vo.setCreateTime(ht.getCreateTime());
        vo.setEndTime(ht.getEndTime());

        // 已办列表需要展示流程标题/业务键，否则前端这些列为空
        WorkflowInstanceDO instance = workflowInstanceMapper.selectOne(
                new LambdaQueryWrapper<WorkflowInstanceDO>()
                        .eq(WorkflowInstanceDO::getProcessInstanceId, ht.getProcessInstanceId()));
        if (instance != null) {
            vo.setProcessDefinitionKey(instance.getProcessDefinitionKey());
            vo.setBusinessKey(instance.getBusinessKey());
            vo.setTitle(instance.getTitle());
        }
        return vo;
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
