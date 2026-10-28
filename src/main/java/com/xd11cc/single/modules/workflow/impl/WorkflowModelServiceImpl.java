package com.xd11cc.single.modules.workflow.impl;

import com.xd11cc.single.common.context.TenantContextHolder;
import com.xd11cc.single.common.exception.ServiceException;
import com.xd11cc.single.entity.base.PageResult;
import com.xd11cc.single.entity.vo.WorkflowDraftDeployVO;
import com.xd11cc.single.entity.vo.WorkflowDraftQueryVO;
import com.xd11cc.single.entity.vo.WorkflowDraftSaveVO;
import com.xd11cc.single.entity.vo.WorkflowModelVO;
import com.xd11cc.single.enums.SystemErrorEnum;
import com.xd11cc.single.modules.workflow.WorkflowModelService;
import com.xd11cc.single.service.WorkflowExpressionResolver;
import com.xd11cc.single.utils.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.flowable.bpmn.converter.BpmnXMLConverter;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.Process;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.Model;
import org.flowable.engine.repository.ModelQuery;
import org.flowable.engine.repository.ProcessDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 流程模型服务实现。
 *
 * @author xd11cc
 */
@Slf4j
@Service
public class WorkflowModelServiceImpl implements WorkflowModelService {

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private WorkflowExpressionResolver expressionResolver;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WorkflowModelVO saveDraft(WorkflowDraftSaveVO saveVO) {
        Long userId = SecurityUtils.getUserId();
        Long tenantId = TenantContextHolder.getTenantId();

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
        Long tenantId = TenantContextHolder.getTenantId();

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
        String tenantId = String.valueOf(TenantContextHolder.getTenantId());

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
        Long tenantId = TenantContextHolder.getTenantId();
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
        Long tenantId = TenantContextHolder.getTenantId();
        Model model = repositoryService.getModel(modelId);
        if (model == null || !tenantId.toString().equals(model.getTenantId())) {
            throw new ServiceException(SystemErrorEnum.PROCESS_DEFINITION_NOT_FOUND);
        }
        if (model.getDeploymentId() != null) {
            throw new ServiceException(SystemErrorEnum.PROCESS_ALREADY_COMPLETED);
        }
        repositoryService.deleteModel(modelId);
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
}
