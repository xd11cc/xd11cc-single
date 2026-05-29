package com.xd11cc.single.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xd11cc.single.config.RedisCache;
import com.xd11cc.single.config.exception.ServiceException;
import com.xd11cc.single.constants.CacheConstants;
import com.xd11cc.single.convert.SystemTenantConvert;
import com.xd11cc.single.entity.domain.SystemTenantDO;
import com.xd11cc.single.entity.dto.TenantDTO;
import com.xd11cc.single.entity.vo.SystemTenantAddVO;
import com.xd11cc.single.entity.vo.SystemTenantQueryVO;
import com.xd11cc.single.entity.vo.SystemTenantUpdateVO;
import com.xd11cc.single.enums.SystemErrorEnum;
import com.xd11cc.single.enums.SystemStatusEnum;
import com.xd11cc.single.mapper.SystemTenantMapper;
import com.xd11cc.single.service.ISystemTenantService;
import com.xd11cc.single.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author xd11cc
 * @date 2026-05-28
 */
@Slf4j
@Service
public class SystemTenantServiceImpl extends ServiceImpl<SystemTenantMapper, SystemTenantDO> implements ISystemTenantService {

    @Autowired
    private RedisCache redisCache;

    @Override
    public int add(SystemTenantAddVO vo) {
        SystemTenantDO tenantDO = SystemTenantConvert.INSTANCE.addVO2DO(vo);
        int row;
        try {
            row = baseMapper.insert(tenantDO);
        } catch (DuplicateKeyException e) {
            throw new ServiceException(SystemErrorEnum.TENANT_DOMAIN_EXISTS);
        }
        if (row > 0) {
            refreshCache();
        }
        return row;
    }

    @Override
    public int deleteByIds(List<Long> ids) {
        int row = baseMapper.deleteBatchIds(ids);
        if (row > 0) {
            refreshCache();
        }
        return row;
    }

    @Override
    public int modifyById(SystemTenantUpdateVO vo) {
        SystemTenantDO tenantDO = SystemTenantConvert.INSTANCE.updateVO2DO(vo);
        int row;
        try {
            row = baseMapper.updateById(tenantDO);
        } catch (DuplicateKeyException e) {
            throw new ServiceException(SystemErrorEnum.TENANT_DOMAIN_EXISTS);
        }
        if (row > 0) {
            refreshCache();
        }
        return row;
    }

    @Override
    public SystemTenantDO getDetail(Long id) {
        return baseMapper.selectById(id);
    }

    @Override
    public List<SystemTenantDO> getList(SystemTenantQueryVO queryVO) {
        LambdaQueryWrapper<SystemTenantDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.isNotEmpty(queryVO.getName()),
                SystemTenantDO::getName, queryVO.getName());
        wrapper.eq(StringUtils.isNotEmpty(queryVO.getStatus()),
                SystemTenantDO::getStatus, queryVO.getStatus());
        wrapper.orderByDesc(SystemTenantDO::getId);
        return baseMapper.selectList(wrapper);
    }

    @Override
    public void refreshCache() {
        // 查询所有有效租户
        LambdaQueryWrapper<SystemTenantDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SystemTenantDO::getStatus, SystemStatusEnum.NORMAL.getCode());
        List<SystemTenantDO> tenantList = baseMapper.selectList(wrapper);

        // 重建 Redis Hash
        redisCache.removeCacheObject(CacheConstants.TENANT_DOMAIN_KEY, false);
        Map<String, TenantDTO> tenantMap = new HashMap<>();
        for (SystemTenantDO tenant : tenantList) {
            TenantDTO dto = new TenantDTO();
            dto.setId(tenant.getId());
            dto.setName(tenant.getName());
            dto.setStatus(tenant.getStatus());
            dto.setDomain(tenant.getDomain());
            dto.setExpireTime(tenant.getExpireTime());
            dto.setAccountCount(tenant.getAccountCount());
            for (String domain : tenant.getDomain().split(",")) {
                tenantMap.put(domain, dto);
            }
        }
        if (!tenantMap.isEmpty()) {
            redisCache.setCacheMap(CacheConstants.TENANT_DOMAIN_KEY, tenantMap, false);
        }
        log.info("租户缓存刷新完成，共加载 {} 个租户", tenantMap.size());
    }
}
