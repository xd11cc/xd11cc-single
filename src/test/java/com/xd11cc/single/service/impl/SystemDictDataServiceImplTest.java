package com.xd11cc.single.service.impl;

import com.xd11cc.single.config.RedisCache;
import com.xd11cc.single.config.exception.ServiceException;
import com.xd11cc.single.constants.CacheConstants;
import com.xd11cc.single.entity.domain.SystemDictDataDO;
import com.xd11cc.single.entity.domain.SystemDictTypeDO;
import com.xd11cc.single.entity.vo.SystemDictDataAddVO;
import com.xd11cc.single.entity.vo.SystemDictDataQueryVO;
import com.xd11cc.single.entity.vo.SystemDictDataUpdateVO;
import com.xd11cc.single.enums.SystemErrorEnum;
import com.xd11cc.single.mapper.SystemDictDataMapper;
import com.xd11cc.single.service.ISystemDictTypeService;
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
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;

@ExtendWith(MockitoExtension.class)
class SystemDictDataServiceImplTest extends BaseUnitTest {

    private static final String DICT_CACHE_KEY = CacheConstants.DICT_TYPE_KEY + "sys_user";

    @Mock
    private SystemDictDataMapper baseMapper;
    @Mock
    private ISystemDictTypeService systemDictTypeService;
    @Mock
    private RedisCache redisCache;

    @InjectMocks
    private SystemDictDataServiceImpl dictDataService;

    // ==================== add ====================

    @Test
    void add_字典类型不存在_抛字典类型不存在() {
        SystemDictDataAddVO vo = buildAddVO("not_exist_type", "label", "val");
        given(systemDictTypeService.getByDictType("not_exist_type")).willReturn(null);

        ServiceException ex = assertThrows(ServiceException.class, () -> dictDataService.add(vo));
        assertThat(ex.getErrorCode()).isEqualTo(SystemErrorEnum.DICT_TYPE_NOT_EXISTS);
    }

    @Test
    void add_成功_写缓存并返回行数() {
        SystemDictTypeDO type = buildType(1L, "sys_user");
        given(systemDictTypeService.getByDictType("sys_user")).willReturn(type);
        given(baseMapper.insert(any(SystemDictDataDO.class))).willReturn(1);

        int row = dictDataService.add(buildAddVO("sys_user", "label", "val"));

        assertThat(row).isEqualTo(1);
        // 实现中仅调用一次 removeCacheObject，使用当前租户上下文下的 key
        then(redisCache).should().removeCacheObject(DICT_CACHE_KEY);
    }

    @Test
    void add_重复键_抛字典数据已存在() {
        SystemDictTypeDO type = buildType(1L, "sys_user");
        given(systemDictTypeService.getByDictType("sys_user")).willReturn(type);
        willThrow(new DuplicateKeyException("dup")).given(baseMapper).insert(any(SystemDictDataDO.class));

        ServiceException ex = assertThrows(ServiceException.class,
                () -> dictDataService.add(buildAddVO("sys_user", "label", "val")));
        assertThat(ex.getErrorCode()).isEqualTo(SystemErrorEnum.DICT_DATA_EXISTS);
    }

    // ==================== deleteByIds ====================

    @Test
    void deleteByIds_成功_按dictType清理缓存() {
        SystemDictDataDO d1 = buildData(1L, "sys_user", "l1", "v1", "0");
        SystemDictDataDO d2 = buildData(2L, "sys_role", "l2", "v2", "0");
        given(baseMapper.selectBatchIds(Arrays.asList(1L, 2L))).willReturn(Arrays.asList(d1, d2));
        given(baseMapper.deleteBatchIds(Arrays.asList(1L, 2L))).willReturn(2);

        int row = dictDataService.deleteByIds(Arrays.asList(1L, 2L));

        assertThat(row).isEqualTo(2);
        then(redisCache).should().removeCacheObject(CacheConstants.DICT_TYPE_KEY + "sys_user");
        then(redisCache).should().removeCacheObject(CacheConstants.DICT_TYPE_KEY + "sys_role");
    }

    // ==================== modifyById ====================

    @Test
    void modifyById_成功_清理缓存() {
        SystemDictDataUpdateVO vo = buildUpdateVO(1L, "sys_user", "new_label", "new_val", "0");
        given(baseMapper.updateById(any(SystemDictDataDO.class))).willReturn(1);

        int row = dictDataService.modifyById(vo);

        assertThat(row).isEqualTo(1);
        // 实现中仅调用一次 removeCacheObject，使用当前租户上下文下的 key
        then(redisCache).should().removeCacheObject(DICT_CACHE_KEY);
    }

    // ==================== getList ====================

