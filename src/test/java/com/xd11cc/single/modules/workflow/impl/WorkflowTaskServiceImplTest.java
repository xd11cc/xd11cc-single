package com.xd11cc.single.modules.workflow.impl;

import com.xd11cc.single.common.context.TenantContextHolder;
import com.xd11cc.single.common.exception.ServiceException;
import com.xd11cc.single.entity.domain.SystemUserDO;
import com.xd11cc.single.entity.domain.WorkflowInstanceDO;
import com.xd11cc.single.entity.dto.LoginUserDTO;
import com.xd11cc.single.entity.vo.WorkflowTaskApproveVO;
import com.xd11cc.single.entity.vo.WorkflowTaskDelegateVO;
import com.xd11cc.single.entity.vo.WorkflowTaskRejectVO;
import com.xd11cc.single.enums.SystemErrorEnum;
import com.xd11cc.single.enums.WorkflowStatusEnum;
import com.xd11cc.single.mapper.SystemUserRoleMapper;
import com.xd11cc.single.mapper.WorkflowInstanceMapper;
import com.xd11cc.single.modules.workflow.AbstractWorkflowHandler;
import com.xd11cc.single.modules.workflow.WorkflowEvent;
import com.xd11cc.single.modules.workflow.WorkflowHandlerFactory;
import com.xd11cc.single.modules.workflow.WorkflowQuerySupport;
import com.xd11cc.single.util.BaseUnitTest;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.runtime.ChangeActivityStateBuilder;
import org.flowable.engine.runtime.ProcessInstanceQuery;
import org.flowable.task.api.Task;
import org.flowable.task.api.TaskQuery;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

