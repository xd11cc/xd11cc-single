package com.xd11cc.single.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xd11cc.single.entity.domain.WorkflowExpressionConfigDO;
import com.xd11cc.single.entity.vo.WorkflowExpressionConfigAddVO;
import com.xd11cc.single.entity.vo.WorkflowExpressionConfigQueryVO;
import com.xd11cc.single.entity.vo.WorkflowExpressionConfigUpdateVO;

import java.util.List;

public interface IWorkflowExpressionConfigService extends IService<WorkflowExpressionConfigDO> {
    int add(WorkflowExpressionConfigAddVO vo);
    int deleteByIds(List<Long> ids);
    int modifyById(WorkflowExpressionConfigUpdateVO vo);
    List<WorkflowExpressionConfigDO> getList(WorkflowExpressionConfigQueryVO vo);
    List<WorkflowExpressionConfigDO> getOptions(String expressionType);
    WorkflowExpressionConfigDO getEnabledByKey(String configKey);
    String resolveExpression(String configKey, String parametersJson);
}
