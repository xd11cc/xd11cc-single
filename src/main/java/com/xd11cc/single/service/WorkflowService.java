package com.xd11cc.single.service;

import com.xd11cc.single.entity.base.PageResult;
import com.xd11cc.single.entity.vo.*;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Map;

/**
 * 工作流服务接口。
 * <p>
 * 统一入口，按职责由三个 Controller 消费：
 * <ul>
 *   <li>{@link com.xd11cc.single.controller.WorkflowDefinitionController} — 流程定义管理</li>
 *   <li>{@link com.xd11cc.single.controller.WorkflowProcessController} — 流程实例生命周期</li>
 *   <li>{@link com.xd11cc.single.controller.WorkflowTaskController} — 待办/已办任务</li>
 * </ul>
 * </p>
 *
 * @author xd11cc
 * @date 2026-08-18
 */
public interface WorkflowService {

    // ==================== 流程定义管理（Designer + 管理员） ====================

    /**
     * 保存流程草稿。
     * <p>modelId 为空时新建草稿，否则更新已有草稿。</p>
     */
    WorkflowModelVO saveDraft(@NonNull WorkflowDraftSaveVO saveVO);

    /**
     * 将草稿部署为正式流程定义。
     * <p>部署成功后会生成 deploymentId 和 processDefinitionKey，
     * 草稿状态由 0 变为 1。</p>
     */
    String deployDraft(@NonNull WorkflowDraftDeployVO deployVO);

    /**
     * 分页查询模型（草稿 + 已部署）。
     * <p>status 为 null 时查全部，0=草稿，1=已部署。</p>
     */
    PageResult<WorkflowModelVO> getModelPage(@NonNull WorkflowDraftQueryVO queryVO);

    /**
     * 模型详情。
     */
    WorkflowModelVO getModelDetail(@NonNull String modelId);

    /**
     * 删除草稿（只能删除未部署的草稿）。
     */
    void removeDraft(@NonNull String modelId);

    /**
     * 挂起流程定义（已部署状态）。
     * <p>已有实例继续运行，但不再允许发起新实例。</p>
     */
    void suspendDefinition(@NonNull String processDefinitionId);

    /**
     * 激活流程定义。
     */
    void activateDefinition(@NonNull String processDefinitionId);

    /** 查询当前租户下指定流程标识的全部已部署版本。 */
    List<WorkflowDefinitionVersionVO> getDefinitionVersions(@NonNull String key);

    /**
     * 复制流程定义为新部署。
     */
    String copyDefinition(@NonNull WorkflowCopyVO copyVO);

    // ==================== 流程实例生命周期 ====================

    /**
     * 发起流程。
     * <p>businessKey 建议格式：业务类型:业务ID，用于和业务系统关联。</p>
     */
    String startProcess(@NonNull WorkflowStartVO startVO);

    /**
     * 取消/撤回流程（仅发起人可操作）。
     */
    void cancelProcess(@NonNull String processInstanceId, String reason);

    /**
     * 流程实例详情（含审批历史 + 当前活动任务）。
     */
    WorkflowInstanceDetailVO getProcessDetail(@NonNull String processInstanceId);

    /**
     * 我发起的流程列表。
     */
    PageResult<WorkflowInstanceDetailVO> getMyStartedProcesses(@NonNull WorkflowInstanceQueryVO queryVO);

    /**
     * 流程实例分页（管理员视角，支持按发起人/状态/标题/流程定义筛选）。
     */
    PageResult<WorkflowInstanceDetailVO> getProcessPage(@NonNull WorkflowInstanceQueryVO queryVO);

    /**
     * 历史活动节点列表（流程轨迹）。
     */
    PageResult<WorkflowModelActivityVO> getHistoricActivities(@NonNull String processInstanceId,
                                                              Integer pageNo, Integer pageSize);

    /**
     * 获取运行时流程变量。
     */
    List<WorkflowProcessVariableVO> getProcessVariables(@NonNull String processInstanceId);

    /**
     * 更新/新增运行时流程变量。
     */
    void updateProcessVariables(@NonNull String processInstanceId, @NonNull Map<String, Object> variables);

    // ==================== 任务操作 ====================

    /**
     * 审批通过。
     */
    void approveTask(@NonNull WorkflowTaskApproveVO approveVO);

    /**
     * 驳回（退回到指定节点）。
     */
    void rejectTask(@NonNull WorkflowTaskRejectVO rejectVO);

    /**
     * 转办。
     */
    void delegateTask(@NonNull WorkflowTaskDelegateVO delegateVO);

    /**
     * 获取可退回节点列表。
     */
    List<WorkflowActivityVO> getRejectableNodes(@NonNull String taskId);

    /**
     * 我的待办列表。
     */
    PageResult<WorkflowTaskDetailVO> getMyTodoTasks(@NonNull WorkflowTaskQueryVO queryVO);

    /**
     * 我的已办列表。
     */
    PageResult<WorkflowTaskDetailVO> getMyDoneTasks(@NonNull WorkflowTaskQueryVO queryVO);
}
