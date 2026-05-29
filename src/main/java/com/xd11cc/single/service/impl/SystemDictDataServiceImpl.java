package com.xd11cc.single.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xd11cc.single.config.RedisCache;
import com.xd11cc.single.constants.CacheConstants;
import com.xd11cc.single.convert.SystemDictDataConvert;
import com.xd11cc.single.entity.domain.SystemDictDataDO;
import com.xd11cc.single.entity.domain.SystemDictTypeDO;
import com.xd11cc.single.entity.vo.SystemDictDataAddVO;
import com.xd11cc.single.entity.vo.SystemDictDataQueryVO;
import com.xd11cc.single.entity.vo.SystemDictDataUpdateVO;
import com.xd11cc.single.enums.SystemErrorEnum;
import com.xd11cc.single.enums.SystemStatusEnum;
import com.xd11cc.single.config.exception.ServiceException;
import com.xd11cc.single.mapper.SystemDictDataMapper;
import com.xd11cc.single.service.ISystemDictDataService;
import com.xd11cc.single.service.ISystemDictTypeService;
import com.xd11cc.single.utils.StringUtils;
import com.xd11cc.single.utils.TenantUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;

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

    @Autowired
    private RedisCache redisCache;

    private String getDictTypeKey(String dictType) {
        return CacheConstants.DICT_TYPE_KEY + dictType;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
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
            redisCache.removeCacheObject(getDictTypeKey(systemDictDataDO.getDictType()));
        }
        return row;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteByIds(List<Long> ids) {
        int i = baseMapper.deleteBatchIds(ids);
        if (i > 0){
            // 删除缓存
        }
        return i;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
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
        wrapper.eq(StringUtils.isNotEmpty(systemDictDataQueryVO.getStatus()),
                SystemDictDataDO::getStatus, systemDictDataQueryVO.getStatus());
        wrapper.orderByDesc(SystemDictDataDO::getId);
        return baseMapper.selectList(wrapper);
    }

    @Override
    public List<SystemDictDataDO> getCache(String dictType) {
        // 1、查询租户缓存
        List<SystemDictDataDO> tenantCacheList = getDictCache(dictType);
        if (null != tenantCacheList) {
            return tenantCacheList;
        }
        // 2、查询租户DB
        List<SystemDictDataDO> tenantList = getDictList(dictType);
        if (!CollectionUtils.isEmpty(tenantList)) {
            return tenantList;
        }
        // 3、查询全局字典缓存
        List<SystemDictDataDO> globalCacheList = TenantUtils.execute(-1L, () ->{
            return getDictCache(dictType);
        });
        if (!CollectionUtils.isEmpty(globalCacheList)) {
            // 设置为租户缓存
            setDictCache(dictType, globalCacheList);
            return globalCacheList;
        }
        // 4、查询全局DB
        List<SystemDictDataDO> globalist = TenantUtils.execute(-1L, () ->{
            return getDictList(dictType);
        });
        // 5、将全局字典缓存到租户字典，空数组也保存，防止缓存穿透
        if (CollectionUtils.isEmpty(globalist)) {
            redisCache.setCacheObject(getDictTypeKey(dictType), globalCacheList, 10, TimeUnit.MINUTES);
        }else {
            setDictCache(dictType, globalist);
        }
        return globalist;
    }

    /**
     * 获取字典DB
     * @param dictType
     * @return
     */
    private List<SystemDictDataDO> getDictList(String dictType) {
        LambdaQueryWrapper<SystemDictDataDO> wrapper = new LambdaQueryWrapper<SystemDictDataDO>();
        wrapper.eq(SystemDictDataDO::getDictType, dictType)
                .eq(SystemDictDataDO::getStatus, SystemStatusEnum.NORMAL.getCode())
                .orderByAsc(SystemDictDataDO::getSort);
        return baseMapper.selectList(wrapper);
    }

    /**
     * 获取字典缓存
     * @param dictType
     * @return
     */
    private List<SystemDictDataDO> getDictCache(String dictType) {
        return redisCache.getCacheObject(getDictTypeKey(dictType));
    }

    /**
     * 设置字典缓存
     * @param dictType
     * @param globalCacheList
     */
    private void setDictCache(String dictType, List<SystemDictDataDO> globalCacheList) {
        redisCache.setCacheObject(getDictTypeKey(dictType), globalCacheList);
    }
}
