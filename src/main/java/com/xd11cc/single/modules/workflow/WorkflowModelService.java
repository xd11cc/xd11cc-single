package com.xd11cc.single.modules.workflow;

import com.xd11cc.single.entity.base.PageResult;
import com.xd11cc.single.entity.vo.WorkflowDraftDeployVO;
import com.xd11cc.single.entity.vo.WorkflowDraftQueryVO;
import com.xd11cc.single.entity.vo.WorkflowDraftSaveVO;
import com.xd11cc.single.entity.vo.WorkflowModelVO;
import org.springframework.lang.NonNull;

/**
 * 流程模型服务：草稿的编辑与发布。
 * <p>
 * 负责 Flowable {@code ACT_RE_MODEL} 上的完整生命周期 —— 从设计器保存草稿、
 * 到部署成正式流程定义。已部署定义的治理（挂起/激活/版本）由
 * {@link WorkflowDefinitionService} 负责，两者刻意分开：
 * 草稿编辑随业务人员频繁变动，定义治理由管理员低频操作。
 * </p>
 *
 * @author xd11cc
 */
public interface WorkflowModelService {

    /**
     * 保存流程草稿。
     * <p>modelId 为空时新建草稿，否则更新已有草稿。</p>
     */
    WorkflowModelVO saveDraft(@NonNull WorkflowDraftSaveVO saveVO);

    /**
     * 将草稿部署为正式流程定义。
     * <p>部署成功后会生成 deploymentId 并回填到 Model，草稿状态由 0 变为 1。</p>
     *
     * @return 新建的 deploymentId
     */
    String deployDraft(@NonNull WorkflowDraftDeployVO deployVO);

    /**
     * 分页查询模型（草稿 + 已部署）。
     * <p>status 为 null 时查全部，DRAFT=草稿，其余视为已部署。</p>
     */
    PageResult<WorkflowModelVO> getModelPage(@NonNull WorkflowDraftQueryVO queryVO);

    /**
     * 模型详情（含设计器源码）。
     */
    WorkflowModelVO getModelDetail(@NonNull String modelId);

    /**
     * 删除草稿（只能删除未部署的草稿）。
     */
    void removeDraft(@NonNull String modelId);
}
