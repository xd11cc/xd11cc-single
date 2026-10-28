package com.xd11cc.single.modules.workflow;

import com.xd11cc.single.entity.base.PageResult;
import com.xd11cc.single.entity.vo.WorkflowActivityVO;
import com.xd11cc.single.entity.vo.WorkflowTaskApproveVO;
import com.xd11cc.single.entity.vo.WorkflowTaskDelegateVO;
import com.xd11cc.single.entity.vo.WorkflowTaskDetailVO;
import com.xd11cc.single.entity.vo.WorkflowTaskQueryVO;
import com.xd11cc.single.entity.vo.WorkflowTaskRejectVO;
import org.springframework.lang.NonNull;

import java.util.List;

/**
 * 任务服务：办理人视角的待办 / 已办 / 审批动作。
 * <p>
 * 与 {@link WorkflowInstanceService} 的分工：本模块关心「某一步谁来办」，
 * 实例模块关心「一个流程整体」。审批会推进实例状态，但状态迁移的副作用
 * 由本模块负责，实例模块只负责展示与取消。
 * </p>
 *
 * @author xd11cc
 */
public interface WorkflowTaskService {

    /**
     * 审批通过。
     * <p>若推进后流程结束，会置实例为已完成并发布 completed 事件；
     * 否则为每个产生的新任务发布 approved 事件。</p>
     */
    void approveTask(@NonNull WorkflowTaskApproveVO approveVO);

    /**
     * 驳回（退回到指定节点）。
     * <p>退回到发起节点时，实例状态置为「已驳回」。</p>
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
