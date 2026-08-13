package com.xd11cc.single.service.impl;

import com.xd11cc.single.config.RedisCache;
import com.xd11cc.single.config.exception.ServiceException;
import com.xd11cc.single.constants.CacheConstants;
import com.xd11cc.single.entity.domain.SystemTenantDO;
import com.xd11cc.single.entity.dto.TenantDTO;
import com.xd11cc.single.entity.vo.SystemTenantAddVO;
import com.xd11cc.single.entity.vo.SystemTenantQueryVO;
import com.xd11cc.single.entity.vo.SystemTenantUpdateVO;
import com.xd11cc.single.enums.SystemErrorEnum;
import com.xd11cc.single.enums.SystemStatusEnum;
import com.xd11cc.single.mapper.SystemTenantMapper;
import com.xd11cc.single.util.BaseUnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;

@ExtendWith(MockitoExtension.class)
class SystemTenantServiceImplTest extends BaseUnitTest {

    private static final String TENANT_DOMAIN_KEY = CacheConstants.TENANT_DOMAIN_KEY;

    @Mock
    private SystemTenantMapper baseMapper;
    @Mock
    private RedisCache redisCache;

    @InjectMocks
    private SystemTenantServiceImpl tenantService;

    // ==================== add ====================

    @Test
    void add_成功_写入并刷新缓存() {
        SystemTenantAddVO vo = buildAddVO("example.com", "测试租户", "联系人", "13800000000", 100, "0", new Date());
        SystemTenantDO saved = buildTenant(1L, "example.com", "测试租户", "联系人", "13800000000", 100, "0", new Date());
        given(baseMapper.insert(any(SystemTenantDO.class))).willReturn(1);
        given(baseMapper.selectList(any())).willReturn(Collections.singletonList(saved));

        int row = tenantService.add(vo);

        assertThat(row).isEqualTo(1);
        then(baseMapper).should().insert(any(SystemTenantDO.class));
        then(redisCache).should().removeCacheObject(TENANT_DOMAIN_KEY);
        then(redisCache).should().setCacheMap(eq(TENANT_DOMAIN_KEY), any(Map.class));
    }

    @Test
    void add_重复键_抛租户域名已存在() {
        SystemTenantAddVO vo = buildAddVO("dup.com", "重复", "联系人", "13800000000", 100, "0", new Date());
        willThrow(new DuplicateKeyException("dup")).given(baseMapper).insert(any(SystemTenantDO.class));

        ServiceException ex = assertThrows(ServiceException.class, () -> tenantService.add(vo));
        assertThat(ex.getErrorCode()).isEqualTo(SystemErrorEnum.TENANT_DOMAIN_EXISTS);
        then(redisCache).should(org.mockito.BDDMockito.never()).removeCacheObject(any());
    }

    // ==================== deleteByIds ====================

    @Test
    void deleteByIds_成功_删除并刷新缓存() {
        SystemTenantDO tenant = buildTenant(3L, "c.com", "C", "联系人", "13800000000", 100, "0", new Date());
        given(baseMapper.deleteBatchIds(Arrays.asList(1L, 2L))).willReturn(2);
        given(baseMapper.selectList(any())).willReturn(Collections.singletonList(tenant));

        int row = tenantService.deleteByIds(Arrays.asList(1L, 2L));

        assertThat(row).isEqualTo(2);
        then(baseMapper).should().deleteBatchIds(Arrays.asList(1L, 2L));
        then(redisCache).should().removeCacheObject(TENANT_DOMAIN_KEY);
        then(redisCache).should().setCacheMap(eq(TENANT_DOMAIN_KEY), any(Map.class));
    }

    @Test
    void deleteByIds_空列表_返回0() {
        int row = tenantService.deleteByIds(Collections.emptyList());

        assertThat(row).isEqualTo(0);
        then(baseMapper).should().deleteBatchIds(Collections.emptyList());
        then(redisCache).should(org.mockito.BDDMockito.never()).removeCacheObject(any());
    }

    // ==================== modifyById ====================

    @Test
    void modifyById_成功_更新并刷新缓存() {
        SystemTenantUpdateVO vo = buildUpdateVO(1L, "new.com", "新租户", "联系人", "13800000000", 100, "0", new Date());
        given(baseMapper.updateById(any(SystemTenantDO.class))).willReturn(1);
        given(baseMapper.selectList(any())).willReturn(Collections.singletonList(
                buildTenant(1L, "new.com", "新租户", "联系人", "13800000000", 100, "0", new Date())));

        int row = tenantService.modifyById(vo);

        assertThat(row).isEqualTo(1);
        then(baseMapper).should().updateById(any(SystemTenantDO.class));
        then(redisCache).should().removeCacheObject(TENANT_DOMAIN_KEY);
        then(redisCache).should().setCacheMap(eq(TENANT_DOMAIN_KEY), any(Map.class));
    }

