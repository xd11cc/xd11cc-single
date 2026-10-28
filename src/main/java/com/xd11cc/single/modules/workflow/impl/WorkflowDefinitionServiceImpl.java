package com.xd11cc.single.modules.workflow.impl;

import com.xd11cc.single.common.context.TenantContextHolder;
import com.xd11cc.single.common.exception.ServiceException;
import com.xd11cc.single.entity.vo.WorkflowDefinitionVersionVO;
import com.xd11cc.single.enums.SystemErrorEnum;
import com.xd11cc.single.modules.workflow.WorkflowDefinitionService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 流程定义服务实现。
 *
 * @author xd11cc
 */
@Service
public class WorkflowDefinitionServiceImpl implements WorkflowDefinitionService {

    @Autowired
    private RepositoryService repositoryService;

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
        String tenantId = String.valueOf(TenantContextHolder.getTenantId());
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

    /**
     * 按 ID 取定义，并强制校验租户归属，避免跨租户操作他人流程定义。
     */
    private ProcessDefinition getTenantProcessDefinition(String processDefinitionId) {
        ProcessDefinition definition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionId(processDefinitionId)
                .processDefinitionTenantId(String.valueOf(TenantContextHolder.getTenantId()))
                .singleResult();
        if (definition == null) throw new ServiceException(SystemErrorEnum.PROCESS_DEFINITION_NOT_FOUND);
        return definition;
    }
}
