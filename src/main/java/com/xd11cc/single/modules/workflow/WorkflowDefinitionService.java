package com.xd11cc.single.modules.workflow;

import com.xd11cc.single.entity.vo.WorkflowDefinitionVersionVO;
import org.springframework.lang.NonNull;

import java.util.List;

/**
 * 流程定义服务：已部署定义的治理。
 * <p>
 * 只处理 Flowable {@code ACT_RE_PROCDEF} 上的操作 —— 挂起、激活、版本查询。
 * 草稿的编辑与发布由 {@link WorkflowModelService} 负责，两者分开的原因：
 * 定义治理是低频的管理动作，且与运行中实例的可用性强相关。
 * </p>
 *
 * @author xd11cc
 */
public interface WorkflowDefinitionService {

    /**
     * 挂起流程定义。
     * <p>已有实例继续运行，但不再允许发起新实例。</p>
     */
    void suspendDefinition(@NonNull String processDefinitionId);

    /**
     * 激活流程定义。
     * <p>同一 key 下存在多个活跃定义时，会先挂起其余版本，保证同一时刻只有一个活跃版本。</p>
     */
    void activateDefinition(@NonNull String processDefinitionId);

    /**
     * 查询当前租户下指定流程标识的全部已部署版本（按版本号倒序）。
     */
    List<WorkflowDefinitionVersionVO> getDefinitionVersions(@NonNull String key);
}