    @Test
    void getList_带筛选条件_拼接查询条件() {
        SystemDictDataQueryVO vo = new SystemDictDataQueryVO();
        vo.setDictType("sys_user");
        vo.setLabel("label");
        vo.setValue("val");
        vo.setStatus("0");
        List<SystemDictDataDO> expected = Collections.singletonList(
                buildData(1L, "sys_user", "label", "val", "0"));
        given(baseMapper.selectList(any())).willReturn(expected);

        List<SystemDictDataDO> result = dictDataService.getList(vo);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDictType()).isEqualTo("sys_user");
    }

    // ==================== getCache 命中租户缓存 ====================

    @Test
    void getCache_命中租户缓存_直接返回() {
        List<SystemDictDataDO> cached = Collections.singletonList(
                buildData(1L, "sys_user", "l", "v", "0"));
        given(redisCache.getCacheObject(DICT_CACHE_KEY)).willReturn(cached);

        List<SystemDictDataDO> result = dictDataService.getCache("sys_user");

        assertThat(result).isSameAs(cached);
        then(baseMapper).should(org.mockito.BDDMockito.never()).selectList(any());
    }

    // ==================== getCache 租户DB miss → 全局缓存命中 ====================

    @Test
    void getCache_租户DB_miss_全局缓存命中_回填租户缓存() {
        // getCacheObject("dict_type:sys_user") 被调用 2 次：
        // 第1次(tenant context) → null；第2次(global context，inside TenantUtils.execute) → globalCache
        // 注意：Mockito 拦截的是 raw key，resolveKey 在 RedisCache 内部发生
        List<SystemDictDataDO> globalCache = Collections.singletonList(
                buildData(1L, "sys_user", "l", "v", "0"));
        AtomicInteger cacheCallCount = new AtomicInteger(0);
        given(redisCache.getCacheObject(DICT_CACHE_KEY)).willAnswer(invocation -> {
            int call = cacheCallCount.getAndIncrement();
            return call == 0 ? null : globalCache;
        });
        // getDictList 被调用 1 次（step 2 tenant，因为 step 3 命中后直接返回）
        given(baseMapper.selectList(any())).willReturn(Collections.emptyList());

        List<SystemDictDataDO> result = dictDataService.getCache("sys_user");

        assertThat(result).isSameAs(globalCache);
        // setDictCache 在 step 3 命中后被调用，写回租户缓存（当前租户上下文）
        then(redisCache).should().setCacheObject(eq(DICT_CACHE_KEY), eq(globalCache));
    }

    // ==================== getCache 全局缓存 miss → 全局DB命中 ====================

    @Test
    void getCache_全局缓存_miss_全局DB命中_回填租户缓存() {
        // getCacheObject 被调用 2 次，均 miss → null
        given(redisCache.getCacheObject(DICT_CACHE_KEY)).willReturn(null);
        // getDictList 被调用 2 次：step 2(tenant) 空，step 4(global) globalList
        List<SystemDictDataDO> globalList = Collections.singletonList(
                buildData(1L, "sys_user", "l", "v", "0"));
        AtomicInteger listCallCount = new AtomicInteger(0);
        given(baseMapper.selectList(any())).willAnswer(invocation -> {
            int call = listCallCount.getAndIncrement();
            return call == 0 ? Collections.emptyList() : globalList;
        });

        List<SystemDictDataDO> result = dictDataService.getCache("sys_user");

        assertThat(result).isSameAs(globalList);
        then(redisCache).should().setCacheObject(eq(DICT_CACHE_KEY), eq(globalList));
    }

    // ==================== getCache 全部 miss → 空列表缓存防穿透 ====================

    @Test
    void getCache_全部miss_写空列表缓存并返回空列表() {
        // 两次调用均返回 null
        given(redisCache.getCacheObject(DICT_CACHE_KEY)).willReturn(null);
        given(baseMapper.selectList(any())).willReturn(Collections.emptyList());

        // 使用 sys_user 以匹配 DICT_CACHE_KEY 的 stub 和 verification
        List<SystemDictDataDO> result = dictDataService.getCache("sys_user");

        assertThat(result).isEmpty();
        then(redisCache).should().setCacheObject(
                eq(DICT_CACHE_KEY), anyList(), anyLong(), any());
    }

    // ==================== 辅助方法 ====================

    private static SystemDictTypeDO buildType(Long id, String dictType) {
        SystemDictTypeDO do_ = new SystemDictTypeDO();
        do_.setId(id);
        do_.setDictType(dictType);
        return do_;
    }

    private static SystemDictDataDO buildData(Long id, String dictType, String label, String value, String status) {
        SystemDictDataDO do_ = new SystemDictDataDO();
        do_.setId(id);
        do_.setDictType(dictType);
        do_.setLabel(label);
        do_.setValue(value);
        do_.setStatus(status);
        do_.setSort(1);
        return do_;
    }

    private static SystemDictDataAddVO buildAddVO(String dictType, String label, String value) {
        SystemDictDataAddVO vo = new SystemDictDataAddVO();
        vo.setDictType(dictType);
        vo.setLabel(label);
        vo.setValue(value);
        return vo;
    }

    private static SystemDictDataUpdateVO buildUpdateVO(Long id, String dictType, String label, String value, String status) {
        SystemDictDataUpdateVO vo = new SystemDictDataUpdateVO();
        vo.setId(id);
        vo.setDictType(dictType);
        vo.setLabel(label);
        vo.setValue(value);
        vo.setStatus(status);
        return vo;
    }
}
