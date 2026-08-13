package com.xd11cc.single.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xd11cc.single.config.exception.ServiceException;
import com.xd11cc.single.entity.domain.SystemDictDataDO;
import com.xd11cc.single.entity.domain.SystemDictTypeDO;
import com.xd11cc.single.entity.vo.SystemDictTypeAddVO;
import com.xd11cc.single.entity.vo.SystemDictTypeQueryVO;
import com.xd11cc.single.enums.SystemErrorEnum;
import com.xd11cc.single.mapper.SystemDictTypeMapper;
import com.xd11cc.single.service.ISystemDictDataService;
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
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;

@ExtendWith(MockitoExtension.class)
class SystemDictTypeServiceImplTest extends BaseUnitTest {

    @Mock
    private SystemDictTypeMapper baseMapper;
    @Mock
    private ISystemDictDataService systemDictDataService;

    @InjectMocks
    private SystemDictTypeServiceImpl dictTypeService;

    // ==================== getList ====================

    @Test
    void getList_全匹配查询_返回结果() {
        SystemDictTypeQueryVO vo = buildQueryVO("sys", "字典");
        SystemDictTypeDO expected = buildType(1L, "sys_user", "用户字典", "备注");
        given(baseMapper.selectList(any())).willReturn(Collections.singletonList(expected));

        List<SystemDictTypeDO> result = dictTypeService.getList(vo);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDictType()).isEqualTo("sys_user");
    }

    @Test
    void getList_空筛选_不拼接条件() {
        SystemDictTypeQueryVO vo = new SystemDictTypeQueryVO();
        SystemDictTypeDO expected = buildType(1L, "t", "n", null);
        given(baseMapper.selectList(any())).willReturn(Collections.singletonList(expected));

        List<SystemDictTypeDO> result = dictTypeService.getList(vo);

        assertThat(result).hasSize(1);
        then(baseMapper).should().selectList(any());
    }

    // ==================== add ====================

    @Test
    void add_成功_返回插入行数() {
        SystemDictTypeAddVO vo = buildAddVO("sys_user", "用户字典", "备注");
        given(baseMapper.insert(any(SystemDictTypeDO.class))).willReturn(1);

        int row = dictTypeService.add(vo);

        assertThat(row).isEqualTo(1);
        then(baseMapper).should().insert(any(SystemDictTypeDO.class));
    }

    @Test
    void add_重复键_抛字典类型已存在() {
        SystemDictTypeAddVO vo = buildAddVO("sys_user", "用户字典", null);
        willThrow(new DuplicateKeyException("dup"))
                .given(baseMapper).insert(any(SystemDictTypeDO.class));

        ServiceException ex = assertThrows(ServiceException.class, () -> dictTypeService.add(vo));
        assertThat(ex.getErrorCode()).isEqualTo(SystemErrorEnum.DICT_TYPE_EXISTS);
    }

    // ==================== deleteByIds ====================

    @Test
    void deleteByIds_关联数据为空_正常删除() {
        SystemDictTypeDO t1 = buildType(1L, "t1", "n1", null);
        SystemDictTypeDO t2 = buildType(2L, "t2", "n2", null);
        given(baseMapper.selectBatchIds(Arrays.asList(1L, 2L))).willReturn(Arrays.asList(t1, t2));
        given(systemDictDataService.list(any())).willReturn(Collections.emptyList());
        given(baseMapper.deleteBatchIds(Arrays.asList(1L, 2L))).willReturn(2);

        int row = dictTypeService.deleteByIds(Arrays.asList(1L, 2L));

        assertThat(row).isEqualTo(2);
        then(baseMapper).should().deleteBatchIds(Arrays.asList(1L, 2L));
    }

    @Test
    void deleteByIds_有关联数据_抛字典类型下存在数据异常() {
        SystemDictTypeDO type = buildType(1L, "sys_user", "用户字典", null);
        given(baseMapper.selectBatchIds(Collections.singletonList(1L))).willReturn(Collections.singletonList(type));
        SystemDictDataDO data = new SystemDictDataDO();
        data.setDictType("sys_user");
        given(systemDictDataService.list(any())).willReturn(Collections.singletonList(data));

        ServiceException ex = assertThrows(ServiceException.class,
                () -> dictTypeService.deleteByIds(Collections.singletonList(1L)));
        assertThat(ex.getErrorCode()).isEqualTo(SystemErrorEnum.DICT_TYPE_HAVE_DATA);
        assertThat(ex.getArgs()).containsExactly("sys_user");
        // 未执行删除
        then(baseMapper).should(org.mockito.BDDMockito.never()).deleteBatchIds(any());
    }

    // ==================== getByDictType ====================

    @Test
    void getByDictType_存在_返回字典类型() {
        SystemDictTypeDO expected = buildType(1L, "sys_user", "用户字典", "备注");
        given(baseMapper.selectOne(any())).willReturn(expected);

        SystemDictTypeDO result = dictTypeService.getByDictType("sys_user");

        assertThat(result).isSameAs(expected);
        assertThat(result.getDictName()).isEqualTo("用户字典");
    }

    @Test
    void getByDictType_不存在_返回null() {
        given(baseMapper.selectOne(any())).willReturn(null);

        SystemDictTypeDO result = dictTypeService.getByDictType("not_exists");

        assertThat(result).isNull();
    }

    // ==================== 辅助方法 ====================

    private static SystemDictTypeDO buildType(Long id, String dictType, String dictName, String remark) {
        SystemDictTypeDO do_ = new SystemDictTypeDO();
        do_.setId(id);
        do_.setDictType(dictType);
        do_.setDictName(dictName);
        do_.setRemark(remark);
        return do_;
    }

    private static SystemDictTypeAddVO buildAddVO(String dictType, String dictName, String remark) {
        SystemDictTypeAddVO vo = new SystemDictTypeAddVO();
        vo.setDictType(dictType);
        vo.setDictName(dictName);
        vo.setRemark(remark);
        return vo;
    }

    private static SystemDictTypeQueryVO buildQueryVO(String dictType, String dictName) {
        SystemDictTypeQueryVO vo = new SystemDictTypeQueryVO();
        vo.setDictType(dictType);
        vo.setDictName(dictName);
        return vo;
    }
}
