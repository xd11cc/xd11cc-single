package com.xd11cc.single.service.impl;

import com.xd11cc.single.config.RedisCache;
import com.xd11cc.single.config.exception.ServiceException;
import com.xd11cc.single.constants.CacheConstants;
import com.xd11cc.single.entity.domain.SystemConfigDO;
import com.xd11cc.single.entity.vo.SystemConfigAddVO;
import com.xd11cc.single.entity.vo.SystemConfigQueryVO;
import com.xd11cc.single.entity.vo.SystemConfigUpdateVO;
import com.xd11cc.single.enums.SystemErrorEnum;
import com.xd11cc.single.mapper.SystemConfigMapper;
import com.xd11cc.single.util.BaseUnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;

@ExtendWith(MockitoExtension.class)
class SystemConfigServiceImplTest extends BaseUnitTest {

    private static final String TEST_KEY = "sms.account";
    private static final String TENANT_CACHE_KEY = CacheConstants.SYSTEM_CONFIG_KEY + TEST_KEY;

    @Mock
    private SystemConfigMapper baseMapper;
    @Mock
    private RedisCache redisCache;

    @InjectMocks
    private SystemConfigServiceImpl configService;

    // ==================== getConfig 命中租户缓存 ====================

    @Test
    void getConfig_命中租户缓存_直接返回() {
        given(redisCache.getCacheObject(TENANT_CACHE_KEY)).willReturn("tenant_value");

        String value = configService.getConfig(TEST_KEY);

        assertThat(value).isEqualTo("tenant_value");
        then(baseMapper).should(org.mockito.BDDMockito.never()).selectOne(any());
    }

    // ==================== getConfig 租户缓存 miss → 租户DB命中 ====================

    @Test
    void getConfig_租户缓存miss_租户DB命中_写入缓存并返回() {
        given(redisCache.getCacheObject(TENANT_CACHE_KEY)).willReturn(null);
        SystemConfigDO do_ = new SystemConfigDO();
        do_.setConfigValue("tenant_db_value");
        given(baseMapper.selectOne(any())).willReturn(do_);

        String value = configService.getConfig(TEST_KEY);

        assertThat(value).isEqualTo("tenant_db_value");
        then(redisCache).should().setCacheObject(eq(TENANT_CACHE_KEY), eq("tenant_db_value"));
    }

    // ==================== getConfig 租户DB miss → 全局缓存命中 ====================

    @Test
    void getConfig_租户DB_miss_全局缓存命中_回填租户缓存() {
        // getConfigCache 被调用 2 次（step 1 tenant + step 3 global），第1次 null 第2次命中
        given(redisCache.getCacheObject(TENANT_CACHE_KEY))
                .willReturn(null).willReturn("global_cached");
        // getConfigFromDB 被调用 2 次，但 step 3 命中后直接返回，step 4 不会走到
        given(baseMapper.selectOne(any())).willReturn(null);

        String value = configService.getConfig(TEST_KEY);

        assertThat(value).isEqualTo("global_cached");
        then(redisCache).should().setCacheObject(eq(TENANT_CACHE_KEY), eq("global_cached"));
    }

    // ==================== getConfig 全局缓存 miss → 全局DB命中 ====================

    @Test
    void getConfig_全局缓存_miss_全局DB命中_回填租户缓存() {
        // getConfigCache 被调用 2 次，均 miss
        given(redisCache.getCacheObject(TENANT_CACHE_KEY))
                .willReturn(null).willReturn(null);
        // getConfigFromDB 被调用 2 次：step 2(tenant) null，step 4(global) 命中
        given(baseMapper.selectOne(any()))
                .willReturn(null);
        SystemConfigDO globalDo = new SystemConfigDO();
        globalDo.setConfigValue("global_db_value");
        // 用 Answer 在第二次调用时返回 globalDo（避免 PotentialStubbingProblem）
        given(baseMapper.selectOne(any())).willAnswer(invocation -> {
            // 第一次调用在 test 方法体内（tenant context），返回 null（上面的 willReturn(null)）
            // 第二次调用在 TenantUtils.execute(-1L, ...) 内（global context）
            // 由于 Mockito 的 willReturn 链式：第一次返回 null，后续返回最后的 willReturn
            return globalDo;
        });

        String value = configService.getConfig(TEST_KEY);

        assertThat(value).isEqualTo("global_db_value");
        then(redisCache).should().setCacheObject(eq(TENANT_CACHE_KEY), eq("global_db_value"));
    }

    // ==================== getConfig 全部 miss → 空值缓存防穿透 ====================

    @Test
    void getConfig_全部miss_写空值缓存并返回null() {
        given(redisCache.getCacheObject(TENANT_CACHE_KEY))
                .willReturn(null).willReturn(null);
        given(baseMapper.selectOne(any()))
                .willReturn(null).willReturn(null);

        String value = configService.getConfig(TEST_KEY);

        assertThat(value).isNull();
        then(redisCache).should().setCacheObject(
                eq(TENANT_CACHE_KEY), eq(""), eq(10L), any());
    }

    // ==================== add - 成功与重复键异常 ====================

