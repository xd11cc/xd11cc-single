package com.xd11cc.single.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xd11cc.single.entity.domain.WorkflowCategoryDO;
import com.xd11cc.single.entity.vo.WorkflowCategoryAddVO;
import com.xd11cc.single.entity.vo.WorkflowCategoryQueryVO;
import com.xd11cc.single.entity.vo.WorkflowCategoryUpdateVO;
import com.xd11cc.single.common.context.TenantContextHolder;
import com.xd11cc.single.common.exception.ServiceException;
import com.xd11cc.single.enums.SystemErrorEnum;
import com.xd11cc.single.service.IWorkflowCategoryService;
import com.xd11cc.single.mapper.WorkflowCategoryMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import java.util.List;

/**
* @author xd11cc
* @date 2026-08-21
*/
@Service
public class WorkflowCategoryServiceImpl extends ServiceImpl<WorkflowCategoryMapper, WorkflowCategoryDO> implements IWorkflowCategoryService {

    @Override
    public int add(WorkflowCategoryAddVO addVO) {
        WorkflowCategoryDO category = new WorkflowCategoryDO();
        category.setCategoryCode(StringUtils.trim(addVO.getCategoryCode()));
        category.setCategoryName(StringUtils.trim(addVO.getCategoryName()));
        category.setSort(addVO.getSort() == null ? 0 : addVO.getSort());
        category.setStatus(StringUtils.defaultIfBlank(addVO.getStatus(), "0"));
        category.setRemark(addVO.getRemark());
        category.setTenantId(TenantContextHolder.getTenantId());
        try {
            return baseMapper.insert(category);
        } catch (DuplicateKeyException e) {
            throw new ServiceException(SystemErrorEnum.WORKFLOW_CATEGORY_CODE_EXISTS);
        }
    }

    @Override
    public int modifyById(WorkflowCategoryUpdateVO updateVO) {
        WorkflowCategoryDO category = new WorkflowCategoryDO();
        category.setId(updateVO.getId());
        category.setCategoryCode(StringUtils.trim(updateVO.getCategoryCode()));
        category.setCategoryName(StringUtils.trim(updateVO.getCategoryName()));
        category.setSort(updateVO.getSort() == null ? 0 : updateVO.getSort());
        category.setStatus(StringUtils.defaultIfBlank(updateVO.getStatus(), "0"));
        category.setRemark(updateVO.getRemark());
        try {
            return baseMapper.updateById(category);
        } catch (DuplicateKeyException e) {
            throw new ServiceException(SystemErrorEnum.WORKFLOW_CATEGORY_CODE_EXISTS);
        }
    }

    @Override
    public int deleteByIds(List<Long> ids) {
        return baseMapper.deleteBatchIds(ids);
    }

    @Override
    public List<WorkflowCategoryDO> getList(WorkflowCategoryQueryVO queryVO) {
        LambdaQueryWrapper<WorkflowCategoryDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.isNotBlank(queryVO.getCategoryCode()),
                WorkflowCategoryDO::getCategoryCode, queryVO.getCategoryCode());
        wrapper.like(StringUtils.isNotBlank(queryVO.getCategoryName()),
                WorkflowCategoryDO::getCategoryName, queryVO.getCategoryName());
        wrapper.eq(StringUtils.isNotBlank(queryVO.getStatus()),
                WorkflowCategoryDO::getStatus, queryVO.getStatus());
        wrapper.orderByAsc(WorkflowCategoryDO::getSort)
                .orderByDesc(WorkflowCategoryDO::getId);
        return baseMapper.selectList(wrapper);
    }

}