/**
 * {@link WorkflowTaskServiceImpl} 的特征测试。
 * <p>从原 WorkflowServiceImplTest 迁入，断言保持不变，
 * 用于保证「任务模块」从 968 行单体中拆出后语义未变。</p>
 *
 * @author xd11cc
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class WorkflowTaskServiceImplTest extends BaseUnitTest {

    private static final Long CURRENT_USER_ID = 100L;
    private static final Long TENANT_ID = 1L;

    @Mock
    private RuntimeService runtimeService;
    @Mock
    private TaskService taskService;
    @Mock
    private HistoryService historyService;
    @Mock
    private WorkflowInstanceMapper workflowInstanceMapper;
    @Mock
    private WorkflowHandlerFactory handlerFactory;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private SystemUserRoleMapper systemUserRoleMapper;
    @Mock
    private WorkflowQuerySupport querySupport;

    @InjectMocks
    private WorkflowTaskServiceImpl workflowTaskService;

    @BeforeEach
    void setUpContext() {
        SystemUserDO systemUser = new SystemUserDO();
        systemUser.setUsername("tester");
        LoginUserDTO loginUser = new LoginUserDTO();
        loginUser.setUserId(CURRENT_USER_ID);
        loginUser.setSystemUserDO(systemUser);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(loginUser, null));
        TenantContextHolder.setTenantId(TENANT_ID);
    }

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
        TenantContextHolder.clear();
    }

    @Test
    @DisplayName("approveTask：流程结束时置为 COMPLETED 并发布 completed 事件")
    void approveTask_processCompleted() {
        WorkflowTaskApproveVO approveVO = new WorkflowTaskApproveVO();
        approveVO.setTaskId("task-1");

        Task task = mockTask("task-1", String.valueOf(CURRENT_USER_ID));
        WorkflowInstanceDO instance = runningInstance();
        AbstractWorkflowHandler handler = mock(AbstractWorkflowHandler.class);

        given(handlerFactory.getHandler("leave")).willReturn(handler);
        given(querySupport.getInstanceByProcessId("proc-1")).willReturn(instance);
        // 流程已结束：查不到运行中的实例
        ProcessInstanceQuery rtQuery = mock(ProcessInstanceQuery.class);
        given(runtimeService.createProcessInstanceQuery()).willReturn(rtQuery);
        given(rtQuery.processInstanceId(anyString())).willReturn(rtQuery);
        given(rtQuery.singleResult()).willReturn(null);

        workflowTaskService.approveTask(approveVO);

        assertThat(instance.getStatus()).isEqualTo(WorkflowStatusEnum.COMPLETED.getCode());
        then(workflowInstanceMapper).should().updateById(instance);
        then(taskService).should().complete(eq("task-1"), anyMap());
        then(handler).should().onProcessComplete(any());

        // WorkflowEvent 继承 ApplicationEvent，命中的是 publishEvent(ApplicationEvent) 重载，
        // captor 必须声明为 WorkflowEvent 才能匹配
        ArgumentCaptor<WorkflowEvent> captor = ArgumentCaptor.forClass(WorkflowEvent.class);
        then(eventPublisher).should().publishEvent(captor.capture());
        assertThat(captor.getValue().getAction()).isEqualTo(WorkflowEvent.Action.COMPLETED);
        assertThat(captor.getValue().getProcessInstanceId()).isEqualTo("proc-1");
    }

    @Test
    @DisplayName("approveTask：任务不属于当前用户时抛 TASK_NOT_ASSIGNEE")
    void approveTask_notAssignee() {
        WorkflowTaskApproveVO approveVO = new WorkflowTaskApproveVO();
        approveVO.setTaskId("task-1");

        mockTask("task-1", "999");   // 办理人是别人
        given(systemUserRoleMapper.selectList(any())).willReturn(Collections.emptyList());

        assertThatThrownBy(() -> workflowTaskService.approveTask(approveVO))
                .isInstanceOf(ServiceException.class)
                .satisfies(e -> assertThat(((ServiceException) e).getErrorCode().getErrorCode())
                        .isEqualTo(SystemErrorEnum.TASK_NOT_ASSIGNEE.getErrorCode()));
        then(taskService).should(never()).complete(anyString(), anyMap());
    }

    @Test
    @DisplayName("rejectTask：退回到非发起节点时不改实例状态，但会回调 handler")
    void rejectTask_toNonStartNode() {
        WorkflowTaskRejectVO rejectVO = new WorkflowTaskRejectVO();
        rejectVO.setTaskId("task-1");
        rejectVO.setTargetActivityId("approve-node");

        Task task = mockTask("task-1", String.valueOf(CURRENT_USER_ID));
        WorkflowInstanceDO instance = runningInstance();
        AbstractWorkflowHandler handler = mock(AbstractWorkflowHandler.class);
        given(handler.getStartActivityId()).willReturn("start-node");
        given(handlerFactory.getHandler("leave")).willReturn(handler);
        given(querySupport.getInstanceByProcessId("proc-1")).willReturn(instance);

        ChangeActivityStateBuilder stateBuilder = mock(ChangeActivityStateBuilder.class);
        given(runtimeService.createChangeActivityStateBuilder()).willReturn(stateBuilder);
        given(stateBuilder.processInstanceId(anyString())).willReturn(stateBuilder);
        given(stateBuilder.moveActivityIdTo(anyString(), anyString())).willReturn(stateBuilder);

        TaskQuery taskQuery = mock(TaskQuery.class);
        given(taskQuery.taskId("task-1")).willReturn(taskQuery);
        given(taskQuery.singleResult()).willReturn(task);
        given(taskQuery.processInstanceId(anyString())).willReturn(taskQuery);
        given(taskQuery.list()).willReturn(Collections.emptyList());
        given(taskQuery.taskCandidateOrAssigned(anyString())).willReturn(taskQuery);
        given(taskService.createTaskQuery()).willReturn(taskQuery);

        workflowTaskService.rejectTask(rejectVO);

        assertThat(instance.getStatus()).isEqualTo(WorkflowStatusEnum.RUNNING.getCode());
        then(workflowInstanceMapper).should(never()).updateById(any());
        then(handler).should().onTaskReject(any());
        then(runtimeService).should().createChangeActivityStateBuilder();
    }

    @Test
    @DisplayName("delegateTask：目标用户非数字时抛异常，且不得改派引擎任务")
    void delegateTask_invalidTargetUser() {
        WorkflowTaskDelegateVO delegateVO = new WorkflowTaskDelegateVO();
        delegateVO.setTaskId("task-1");
        delegateVO.setTargetUserId("not-a-number");

        assertThatThrownBy(() -> workflowTaskService.delegateTask(delegateVO))
                .isInstanceOf(ServiceException.class)
                .satisfies(e -> assertThat(((ServiceException) e).getErrorCode().getErrorCode())
                        .isEqualTo(SystemErrorEnum.DELEGATE_TARGET_USER_INVALID.getErrorCode()));
        // 关键不变式：校验失败时绝不能已经改派引擎任务
        then(taskService).should(never()).setAssignee(anyString(), anyString());
    }

    // ==================== 辅助 ====================

    private Task mockTask(String taskId, String assignee) {
        Task task = mock(Task.class);
        given(task.getId()).willReturn(taskId);
        given(task.getAssignee()).willReturn(assignee);
        given(task.getProcessInstanceId()).willReturn("proc-1");
        given(task.getTaskDefinitionKey()).willReturn("current-node");

        TaskQuery taskQuery = mock(TaskQuery.class);
        given(taskQuery.taskId(taskId)).willReturn(taskQuery);
        given(taskQuery.singleResult()).willReturn(task);
        given(taskQuery.taskCandidateOrAssigned(anyString())).willReturn(taskQuery);
        given(taskQuery.list()).willReturn(Collections.emptyList());
        given(taskService.createTaskQuery()).willReturn(taskQuery);
        return task;
    }

    private WorkflowInstanceDO runningInstance() {
        WorkflowInstanceDO instance = new WorkflowInstanceDO();
        instance.setId(1L);
        instance.setProcessInstanceId("proc-1");
        instance.setProcessDefinitionKey("leave");
        instance.setBusinessKey("biz-1");
        instance.setTenantId(TENANT_ID);
        instance.setStarterUserId(CURRENT_USER_ID);
        instance.setStatus(WorkflowStatusEnum.RUNNING.getCode());
        return instance;
    }
}
