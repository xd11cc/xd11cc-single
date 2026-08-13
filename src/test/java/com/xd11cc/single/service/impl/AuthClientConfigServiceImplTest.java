package com.xd11cc.single.service.impl;

import com.xd11cc.single.config.exception.ServiceException;
import com.xd11cc.single.entity.domain.AuthClientConfigDO;
import com.xd11cc.single.entity.vo.AuthClientConfigAddVO;
import com.xd11cc.single.entity.vo.AuthClientConfigQueryVO;
import com.xd11cc.single.entity.vo.AuthClientConfigUpdateVO;
import com.xd11cc.single.enums.SystemErrorEnum;
import com.xd11cc.single.mapper.AuthClientConfigMapper;
import com.xd11cc.single.service.IAuthClientConfigService;
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
class AuthClientConfigServiceImplTest extends BaseUnitTest {

    @Mock
    private AuthClientConfigMapper baseMapper;

    @InjectMocks
    private AuthClientConfigServiceImpl clientService;

    // ==================== getBySource ====================

    @Test
    void getBySource_存在_返回配置() {
        AuthClientConfigDO expected = buildConfig(1L, "github", "GitHub", "0", 1);
        given(baseMapper.selectOne(any())).willReturn(expected);

        AuthClientConfigDO result = clientService.getBySource("github");

        assertThat(result).isSameAs(expected);
        assertThat(result.getSource()).isEqualTo("github");
    }

    @Test
    void getBySource_不存在_返回null() {
        given(baseMapper.selectOne(any())).willReturn(null);

        AuthClientConfigDO result = clientService.getBySource("not_exists");

        assertThat(result).isNull();
    }

    // ==================== add ====================

    @Test
    void add_成功_返回插入行数() {
        AuthClientConfigAddVO vo = buildAddVO("wechat", "微信登录", "0", 2, "备注");
        given(baseMapper.insert(any(AuthClientConfigDO.class))).willReturn(1);

        int row = clientService.add(vo);

        assertThat(row).isEqualTo(1);
        then(baseMapper).should().insert(any(AuthClientConfigDO.class));
    }

    @Test
    void add_重复键_抛授权源已存在() {
        AuthClientConfigAddVO vo = buildAddVO("dup", "重复", "0", 2, "备注");
        willThrow(new DuplicateKeyException("dup")).given(baseMapper).insert(any(AuthClientConfigDO.class));

        ServiceException ex = assertThrows(ServiceException.class, () -> clientService.add(vo));
        assertThat(ex.getErrorCode()).isEqualTo(SystemErrorEnum.AUTH_SOURCE_EXISTS);
    }

    // ==================== deleteByIds ====================

    @Test
    void deleteByIds_返回删除行数() {
        given(baseMapper.deleteBatchIds(Arrays.asList(1L, 2L))).willReturn(2);

        int row = clientService.deleteByIds(Arrays.asList(1L, 2L));

        assertThat(row).isEqualTo(2);
        then(baseMapper).should().deleteBatchIds(Arrays.asList(1L, 2L));
    }

    // ==================== modifyById ====================

    @Test
    void modifyById_成功_返回行数() {
        AuthClientConfigUpdateVO vo = buildUpdateVO(1L, "github_new", "GitHub更新", "0", 2, "备注");
        given(baseMapper.updateById(any(AuthClientConfigDO.class))).willReturn(1);

        int row = clientService.modifyById(vo);

        assertThat(row).isEqualTo(1);
        then(baseMapper).should().updateById(any(AuthClientConfigDO.class));
    }

    @Test
    void modifyById_重复键_抛授权源已存在() {
        AuthClientConfigUpdateVO vo = buildUpdateVO(1L, "dup", "重复", "0", 2, "备注");
        willThrow(new DuplicateKeyException("dup")).given(baseMapper).updateById(any(AuthClientConfigDO.class));

        ServiceException ex = assertThrows(ServiceException.class, () -> clientService.modifyById(vo));
        assertThat(ex.getErrorCode()).isEqualTo(SystemErrorEnum.AUTH_SOURCE_EXISTS);
    }

    // ==================== getPageList ====================

    @Test
    void getPageList_带名称筛选_返回结果() {
        AuthClientConfigQueryVO vo = new AuthClientConfigQueryVO();
        vo.setSource("git");
        vo.setName("GitHub");
        vo.setStatus("0");
        AuthClientConfigDO expected = buildConfig(1L, "github", "GitHub", "0", 1);
        given(baseMapper.selectList(any())).willReturn(Collections.singletonList(expected));

        List<AuthClientConfigDO> result = clientService.getPageList(vo);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSource()).isEqualTo("github");
        assertThat(result.get(0).getName()).isEqualTo("GitHub");
    }

    @Test
    void getPageList_空筛选_返回全部() {
        AuthClientConfigQueryVO vo = new AuthClientConfigQueryVO();
        AuthClientConfigDO c1 = buildConfig(1L, "github", "GitHub", "0", 1);
        AuthClientConfigDO c2 = buildConfig(2L, "wechat", "微信", "0", 2);
        given(baseMapper.selectList(any())).willReturn(Arrays.asList(c1, c2));

        List<AuthClientConfigDO> result = clientService.getPageList(vo);

        assertThat(result).hasSize(2);
    }

    // ==================== 辅助方法 ====================

    private static AuthClientConfigDO buildConfig(Long id, String source, String name,
                                                  String status, Integer sort) {
        AuthClientConfigDO config = new AuthClientConfigDO();
        config.setId(id);
        config.setSource(source);
        config.setName(name);
        config.setClientId("client_" + id);
        config.setClientSecret("secret_" + id);
        config.setStatus(status);
        config.setSort(sort);
        config.setRemark("备注");
        return config;
    }

    private static AuthClientConfigAddVO buildAddVO(String source, String name,
                                                    String status, Integer sort, String remark) {
        AuthClientConfigAddVO vo = new AuthClientConfigAddVO();
        vo.setSource(source);
        vo.setName(name);
        vo.setClientId("client_" + source);
        vo.setClientSecret("secret_" + source);
        vo.setStatus(status);
        vo.setSort(sort);
        vo.setRemark(remark);
        return vo;
    }

    private static AuthClientConfigUpdateVO buildUpdateVO(Long id, String source, String name,
                                                           String status, Integer sort, String remark) {
        AuthClientConfigUpdateVO vo = new AuthClientConfigUpdateVO();
        vo.setId(id);
        vo.setSource(source);
        vo.setName(name);
        vo.setClientId("client_" + source);
        vo.setClientSecret("secret_" + source);
        vo.setStatus(status);
        vo.setSort(sort);
        vo.setRemark(remark);
        return vo;
    }
}
