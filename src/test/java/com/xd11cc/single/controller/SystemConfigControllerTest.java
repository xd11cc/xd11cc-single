package com.xd11cc.single.controller;

import com.xd11cc.single.entity.base.PageResult;
import com.xd11cc.single.entity.base.ResponseVO;
import com.xd11cc.single.entity.domain.SystemConfigDO;
import com.xd11cc.single.entity.vo.SystemConfigAddVO;
import com.xd11cc.single.entity.vo.SystemConfigQueryVO;
import com.xd11cc.single.entity.vo.SystemConfigUpdateVO;
import com.xd11cc.single.service.ISystemConfigService;
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
class SystemConfigControllerTest {

    @Mock
    private ISystemConfigService systemConfigService;

    @InjectMocks
    private SystemConfigController configController;

    // ==================== add ====================

    @Test
    void add_成功_返回行数() {
        SystemConfigAddVO vo = buildAddVO("sys.name", "xd11cc", "系统名称", "备注");
        given(systemConfigService.add(vo)).willReturn(1);

        ResponseVO<Integer> result = configController.add(vo);

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData()).isEqualTo(1);
        assertThat(result.getMsg()).isEqualTo("新增成功");
    }

    @Test
    void add_失败_返回失败消息() {
        SystemConfigAddVO vo = buildAddVO("sys.name", "xd11cc", "系统名称", "备注");
        given(systemConfigService.add(vo)).willReturn(0);

        ResponseVO<Integer> result = configController.add(vo);

        assertThat(result.getCode()).isEqualTo(500);
        assertThat(result.getData()).isNull();
        assertThat(result.getMsg()).isEqualTo("新增失败");
    }

    // ==================== removeByIds ====================

    @Test
    void removeByIds_成功_返回删除行数() {
        given(systemConfigService.deleteByIds(Arrays.asList(1L, 2L))).willReturn(2);

        ResponseVO<Integer> result = configController.removeByIds(Arrays.asList(1L, 2L));

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData()).isEqualTo(2);
        assertThat(result.getMsg()).isEqualTo("删除成功");
    }

    // ==================== modifyById ====================

    @Test
    void modifyById_成功_返回行数() {
        SystemConfigUpdateVO vo = buildUpdateVO(1L, "sys.name", "new-value", "系统名称", "备注");
        given(systemConfigService.modifyById(vo)).willReturn(1);

        ResponseVO<Integer> result = configController.modifyById(vo);

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData()).isEqualTo(1);
        assertThat(result.getMsg()).isEqualTo("更新成功");
    }

    // ==================== page ====================

    @Test
    void page_查询_返回分页结果() {
        SystemConfigQueryVO vo = new SystemConfigQueryVO();
        vo.setCurrentPage(1);
        vo.setPageSize(10);
        List<SystemConfigDO> configs = Collections.singletonList(buildConfig(1L, "sys.name", "xd11cc", "系统名称"));
        given(systemConfigService.getList(vo)).willReturn(configs);

        ResponseVO<PageResult<SystemConfigDO>> result = configController.page(vo);

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData()).isNotNull();
        assertThat(result.getData().getRows()).hasSize(1);
    }

    // ==================== getConfig ====================

    @Test
    void getConfig_存在_返回配置值() {
        given(systemConfigService.getConfig("sys.name")).willReturn("xd11cc");

        ResponseVO<String> result = configController.getConfig("sys.name");

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData()).isEqualTo("xd11cc");
    }

    @Test
    void getConfig_不存在_返回null() {
        given(systemConfigService.getConfig("not.exist")).willReturn(null);

        ResponseVO<String> result = configController.getConfig("not.exist");

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData()).isNull();
    }

    // ==================== 辅助方法 ====================

    private static SystemConfigAddVO buildAddVO(String configKey, String configValue, String configName, String remark) {
        SystemConfigAddVO vo = new SystemConfigAddVO();
        vo.setConfigKey(configKey);
        vo.setConfigValue(configValue);
        vo.setConfigName(configName);
        vo.setRemark(remark);
        return vo;
    }

    private static SystemConfigUpdateVO buildUpdateVO(Long id, String configKey, String configValue,
                                                       String configName, String remark) {
        SystemConfigUpdateVO vo = new SystemConfigUpdateVO();
        vo.setId(id);
        vo.setConfigKey(configKey);
        vo.setConfigValue(configValue);
        vo.setConfigName(configName);
        vo.setRemark(remark);
        return vo;
    }

    private static SystemConfigDO buildConfig(Long id, String configKey, String configValue, String configName) {
        SystemConfigDO config = new SystemConfigDO();
        config.setId(id);
        config.setConfigKey(configKey);
        config.setConfigValue(configValue);
        config.setConfigName(configName);
        return config;
    }
}
