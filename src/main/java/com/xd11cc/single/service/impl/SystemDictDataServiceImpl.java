package com.xd11cc.single.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xd11cc.single.convert.SystemDictDataConvert;
import com.xd11cc.single.entity.domain.SystemDictDataDO;
import com.xd11cc.single.entity.domain.SystemDictTypeDO;
import com.xd11cc.single.entity.vo.SystemDictDataAddVO;
import com.xd11cc.single.entity.vo.SystemDictDataQueryVO;
import com.xd11cc.single.entity.vo.SystemDictDataUpdateVO;
import com.xd11cc.single.enums.SystemErrorEnum;
import com.xd11cc.single.exception.ServiceException;
import com.xd11cc.single.mapper.SystemDictDataMapper;
import com.xd11cc.single.service.ISystemDictDataService;
import com.xd11cc.single.service.ISystemDictTypeService;
import com.xd11cc.single.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author xd11cc
 * @date 2026-01-27 16:18:44
 * @description
 */
@Slf4j
@Service
public class SystemDictDataServiceImpl extends ServiceImpl<SystemDictDataMapper, SystemDictDataDO> implements ISystemDictDataService {

    @Autowired
    private ISystemDictTypeService systemDictTypeService;

    @Override
    public int add(SystemDictDataAddVO systemDictDataAddVO) {
        SystemDictTypeDO systemDictTypeDO = systemDictTypeService.getByDictType(systemDictDataAddVO.getDictType());
        if (systemDictTypeDO == null) {
            throw new ServiceException(SystemErrorEnum.DICT_TYPE_NOT_EXISTS);
        }
        SystemDictDataDO systemDictDataDO = SystemDictDataConvert.INSTANCE.addVO2DO(systemDictDataAddVO);
        int row = 0;
        try {
            row = baseMapper.insert(systemDictDataDO);
        } catch (DuplicateKeyException e) {
            throw new ServiceException(SystemErrorEnum.DICT_TYPE_EXISTS);
        }
        if (row > 0) {
            log.info("dictType:{}, tenantId:{}", systemDictDataDO.getDictType(), systemDictDataDO.getTenantId());
//            removeCache(systemDictDataDO.getDictType(), systemDictDataDO.getTenantId());
        }
        return row;
    }

    @Override
    public int deleteByIds(List<Long> ids) {
        int i = baseMapper.deleteBatchIds(ids);
        if (i > 0){
            // 删除缓存
        }
        return i;
    }

    @Override
    public int modifyById(SystemDictDataUpdateVO systemDictDataUpdateVO) {
        SystemDictDataDO systemDictDataDO = SystemDictDataConvert.INSTANCE.updateVO2DO(systemDictDataUpdateVO);
        int i = baseMapper.updateById(systemDictDataDO);
        if (i > 0){
            // 删除缓存
        }
        return i;
    }

    @Override
    public List<SystemDictDataDO> getList(SystemDictDataQueryVO systemDictDataQueryVO) {
        LambdaQueryWrapper<SystemDictDataDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SystemDictDataDO::getDictType, systemDictDataQueryVO.getDictType());
        wrapper.eq(StringUtils.isNotEmpty(systemDictDataQueryVO.getLabel()),
                SystemDictDataDO::getLabel, systemDictDataQueryVO.getLabel());
        wrapper.eq(StringUtils.isNotEmpty(systemDictDataQueryVO.getValue()),
                SystemDictDataDO::getValue, systemDictDataQueryVO.getValue());
        return baseMapper.selectList(wrapper);
    }
}
