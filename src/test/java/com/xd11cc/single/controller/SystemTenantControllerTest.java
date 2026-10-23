package com.xd11cc.single.controller;

import com.xd11cc.single.entity.base.PageResult;
import com.xd11cc.single.entity.base.ResponseVO;
import com.xd11cc.single.entity.domain.SystemTenantDO;
import com.xd11cc.single.entity.vo.SystemTenantAddVO;
import com.xd11cc.single.entity.vo.SystemTenantQueryVO;
import com.xd11cc.single.entity.vo.SystemTenantUpdateVO;
import com.xd11cc.single.service.ISystemTenantService;
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

@ExtendWith(MockitoExtension.class)
class SystemTenantControllerTest {

    @Mock
    private ISystemTenantService systemTenantService;

    @InjectMocks
    private SystemTenantController tenantController;

    // ==================== add ====================

    @Test
    void add_成功_返回行数() {
        SystemTenantAddVO vo = buildAddVO("租户名称", "example.com", "联系人", "13800000000", 10, "0", new Date());
        given(systemTenantService.add(vo)).willReturn(1);

        ResponseVO<Integer> result = tenantController.add(vo);

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData()).isEqualTo(1);
        assertThat(result.getMsg()).isEqualTo("新增成功");
    }

    @Test
    void add_失败_返回失败消息() {
        SystemTenantAddVO vo = buildAddVO("租户名称", "example.com", "联系人", "13800000000", 10, "0", new Date());
        given(systemTenantService.add(vo)).willReturn(0);

        ResponseVO<Integer> result = tenantController.add(vo);

        assertThat(result.getCode()).isEqualTo(500);
        assertThat(result.getData()).isNull();
        assertThat(result.getMsg()).isEqualTo("新增失败");
    }

    // ==================== removeByIds ====================

    @Test
    void removeByIds_成功_返回删除行数() {
        given(systemTenantService.deleteByIds(Arrays.asList(1L, 2L))).willReturn(2);

        ResponseVO<Integer> result = tenantController.removeByIds(Arrays.asList(1L, 2L));

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData()).isEqualTo(2);
        assertThat(result.getMsg()).isEqualTo("删除成功");
    }

    // ==================== modifyById ====================

    @Test
    void modifyById_成功_返回行数() {
        SystemTenantUpdateVO vo = buildUpdateVO(1L, "新名称", "new.example.com", "联系人", "13800000000", 10, "0", new Date());
        given(systemTenantService.modifyById(vo)).willReturn(1);

        ResponseVO<Integer> result = tenantController.modifyById(vo);

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData()).isEqualTo(1);
        assertThat(result.getMsg()).isEqualTo("更新成功");
    }

    // ==================== detail ====================

    @Test
    void detail_存在_返回租户详情() {
        SystemTenantDO tenant = buildTenant(1L, "租户名称", "example.com", "0");
        given(systemTenantService.getDetail(1L)).willReturn(tenant);

        ResponseVO<SystemTenantDO> result = tenantController.detail(1L);

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData()).isSameAs(tenant);
        assertThat(result.getData().getName()).isEqualTo("租户名称");
    }

    @Test
    void detail_不存在_返回null() {
        given(systemTenantService.getDetail(99L)).willReturn(null);

        ResponseVO<SystemTenantDO> result = tenantController.detail(99L);

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData()).isNull();
    }

    // ==================== page ====================

    @Test
    void page_查询_返回分页结果() {
        SystemTenantQueryVO vo = new SystemTenantQueryVO();
        vo.setCurrentPage(1);
        vo.setPageSize(10);
        List<SystemTenantDO> tenants = Collections.singletonList(buildTenant(1L, "租户", "example.com", "0"));
        given(systemTenantService.getList(vo)).willReturn(tenants);

        ResponseVO<PageResult<SystemTenantDO>> result = tenantController.page(vo);

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData()).isNotNull();
        assertThat(result.getData().getRows()).hasSize(1);
    }

    // ==================== refreshCache ====================

    @Test
    void refreshCache_刷新缓存_返回成功() {
        // refreshCache 直接返回 success，无需入参
        ResponseVO<Void> result = tenantController.refreshCache();

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getMsg()).isEqualTo("操作成功");
    }

    // ==================== 辅助方法 ====================

    private static SystemTenantAddVO buildAddVO(String name, String domain, String contactName,
                                                String contactPhone, Integer accountCount,
                                                String status, Date expireTime) {
        SystemTenantAddVO vo = new SystemTenantAddVO();
        vo.setName(name);
        vo.setDomain(domain);
        vo.setContactName(contactName);
        vo.setContactPhone(contactPhone);
        vo.setAccountCount(accountCount);
        vo.setStatus(status);
        vo.setExpireTime(expireTime);
        return vo;
    }

    private static SystemTenantUpdateVO buildUpdateVO(Long id, String name, String domain, String contactName,
                                                       String contactPhone, Integer accountCount,
                                                       String status, Date expireTime) {
        SystemTenantUpdateVO vo = new SystemTenantUpdateVO();
        vo.setId(id);
        vo.setName(name);
        vo.setDomain(domain);
        vo.setContactName(contactName);
        vo.setContactPhone(contactPhone);
        vo.setAccountCount(accountCount);
        vo.setStatus(status);
        vo.setExpireTime(expireTime);
        return vo;
    }

    private static SystemTenantDO buildTenant(Long id, String name, String domain, String status) {
        SystemTenantDO tenant = new SystemTenantDO();
        tenant.setId(id);
        tenant.setName(name);
        tenant.setDomain(domain);
        tenant.setStatus(status);
        return tenant;
    }
}
