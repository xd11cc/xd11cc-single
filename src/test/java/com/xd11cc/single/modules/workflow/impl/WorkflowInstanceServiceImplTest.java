package com.xd11cc.single.modules.workflow.impl;

import com.xd11cc.single.common.context.TenantContextHolder;
import com.xd11cc.single.common.exception.ServiceException;
import com.xd11cc.single.entity.domain.SystemUserDO;
import com.xd11cc.single.entity.domain.WorkflowInstanceDO;
import com.xd11cc.single.entity.dto.LoginUserDTO;
import com.xd11cc.single.enums.SystemErrorEnum;
import com.xd11cc.single.enums.WorkflowStatusEnum;
import com.xd11cc.single.mapper.WorkflowInstanceMapper;
import com.xd11cc.single.modules.workflow.WorkflowHandlerFactory;
import com.xd11cc.single.modules.workflow.AbstractWorkflowHandler;
import com.xd11cc.single.modules.workflow.WorkflowQuerySupport;
import com.xd11cc.single.util.BaseUnitTest;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

/**
 * {@link WorkflowInstanceServiceImpl} 的特征测试。
 * <p>从原 WorkflowServiceImplTest 迁入，断言保持不变。</p>
 *
 * @author xd11cc
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class WorkflowInstanceServiceImplTest extends BaseUnitTest {

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
    private WorkflowQuerySupport querySupport;

    @InjectMocks
    private WorkflowInstanceServiceImpl workflowInstanceService;

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
    @DisplayName("cancelProcess：非发起人取消时抛 TASK_NOT_ASSIGNEE")
    void cancelProcess_notStarter() {
        WorkflowInstanceDO instance = new WorkflowInstanceDO();
        instance.setId(1L);
        instance.setProcessInstanceId("proc-1");
        instance.setProcessDefinitionKey("leave");
        instance.setBusinessKey("biz-1");
        instance.setTenantId(TENANT_ID);
        instance.setStarterUserId(999L);   // 发起人是别人
        instance.setStatus(WorkflowStatusEnum.RUNNING.getCode());
        given(querySupport.getInstanceByProcessId("proc-1")).willReturn(instance);

        assertThatThrownBy(() -> workflowInstanceService.cancelProcess("proc-1", "不想请了"))
                .isInstanceOf(ServiceException.class)
                .satisfies(e -> assertThat(((ServiceException) e).getErrorCode().getErrorCode())
                        .isEqualTo(SystemErrorEnum.TASK_NOT_ASSIGNEE.getErrorCode()));
        then(runtimeService).should(never()).deleteProcessInstance(anyString(), anyString());
        then(workflowInstanceMapper).should(never()).updateById(any());
    }
    @Test
    @DisplayName("cancelProcess：流程已完成时抛 PROCESS_CANNOT_CANCEL")
    void cancelProcess_completedStatus() {
        WorkflowInstanceDO instance = new WorkflowInstanceDO();
        instance.setId(1L);
        instance.setProcessInstanceId("proc-1");
        instance.setProcessDefinitionKey("leave");
        instance.setTenantId(TENANT_ID);
        // 发起人是自己，才能走到状态校验分支
        instance.setStarterUserId(CURRENT_USER_ID);
        instance.setStatus(WorkflowStatusEnum.COMPLETED.getCode());
        given(querySupport.getInstanceByProcessId("proc-1")).willReturn(instance);

        assertThatThrownBy(() -> workflowInstanceService.cancelProcess("proc-1", "想撤回"))
                .isInstanceOf(ServiceException.class)
                .satisfies(e -> assertThat(((ServiceException) e).getErrorCode().getErrorCode())
                        .isEqualTo(SystemErrorEnum.PROCESS_CANNOT_CANCEL.getErrorCode()));
        then(runtimeService).should(never()).deleteProcessInstance(anyString(), anyString());
    }

    @Test
    @DisplayName("cancelProcess：已驳回状态允许取消")
    void cancelProcess_rejectedStatusAllowed() {
        WorkflowInstanceDO instance = new WorkflowInstanceDO();
        instance.setId(1L);
        instance.setProcessInstanceId("proc-1");
        instance.setProcessDefinitionKey("leave");
        instance.setTenantId(TENANT_ID);
        instance.setStarterUserId(CURRENT_USER_ID);
        instance.setStatus(WorkflowStatusEnum.REJECTED.getCode());
        given(querySupport.getInstanceByProcessId("proc-1")).willReturn(instance);
        given(workflowInstanceMapper.updateById(any())).willReturn(1);
        AbstractWorkflowHandler handler = mock(AbstractWorkflowHandler.class);
        given(handlerFactory.getHandler("leave")).willReturn(handler);

        workflowInstanceService.cancelProcess("proc-1", "不再申请");

        // 取消后状态必须置为 CANCELLED，且引擎实例被删除
        assertThat(instance.getStatus()).isEqualTo(WorkflowStatusEnum.CANCELLED.getCode());
        then(runtimeService).should().deleteProcessInstance(eq("proc-1"), anyString());
    }

}