    @Test
    void add_成功_写入并清除缓存() {
        SystemConfigAddVO vo = buildConfigAddVO("new.key", "name", "备注");
        given(baseMapper.insert(any(SystemConfigDO.class))).willReturn(1);

        int row = configService.add(vo);

        assertThat(row).isEqualTo(1);
        then(redisCache).should().removeCacheObject(CacheConstants.SYSTEM_CONFIG_KEY + "new.key");
    }

    @Test
    void add_重复键_抛配置键已存在() {
        SystemConfigAddVO vo = buildConfigAddVO("dup.key", "name", "备注");
        willThrow(new DuplicateKeyException("dup")).given(baseMapper).insert(any(SystemConfigDO.class));

        ServiceException ex = assertThrows(ServiceException.class, () -> configService.add(vo));
        assertThat(ex.getErrorCode()).isEqualTo(SystemErrorEnum.CONFIG_KEY_EXISTS);
    }

    // ==================== deleteByIds - 成功与缓存清理 ====================

    @Test
    void deleteByIds_成功_按配置键清理缓存() {
        SystemConfigDO c1 = buildConfigDO(1L, "key.one", "v1");
        SystemConfigDO c2 = buildConfigDO(2L, "key.two", "v2");
        given(baseMapper.selectBatchIds(Arrays.asList(1L, 2L))).willReturn(Arrays.asList(c1, c2));
        given(baseMapper.deleteBatchIds(Arrays.asList(1L, 2L))).willReturn(2);

        int row = configService.deleteByIds(Arrays.asList(1L, 2L));

        assertThat(row).isEqualTo(2);
        then(redisCache).should().removeCacheObject(CacheConstants.SYSTEM_CONFIG_KEY + "key.one");
        then(redisCache).should().removeCacheObject(CacheConstants.SYSTEM_CONFIG_KEY + "key.two");
    }

    // ==================== modifyById - 成功与重复键异常 ====================

    @Test
    void modifyById_成功_更新并清理缓存() {
        SystemConfigUpdateVO vo = buildConfigUpdateVO(1L, "updated.key", "name", "备注");
        SystemConfigDO updated = new SystemConfigDO();
        updated.setConfigKey("updated.key");
        given(baseMapper.updateById(any(SystemConfigDO.class))).willReturn(1);

        int row = configService.modifyById(vo);

        assertThat(row).isEqualTo(1);
        then(redisCache).should().removeCacheObject(CacheConstants.SYSTEM_CONFIG_KEY + "updated.key");
    }

    @Test
    void modifyById_重复键_抛配置键已存在() {
        SystemConfigUpdateVO vo = buildConfigUpdateVO(1L, "dup.key", "name", "备注");
        willThrow(new DuplicateKeyException("dup")).given(baseMapper).updateById(any(SystemConfigDO.class));

        ServiceException ex = assertThrows(ServiceException.class, () -> configService.modifyById(vo));
        assertThat(ex.getErrorCode()).isEqualTo(SystemErrorEnum.CONFIG_KEY_EXISTS);
    }

    // ==================== getList - 查询条件过滤 ====================

    @Test
    void getList_带筛选条件_拼接like查询() {
        SystemConfigQueryVO vo = new SystemConfigQueryVO();
        vo.setConfigKey("sms");
        vo.setConfigName("短信");
        SystemConfigDO expected = buildConfigDO(1L, "sms.account", "v");
        given(baseMapper.selectList(any())).willReturn(Collections.singletonList(expected));

        List<SystemConfigDO> result = configService.getList(vo);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getConfigKey()).isEqualTo("sms.account");
    }

    @Test
    void getList_空筛选_返回全部并按id倒序() {
        SystemConfigQueryVO vo = new SystemConfigQueryVO();
        SystemConfigDO a = buildConfigDO(1L, "a", "va");
        SystemConfigDO b = buildConfigDO(2L, "b", "vb");
        given(baseMapper.selectList(any())).willReturn(Arrays.asList(b, a));

        List<SystemConfigDO> result = configService.getList(vo);

        assertThat(result).hasSize(2);
    }

    // ==================== 辅助方法 ====================

    private static SystemConfigDO buildConfigDO(Long id, String key, String value) {
        SystemConfigDO do_ = new SystemConfigDO();
        do_.setId(id);
        do_.setConfigKey(key);
        do_.setConfigValue(value);
        do_.setConfigName("name_" + key);
        return do_;
    }

    private static SystemConfigAddVO buildConfigAddVO(String configKey, String configName, String remark) {
        SystemConfigAddVO vo = new SystemConfigAddVO();
        vo.setConfigKey(configKey);
        vo.setConfigName(configName);
        vo.setConfigValue("val_" + configKey);
        vo.setRemark(remark);
        return vo;
    }

    private static SystemConfigUpdateVO buildConfigUpdateVO(Long id, String configKey, String configName, String remark) {
        SystemConfigUpdateVO vo = new SystemConfigUpdateVO();
        vo.setId(id);
        vo.setConfigKey(configKey);
        vo.setConfigName(configName);
        vo.setConfigValue("val_" + configKey);
        vo.setRemark(remark);
        return vo;
    }
}
