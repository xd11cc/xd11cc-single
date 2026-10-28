package com.xd11cc.single.modules.workflow;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xd11cc.single.common.exception.ServiceException;
import com.xd11cc.single.entity.domain.WorkflowInstanceDO;
import com.xd11cc.single.entity.vo.WorkflowTaskDetailVO;
import com.xd11cc.single.enums.SystemErrorEnum;
import com.xd11cc.single.mapper.WorkflowInstanceMapper;
import org.flowable.engine.TaskService;
import org.flowable.task.api.Task;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 工作流跨模块共享的查询与视图装配。
 * <p>
 * <b>这是模块内部组件，不属于对外接口。</b>
 * 它承载两类被「流程实例」与「任务」两侧共同需要的能力：
 * <ol>
 *   <li>按流程实例 ID 取业务侧的 {@link WorkflowInstanceDO}</li>
 *   <li>把 Flowable 的 {@link Task} / {@link HistoricTaskInstance} 装配成 {@link WorkflowTaskDetailVO}</li>
 * </ol>
 * </p>
 *
 * <p>
 * 之所以独立成类而不放进某一侧：`getInstanceByProcessId` 属实例概念，
 * 而三个视图装配方法属任务概念。强行塞进任一侧，都会让该模块承担不属于自己的职责 ——
 * 这正是把 968 行单体拆分掉要消除的问题。
 * </p>
 *
 * <p>
 * <b>使用纪律</b>：只允许 {@code modules.workflow} 内部使用。
 * 如果它开始膨胀（例如超过 200 行），说明「实例」与「任务」两个模块本不该拆开，
 * 此时应该合并这两个模块，而不是继续往这里塞方法。
 * </p>
 *
 * @author xd11cc
 */
@Component
public class WorkflowQuerySupport {

    @Autowired
    private WorkflowInstanceMapper workflowInstanceMapper;

    @Autowired
    private TaskService taskService;

    /**
     * 按流程实例 ID 取业务侧实例记录，不存在时抛异常。
     */
    public WorkflowInstanceDO getInstanceByProcessId(String processInstanceId) {
        WorkflowInstanceDO instance = workflowInstanceMapper.selectOne(
                new LambdaQueryWrapper<WorkflowInstanceDO>()
                        .eq(WorkflowInstanceDO::getProcessInstanceId, processInstanceId));
        if (instance == null) {
            throw new ServiceException(SystemErrorEnum.PROCESS_INSTANCE_NOT_FOUND);
        }
        return instance;
    }

    /**
     * 按流程实例取回全部审批意见，并按 taskId 归并出「最后一条」。
     * <p>
     * 不能对每个历史任务单独调用 {@code taskService.getTaskComments(taskId)}：
     * 该方法只查运行时注释表，流程结束后或任务被删除后返回空列表，
     * 会导致审批意见在流程详情中永远为 null。
     * </p>
     */
    public Map<String, org.flowable.engine.task.Comment> getLatestCommentsByTask(String processInstanceId) {
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

    /**
     * 运行时任务 → 详情 VO（补齐流程标题、业务键）。
     */
    public WorkflowTaskDetailVO convertToTaskDetailVO(Task task) {
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

    /**
     * 历史任务 → 详情 VO（已办列表需要展示流程标题/业务键，否则前端这些列为空）。
     */
    public WorkflowTaskDetailVO convertHistoricToTaskDetailVO(HistoricTaskInstance ht) {
        WorkflowTaskDetailVO vo = new WorkflowTaskDetailVO();
        vo.setTaskId(ht.getId());
        vo.setTaskName(ht.getName());
        vo.setTaskDefinitionKey(ht.getTaskDefinitionKey());
        vo.setAssignee(ht.getAssignee());
        vo.setProcessInstanceId(ht.getProcessInstanceId());
        vo.setCreateTime(ht.getCreateTime());
        vo.setEndTime(ht.getEndTime());

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
}