    @Test
    void modifyById_重复键_抛租户域名已存在() {
        SystemTenantUpdateVO vo = buildUpdateVO(1L, "dup.com", "重复", "联系人", "13800000000", 100, "0", new Date());
        willThrow(new DuplicateKeyException("dup")).given(baseMapper).updateById(any(SystemTenantDO.class));

        ServiceException ex = assertThrows(ServiceException.class, () -> tenantService.modifyById(vo));
        assertThat(ex.getErrorCode()).isEqualTo(SystemErrorEnum.TENANT_DOMAIN_EXISTS);
        then(redisCache).should(org.mockito.BDDMockito.never()).removeCacheObject(any());
    }

    // ==================== getDetail ====================

    @Test
    void getDetail_存在_返回租户() {
        SystemTenantDO expected = buildTenant(1L, "example.com", "测试租户", "联系人", "13800000000", 100, "0", new Date());
        given(baseMapper.selectById(1L)).willReturn(expected);

        SystemTenantDO result = tenantService.getDetail(1L);

        assertThat(result).isSameAs(expected);
        then(baseMapper).should().selectById(1L);
    }

    @Test
    void getDetail_不存在_返回null() {
        given(baseMapper.selectById(99L)).willReturn(null);

        SystemTenantDO result = tenantService.getDetail(99L);

        assertThat(result).isNull();
    }

    // ==================== getList ====================

    @Test
    void getList_带名称和状态筛选_返回列表() {
        SystemTenantQueryVO vo = new SystemTenantQueryVO();
        vo.setName("测试租户");
        vo.setStatus(SystemStatusEnum.NORMAL.getCode());
        SystemTenantDO expected = buildTenant(1L, "example.com", "测试租户", "联系人", "13800000000", 100, SystemStatusEnum.NORMAL.getCode(), new Date());
        given(baseMapper.selectList(any())).willReturn(Collections.singletonList(expected));

        List<SystemTenantDO> result = tenantService.getList(vo);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("测试租户");
        assertThat(result.get(0).getStatus()).isEqualTo(SystemStatusEnum.NORMAL.getCode());
    }

    @Test
    void getList_空筛选_返回全部() {
        SystemTenantQueryVO vo = new SystemTenantQueryVO();
        SystemTenantDO t1 = buildTenant(2L, "b.com", "B", "联系人", "13800000000", 100, "0", new Date());
        SystemTenantDO t2 = buildTenant(1L, "a.com", "A", "联系人", "13800000000", 100, "0", new Date());
        given(baseMapper.selectList(any())).willReturn(Arrays.asList(t1, t2));

        List<SystemTenantDO> result = tenantService.getList(vo);

        assertThat(result).hasSize(2);
        then(baseMapper).should().selectList(any());
    }

    // ==================== 辅助方法 ====================

    private static SystemTenantDO buildTenant(Long id, String domain, String name,
                                              String contactName, String contactPhone,
                                              Integer accountCount, String status, Date expireTime) {
        SystemTenantDO tenant = new SystemTenantDO();
        tenant.setId(id);
        tenant.setDomain(domain);
        tenant.setName(name);
        tenant.setContactName(contactName);
        tenant.setContactPhone(contactPhone);
        tenant.setAccountCount(accountCount);
        tenant.setStatus(status);
        tenant.setExpireTime(expireTime);
        return tenant;
    }

    private static SystemTenantAddVO buildAddVO(String domain, String name,
                                                String contactName, String contactPhone,
                                                Integer accountCount, String status, Date expireTime) {
        SystemTenantAddVO vo = new SystemTenantAddVO();
        vo.setDomain(domain);
        vo.setName(name);
        vo.setContactName(contactName);
        vo.setContactPhone(contactPhone);
        vo.setAccountCount(accountCount);
        vo.setStatus(status);
        vo.setExpireTime(expireTime);
        return vo;
    }

    private static SystemTenantUpdateVO buildUpdateVO(Long id, String domain, String name,
                                                       String contactName, String contactPhone,
                                                       Integer accountCount, String status, Date expireTime) {
        SystemTenantUpdateVO vo = new SystemTenantUpdateVO();
        vo.setId(id);
        vo.setDomain(domain);
        vo.setName(name);
        vo.setContactName(contactName);
        vo.setContactPhone(contactPhone);
        vo.setAccountCount(accountCount);
        vo.setStatus(status);
        vo.setExpireTime(expireTime);
        return vo;
    }
}
