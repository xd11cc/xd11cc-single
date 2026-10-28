package com.xd11cc.single.modules.workflow.impl;

import com.xd11cc.single.common.context.TenantContextHolder;
import com.xd11cc.single.common.exception.ServiceException;
import com.xd11cc.single.entity.vo.WorkflowDraftDeployVO;
import com.xd11cc.single.enums.SystemErrorEnum;
import com.xd11cc.single.service.WorkflowExpressionResolver;
import com.xd11cc.single.util.BaseUnitTest;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.DeploymentBuilder;
import org.flowable.engine.repository.Model;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.repository.ProcessDefinitionQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

/**
 * {@link WorkflowModelServiceImpl} 的特征测试。
 * <p>从原 WorkflowServiceImplTest 迁入，行为断言保持不变，
 * 用于保证「模型模块」从 968 行单体中拆出后语义未变。</p>
 *
 * @author xd11cc
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class WorkflowModelServiceImplTest extends BaseUnitTest {

    private static final Long TENANT_ID = 1L;

    @Mock
    private RepositoryService repositoryService;
    @Mock
    private WorkflowExpressionResolver expressionResolver;

    @InjectMocks
    private WorkflowModelServiceImpl workflowModelService;

    @BeforeEach
    void setUpContext() {
        TenantContextHolder.setTenantId(TENANT_ID);
    }

    @Test
    @DisplayName("deployDraft：部署成功返回 deploymentId，并回填到 Model")
    void deployDraft_success() {
        WorkflowDraftDeployVO deployVO = new WorkflowDraftDeployVO();
        deployVO.setModelId("model-1");

        Model model = mock(Model.class);
        given(model.getId()).willReturn("model-1");
        given(model.getKey()).willReturn("leave");
        given(model.getTenantId()).willReturn(String.valueOf(TENANT_ID));
        given(model.getDeploymentId()).willReturn(null);
        given(model.getName()).willReturn("请假流程");
        given(repositoryService.getModel("model-1")).willReturn(model);

        // BPMN 中的 processId 必须与 model.key 一致，否则 validateBpmnProcessId 抛错
        String bpmn = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<definitions xmlns=\"http://www.omg.org/spec/BPMN/20100524/MODEL\"\n"
                + "  targetNamespace=\"http://www.flowable.org/processdef\">\n"
                + "  <process id=\"leave\" name=\"leave\"/>\n"
                + "</definitions>";
        byte[] bpmnBytes = bpmn.getBytes(StandardCharsets.UTF_8);
        given(repositoryService.getModelEditorSource("model-1")).willReturn(bpmnBytes);
        given(expressionResolver.resolve(anyString(), anyBoolean())).willReturn(bpmn);

        // 当前无活跃定义
        ProcessDefinitionQuery defQuery = mock(ProcessDefinitionQuery.class);
        given(repositoryService.createProcessDefinitionQuery()).willReturn(defQuery);
        given(defQuery.processDefinitionKey(anyString())).willReturn(defQuery);
        given(defQuery.processDefinitionTenantId(nullable(String.class))).willReturn(defQuery);
        given(defQuery.active()).willReturn(defQuery);
        given(defQuery.list()).willReturn(Collections.emptyList());

        // 部署产出
        Deployment deployment = mock(Deployment.class);
        given(deployment.getId()).willReturn("dep-1");
        DeploymentBuilder builder = mock(DeploymentBuilder.class);
        given(repositoryService.createDeployment()).willReturn(builder);
        given(builder.name(nullable(String.class))).willReturn(builder);
        given(builder.category(nullable(String.class))).willReturn(builder);
        given(builder.addBytes(anyString(), any(byte[].class))).willReturn(builder);
        given(builder.tenantId(nullable(String.class))).willReturn(builder);
        given(builder.deploy()).willReturn(deployment);

        ProcessDefinition newDefinition = mock(ProcessDefinition.class);
        given(newDefinition.getId()).willReturn("def-2");
        given(defQuery.deploymentId("dep-1")).willReturn(defQuery);
        given(defQuery.singleResult()).willReturn(newDefinition);

        String result = workflowModelService.deployDraft(deployVO);

        assertThat(result).isEqualTo("dep-1");
        // 部署后必须把 deploymentId 回填到 Model，否则列表页一直显示为草稿
        then(repositoryService).should().saveModel(model);
        // Model 是 Flowable 的接口，mock 上 setter 不存值，因此验证「调用发生过」
        then(model).should().setDeploymentId("dep-1");
    }

    @Test
    @DisplayName("deployDraft：Model 不属于当前租户时抛 PROCESS_DEFINITION_NOT_FOUND")
    void deployDraft_tenantMismatch() {
        WorkflowDraftDeployVO deployVO = new WorkflowDraftDeployVO();
        deployVO.setModelId("model-1");

        Model model = mock(Model.class);
        given(model.getTenantId()).willReturn("999");
        given(repositoryService.getModel("model-1")).willReturn(model);

        assertThatThrownBy(() -> workflowModelService.deployDraft(deployVO))
                .isInstanceOf(ServiceException.class)
                .satisfies(e -> assertThat(((ServiceException) e).getErrorCode().getErrorCode())
                        .isEqualTo(SystemErrorEnum.PROCESS_DEFINITION_NOT_FOUND.getErrorCode()));
    }
}
