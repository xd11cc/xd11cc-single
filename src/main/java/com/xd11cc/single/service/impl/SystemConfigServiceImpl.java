package com.xd11cc.single.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xd11cc.single.config.RedisCache;
import com.xd11cc.single.config.exception.ServiceException;
import com.xd11cc.single.constants.CacheConstants;
import com.xd11cc.single.convert.SystemConfigConvert;
import com.xd11cc.single.entity.domain.SystemConfigDO;
import com.xd11cc.single.entity.vo.SystemConfigAddVO;
import com.xd11cc.single.entity.vo.SystemConfigQueryVO;
import com.xd11cc.single.entity.vo.SystemConfigUpdateVO;
import com.xd11cc.single.enums.SystemErrorEnum;
import com.xd11cc.single.mapper.SystemConfigMapper;
import com.xd11cc.single.service.ISystemConfigService;
import com.xd11cc.single.utils.StringUtils;
import com.xd11cc.single.utils.TenantUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author xd11cc
 * @date 2026-05-20
 */
@Slf4j
@Service
public class SystemConfigServiceImpl extends ServiceImpl<SystemConfigMapper, SystemConfigDO> implements ISystemConfigService {

    @Autowired
    private RedisCache redisCache;

    private String getConfigCacheKey(String configKey) {
        return CacheConstants.SYSTEM_CONFIG_KEY + configKey;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int add(SystemConfigAddVO systemConfigAddVO) {
        SystemConfigDO systemConfigDO = SystemConfigConvert.INSTANCE.addVO2DO(systemConfigAddVO);
        int row;
        try {
            row = baseMapper.insert(systemConfigDO);
        } catch (DuplicateKeyException e) {
            throw new ServiceException(SystemErrorEnum.CONFIG_KEY_EXISTS);
        }
        if (row > 0) {
            redisCache.removeCacheObject(getConfigCacheKey(systemConfigDO.getConfigKey()));
        }
        return row;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteByIds(List<Long> ids) {
        List<SystemConfigDO> configList = baseMapper.selectBatchIds(ids);
        int row = baseMapper.deleteBatchIds(ids);
        if (row > 0) {
            configList.forEach(config -> redisCache.removeCacheObject(getConfigCacheKey(config.getConfigKey())));
        }
        return row;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int modifyById(SystemConfigUpdateVO systemConfigUpdateVO) {
        SystemConfigDO systemConfigDO = SystemConfigConvert.INSTANCE.updateVO2DO(systemConfigUpdateVO);
        int row;
        try {
            row = baseMapper.updateById(systemConfigDO);
        } catch (DuplicateKeyException e) {
            throw new ServiceException(SystemErrorEnum.CONFIG_KEY_EXISTS);
        }
        if (row > 0) {
            redisCache.removeCacheObject(getConfigCacheKey(systemConfigDO.getConfigKey()));
        }
        return row;
    }

    @Override
    public List<SystemConfigDO> getList(SystemConfigQueryVO systemConfigQueryVO) {
        LambdaQueryWrapper<SystemConfigDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.isNotEmpty(systemConfigQueryVO.getConfigKey()),
                SystemConfigDO::getConfigKey, systemConfigQueryVO.getConfigKey());
        wrapper.like(StringUtils.isNotEmpty(systemConfigQueryVO.getConfigName()),
                SystemConfigDO::getConfigName, systemConfigQueryVO.getConfigName());
        wrapper.orderByDesc(SystemConfigDO::getId);
        return baseMapper.selectList(wrapper);
    }

    @Override
    public String getConfig(String configKey) {
        // 1、查询租户缓存
        String tenantCache = getConfigCache(configKey);
        if (tenantCache != null) {
            return tenantCache;
        }
        // 2、查询租户DB
        String tenantValue = getConfigFromDB(configKey);
        if (tenantValue != null) {
            setConfigCache(configKey, tenantValue);
            return tenantValue;
        }
        // 3、查询全局配置缓存
        String globalCache = TenantUtils.execute(-1L, () -> getConfigCache(configKey));
        if (globalCache != null) {
            setConfigCache(configKey, globalCache);
            return globalCache;
        }
        // 4、查询全局DB
        String globalValue = TenantUtils.execute(-1L, () -> getConfigFromDB(configKey));
        if (globalValue != null) {
            setConfigCache(configKey, globalValue);
            TenantUtils.execute(-1L, () -> {
                setConfigCache(configKey, globalValue);
            });
            return globalValue;
        }
        // 5、空值缓存防止穿透
        setConfigCache(configKey, "", 10, TimeUnit.MINUTES);
        return null;
    }

    private String getConfigFromDB(String configKey) {
        LambdaQueryWrapper<SystemConfigDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SystemConfigDO::getConfigKey, configKey).last("LIMIT 1");
        SystemConfigDO configDO = baseMapper.selectOne(wrapper);
        return configDO != null ? configDO.getConfigValue() : null;
    }

    private String getConfigCache(String configKey) {
        return redisCache.getCacheObject(getConfigCacheKey(configKey));
    }

    private void setConfigCache(String configKey, String configValue) {
        redisCache.setCacheObject(getConfigCacheKey(configKey), configValue);
    }

    private void setConfigCache(String configKey, String configValue, Integer expire, TimeUnit timeUnit) {
        redisCache.setCacheObject(getConfigCacheKey(configKey), configValue, expire, timeUnit);
    }
}
