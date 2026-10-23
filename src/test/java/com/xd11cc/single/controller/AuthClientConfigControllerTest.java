package com.xd11cc.single.controller;

import com.xd11cc.single.convert.AuthClientConfigConvert;
import com.xd11cc.single.entity.base.PageResult;
import com.xd11cc.single.entity.base.ResponseVO;
import com.xd11cc.single.entity.domain.AuthClientConfigDO;
import com.xd11cc.single.entity.vo.AuthClientConfigAddVO;
import com.xd11cc.single.entity.vo.AuthClientConfigListVO;
import com.xd11cc.single.entity.vo.AuthClientConfigQueryVO;
import com.xd11cc.single.entity.vo.AuthClientConfigUpdateVO;
import com.xd11cc.single.service.IAuthClientConfigService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class AuthClientConfigControllerTest {

    @Mock
    private IAuthClientConfigService authClientConfigService;

    @InjectMocks
    private AuthClientConfigController authClientConfigController;

    // ==================== list ====================

    @Test
    void list_查询授权配置列表() {
        List<AuthClientConfigDO> doList = Collections.singletonList(buildDO(1L, "gitee", "Gitee", "el-icon-gitee"));
        given(authClientConfigService.list()).willReturn(doList);

        ResponseVO<List<AuthClientConfigListVO>> result = authClientConfigController.list();

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData()).hasSize(1);
        assertThat(result.getData().get(0).getSource()).isEqualTo("gitee");
        assertThat(result.getData().get(0).getName()).isEqualTo("Gitee");
    }

    // ==================== add ====================

    @Test
    void add_成功_返回行数() {
        AuthClientConfigAddVO vo = buildAddVO("gitee", "client-id", "client-secret", "https://redirect", "Gitee", "el-icon-gitee", "0", 1, "备注");
        given(authClientConfigService.add(vo)).willReturn(1);

        ResponseVO<Integer> result = authClientConfigController.add(vo);

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData()).isEqualTo(1);
        assertThat(result.getMsg()).isEqualTo("新增成功");
    }

    @Test
    void add_失败_返回失败消息() {
        AuthClientConfigAddVO vo = buildAddVO("gitee", "client-id", "client-secret", "https://redirect", "Gitee", "el-icon-gitee", "0", 1, "备注");
        given(authClientConfigService.add(vo)).willReturn(0);

        ResponseVO<Integer> result = authClientConfigController.add(vo);

        assertThat(result.getCode()).isEqualTo(500);
        assertThat(result.getData()).isNull();
        assertThat(result.getMsg()).isEqualTo("新增失败");
    }

    // ==================== removeByIds ====================

    @Test
    void removeByIds_成功_返回删除行数() {
        given(authClientConfigService.deleteByIds(Arrays.asList(1L, 2L))).willReturn(2);

        ResponseVO<Integer> result = authClientConfigController.removeByIds(Arrays.asList(1L, 2L));

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData()).isEqualTo(2);
        assertThat(result.getMsg()).isEqualTo("删除成功");
    }

    // ==================== modifyById ====================

    @Test
    void modifyById_成功_返回行数() {
        AuthClientConfigUpdateVO vo = buildUpdateVO(1L, "gitee", "new-client-id", "new-secret", "https://new-redirect", "New Gitee", "el-icon-gitee", "0", 1, "备注");
        given(authClientConfigService.modifyById(vo)).willReturn(1);

        ResponseVO<Integer> result = authClientConfigController.modifyById(vo);

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData()).isEqualTo(1);
        assertThat(result.getMsg()).isEqualTo("修改成功");
    }

    // ==================== page ====================

    @Test
    void page_查询_返回分页结果() {
        AuthClientConfigQueryVO vo = new AuthClientConfigQueryVO();
        vo.setCurrentPage(1);
        vo.setPageSize(10);
        List<AuthClientConfigDO> doList = Collections.singletonList(buildDO(1L, "gitee", "Gitee", "el-icon-gitee"));
        given(authClientConfigService.getPageList(vo)).willReturn(doList);

        ResponseVO<PageResult<AuthClientConfigDO>> result = authClientConfigController.page(vo);

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData()).isNotNull();
        assertThat(result.getData().getRows()).hasSize(1);
    }

    // ==================== 辅助方法 ====================

    private static AuthClientConfigDO buildDO(Long id, String source, String name, String icon) {
        AuthClientConfigDO doObj = new AuthClientConfigDO();
        doObj.setId(id);
        doObj.setSource(source);
        doObj.setName(name);
        doObj.setIcon(icon);
        return doObj;
    }

    private static AuthClientConfigAddVO buildAddVO(String source, String clientId, String clientSecret,
                                                    String redirectUri, String name, String icon,
                                                    String status, Integer sort, String remark) {
        AuthClientConfigAddVO vo = new AuthClientConfigAddVO();
        vo.setSource(source);
        vo.setClientId(clientId);
        vo.setClientSecret(clientSecret);
        vo.setRedirectUri(redirectUri);
        vo.setName(name);
        vo.setIcon(icon);
        vo.setStatus(status);
        vo.setSort(sort);
        vo.setRemark(remark);
        return vo;
    }

    private static AuthClientConfigUpdateVO buildUpdateVO(Long id, String source, String clientId, String clientSecret,
                                                          String redirectUri, String name, String icon,
                                                          String status, Integer sort, String remark) {
        AuthClientConfigUpdateVO vo = new AuthClientConfigUpdateVO();
        vo.setId(id);
        vo.setSource(source);
        vo.setClientId(clientId);
        vo.setClientSecret(clientSecret);
        vo.setRedirectUri(redirectUri);
        vo.setName(name);
        vo.setIcon(icon);
        vo.setStatus(status);
        vo.setSort(sort);
        vo.setRemark(remark);
        return vo;
    }
}
