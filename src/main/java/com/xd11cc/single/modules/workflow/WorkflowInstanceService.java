package com.xd11cc.single.modules.workflow;

import com.xd11cc.single.entity.base.PageResult;
import com.xd11cc.single.entity.vo.WorkflowInstanceDetailVO;
import com.xd11cc.single.entity.vo.WorkflowInstanceQueryVO;
import org.springframework.lang.NonNull;

/**
 * 流程实例服务：已发起流程的生命周期与查询。
 * <p>
 * 与 {@link WorkflowTaskService} 的分工：本模块关心「一个流程整体」的状态与展示，
 * 任务模块关心「某一步谁来办」。实例详情需要聚合任务信息，
 * 因此两侧共享 {@link WorkflowQuerySupport}。
 * </p>
 *
 * @author xd11cc
 */
public interface WorkflowInstanceService {

    /**
     * 取消/撤回流程（仅发起人可操作，且仅进行中或已驳回状态可取消）。
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
}
