package com.xd11cc.single.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xd11cc.single.entity.domain.WorkflowCategoryDO;
import com.xd11cc.single.entity.vo.WorkflowCategoryAddVO;
import com.xd11cc.single.entity.vo.WorkflowCategoryQueryVO;
import com.xd11cc.single.entity.vo.WorkflowCategoryUpdateVO;

import java.util.List;

/**
* @author xd11cc
* @date 2026-08-21
*/
public interface IWorkflowCategoryService extends IService<WorkflowCategoryDO>{

    int add(WorkflowCategoryAddVO addVO);

    int modifyById(WorkflowCategoryUpdateVO updateVO);

    int deleteByIds(List<Long> ids);

    List<WorkflowCategoryDO> getList(WorkflowCategoryQueryVO queryVO);

}
