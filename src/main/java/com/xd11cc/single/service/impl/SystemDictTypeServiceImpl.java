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
        wrapper.like(!StringUtils.isEmpty(systemDictTypeQueryVO.getName()),
                SystemDictTypeDO::getName, systemDictTypeQueryVO.getName());
        wrapper.like(!StringUtils.isEmpty(systemDictTypeQueryVO.getType()),
                SystemDictTypeDO::getType, systemDictTypeQueryVO.getType());
        wrapper.orderByDesc(SystemDictTypeDO::getId);
        return baseMapper.selectList(wrapper);
    }

    @Override
    public void add(SystemDictTypeAddVO systemDictTypeAddVO) {
        SystemDictTypeDO systemDictTypeDO = SystemDictTypeConvert.INSTANCE.addVO2DO(systemDictTypeAddVO);
        baseMapper.insert(systemDictTypeDO);
    }

    @Override
    public void deleteByIds(List<Long> ids) {
        List<SystemDictTypeDO> systemDictTypeDOS = baseMapper.selectBatchIds(ids);
        List<String> types = systemDictTypeDOS.stream().map(SystemDictTypeDO::getType)
                .collect(Collectors.toList());
        List<SystemDictDataDO> systemDictDataDOS = systemDictDataService.list(new LambdaQueryWrapper<SystemDictDataDO>()
                .in(SystemDictDataDO::getType, types));
        if (StringUtils.isNotEmpty(systemDictTypeDOS)) {
            throw new ServiceException(SystemErrorEnum.DICT_TYPE_HAVE_DATA);
        }
        baseMapper.deleteBatchIds(ids);
    }
}
