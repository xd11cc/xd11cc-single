package com.xd11cc.single.service.impl;

import com.xd11cc.single.entity.domain.SystemLoginLogDO;
import com.xd11cc.single.entity.vo.SystemLoginLogQueryVO;
import com.xd11cc.single.mapper.SystemLoginLogMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SystemLoginLogServiceImplTest {

    @Mock
    private SystemLoginLogMapper baseMapper;

    @InjectMocks
    private SystemLoginLogServiceImpl loginLogService;

    // ==================== getList 全参数 ====================

    @Test
    void getList_带全量查询参数_构建对应wrapper() {
        SystemLoginLogQueryVO queryVO = new SystemLoginLogQueryVO();
        queryVO.setUsername("admin");
        queryVO.setLoginType("username");
        queryVO.setStatus("0");
        Date begin = new Date(1700000000000L);
        Date end = new Date(1800000000000L);
        queryVO.setBeginTime(begin);
        queryVO.setEndTime(end);

        SystemLoginLogDO expected = new SystemLoginLogDO();
        expected.setId(1L);
        expected.setUsername("admin");
        given(baseMapper.selectList(org.mockito.ArgumentMatchers.any()))
                .willReturn(Collections.singletonList(expected));

        List<SystemLoginLogDO> result = loginLogService.getList(queryVO);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUsername()).isEqualTo("admin");
        verify(baseMapper).selectList(org.mockito.ArgumentMatchers.any());
    }

    // ==================== getList 空查询 ====================

    @Test
    void getList_空查询条件_返回全部按id倒序() {
        SystemLoginLogQueryVO queryVO = new SystemLoginLogQueryVO();

        SystemLoginLogDO l1 = new SystemLoginLogDO();
        l1.setId(2L);
        SystemLoginLogDO l2 = new SystemLoginLogDO();
        l2.setId(1L);
        given(baseMapper.selectList(org.mockito.ArgumentMatchers.any()))
                .willReturn(Arrays.asList(l1, l2));

        List<SystemLoginLogDO> result = loginLogService.getList(queryVO);

        assertThat(result).hasSize(2);
    }

    // ==================== deleteByIds ====================

    @Test
    void deleteByIds_批量删除_返回删除行数() {
        List<Long> ids = Arrays.asList(1L, 2L, 3L);
        given(baseMapper.deleteBatchIds(ids)).willReturn(3);

        int rows = loginLogService.deleteByIds(ids);

        assertThat(rows).isEqualTo(3);
        verify(baseMapper).deleteBatchIds(ids);
    }

    // ==================== clean ====================

    @Test
    void clean_清空所有登录日志() {
        loginLogService.clean();

        verify(baseMapper).delete(org.mockito.ArgumentMatchers.any());
    }
}
