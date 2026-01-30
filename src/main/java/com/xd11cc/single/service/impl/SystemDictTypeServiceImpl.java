package com.xd11cc.single.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xd11cc.single.convert.SystemDictTypeConvert;
import com.xd11cc.single.entity.domain.SystemDictDataDO;
import com.xd11cc.single.entity.domain.SystemDictTypeDO;
import com.xd11cc.single.entity.vo.SystemDictTypeAddVO;
import com.xd11cc.single.entity.vo.SystemDictTypeQueryVO;
import com.xd11cc.single.enums.SystemErrorEnum;
import com.xd11cc.single.exception.ServiceException;
import com.xd11cc.single.mapper.SystemDictTypeMapper;
import com.xd11cc.single.service.ISystemDictDataService;
import com.xd11cc.single.service.ISystemDictTypeService;
import com.xd11cc.single.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author xd11cc
 * @date 2026-01-22 22:31:00
 */
@Service
public class SystemDictTypeServiceImpl extends ServiceImpl<SystemDictTypeMapper, SystemDictTypeDO> implements ISystemDictTypeService {

    @Autowired
    private ISystemDictDataService systemDictDataService;

    @Override
    public List<SystemDictTypeDO> getList(SystemDictTypeQueryVO systemDictTypeQueryVO) {
        LambdaQueryWrapper<SystemDictTypeDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(!StringUtils.isEmpty(systemDictTypeQueryVO.getDictName()),
                SystemDictTypeDO::getDictName, systemDictTypeQueryVO.getDictName());
        wrapper.like(!StringUtils.isEmpty(systemDictTypeQueryVO.getDictType()),
                SystemDictTypeDO::getDictType, systemDictTypeQueryVO.getDictType());
        wrapper.orderByDesc(SystemDictTypeDO::getId);
        return baseMapper.selectList(wrapper);
    }

    @Override
    public int add(SystemDictTypeAddVO systemDictTypeAddVO) {
        SystemDictTypeDO systemDictTypeDO = SystemDictTypeConvert.INSTANCE.addVO2DO(systemDictTypeAddVO);
        try {
            return baseMapper.insert(systemDictTypeDO);
        } catch (DuplicateKeyException e) {
            throw new ServiceException(SystemErrorEnum.DICT_TYPE_EXISTS);
        }
    }

    @Override
    public int deleteByIds(List<Long> ids) {
        List<SystemDictTypeDO> systemDictTypeDOS = baseMapper.selectBatchIds(ids);
        List<String> types = systemDictTypeDOS.stream().map(SystemDictTypeDO::getDictType)
                .collect(Collectors.toList());
        List<SystemDictDataDO> systemDictDataDOS = systemDictDataService.list(new LambdaQueryWrapper<SystemDictDataDO>()
                .in(SystemDictDataDO::getDictType, types));
        if (StringUtils.isNotEmpty(systemDictDataDOS)) {
            throw new ServiceException(SystemErrorEnum.DICT_TYPE_HAVE_DATA, new Object[]{String.join(",", types)});
        }
        return baseMapper.deleteBatchIds(ids);
    }

    @Override
    public SystemDictTypeDO getByDictType(String dictType) {
        LambdaQueryWrapper<SystemDictTypeDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SystemDictTypeDO::getDictType, dictType);
        return baseMapper.selectOne(wrapper);
    }
}
