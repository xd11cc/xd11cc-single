package com.xd11cc.single.controller;

import com.xd11cc.single.entity.base.PageResult;
import com.xd11cc.single.entity.base.ResponseVO;
import com.xd11cc.single.entity.domain.SystemDictDataDO;
import com.xd11cc.single.entity.vo.SystemDictDataAddVO;
import com.xd11cc.single.entity.vo.SystemDictDataQueryVO;
import com.xd11cc.single.entity.vo.SystemDictDataUpdateVO;
import com.xd11cc.single.service.ISystemDictDataService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class SystemDictDataControllerTest {

    @Mock
    private ISystemDictDataService systemDictDataService;

    @InjectMocks
    private SystemDictDataController dictDataController;

    // ==================== add ====================

    @Test
    void add_成功_返回行数() {
        SystemDictDataAddVO vo = buildAddVO("sys_user_status", "1", "正常", "", "1", "0", "排序");
        given(systemDictDataService.add(vo)).willReturn(1);

        ResponseVO<Integer> result = dictDataController.add(vo);

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData()).isEqualTo(1);
        assertThat(result.getMsg()).isEqualTo("新增成功");
    }

    @Test
    void add_失败_返回失败消息() {
        SystemDictDataAddVO vo = buildAddVO("sys_user_status", "1", "正常", "", "1", "0", "排序");
        given(systemDictDataService.add(vo)).willReturn(0);

        ResponseVO<Integer> result = dictDataController.add(vo);

        assertThat(result.getCode()).isEqualTo(500);
        assertThat(result.getData()).isNull();
        assertThat(result.getMsg()).isEqualTo("新增失败");
    }

    // ==================== removeByIds ====================

    @Test
    void removeByIds_成功_返回删除行数() {
        given(systemDictDataService.deleteByIds(Arrays.asList(1L, 2L))).willReturn(2);

        ResponseVO<Integer> result = dictDataController.removeByIds(Arrays.asList(1L, 2L));

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData()).isEqualTo(2);
        assertThat(result.getMsg()).isEqualTo("删除成功");
    }

    // ==================== modifyById ====================

    @Test
    void modifyById_成功_返回行数() {
        SystemDictDataUpdateVO vo = buildUpdateVO(1L, "sys_user_status", "1", "正常", "", "1", "0", "排序");
        given(systemDictDataService.modifyById(vo)).willReturn(1);

        ResponseVO<Integer> result = dictDataController.modifyById(vo);

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData()).isEqualTo(1);
        assertThat(result.getMsg()).isEqualTo("更新成功");
    }

    // ==================== page ====================

    @Test
    void page_查询_返回分页结果() {
        SystemDictDataQueryVO vo = new SystemDictDataQueryVO();
        vo.setCurrentPage(1);
        vo.setPageSize(10);
        List<SystemDictDataDO> dataList = Collections.singletonList(buildDictData(1L, "sys_user_status", "正常", "1", "0"));
        given(systemDictDataService.getList(vo)).willReturn(dataList);

        ResponseVO<PageResult<SystemDictDataDO>> result = dictDataController.page(vo);

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData()).isNotNull();
        assertThat(result.getData().getRows()).hasSize(1);
    }

    // ==================== getCache ====================

    @Test
    void getCache_根据类型查询字典数据() {
        List<SystemDictDataDO> dataList = Collections.singletonList(buildDictData(1L, "sys_user_status", "正常", "1", "0"));
        given(systemDictDataService.getCache("sys_user_status")).willReturn(dataList);

        ResponseVO<List<SystemDictDataDO>> result = dictDataController.getCache("sys_user_status");

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData()).hasSize(1);
        assertThat(result.getData().get(0).getLabel()).isEqualTo("正常");
    }

    @Test
    void getCache_空结果_返回空列表() {
        given(systemDictDataService.getCache("unknown")).willReturn(Collections.emptyList());

        ResponseVO<List<SystemDictDataDO>> result = dictDataController.getCache("unknown");

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData()).isEmpty();
    }

    // ==================== getCacheGroup ====================

    @Test
    void getCacheGroup_根据类型组查询字典数据() {
        List<SystemDictDataDO> statusList = Collections.singletonList(buildDictData(1L, "sys_user_status", "正常", "1", "0"));
        List<SystemDictDataDO> genderList = Collections.singletonList(buildDictData(2L, "sys_user_sex", "男", "1", "0"));
        given(systemDictDataService.getCache("sys_user_status")).willReturn(statusList);
        given(systemDictDataService.getCache("sys_user_sex")).willReturn(genderList);

        ResponseVO<Map<String, List<SystemDictDataDO>>> result = dictDataController.getCacheGroup(Arrays.asList("sys_user_status", "sys_user_sex"));

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData()).hasSize(2);
        assertThat(result.getData().get("sys_user_status")).hasSize(1);
        assertThat(result.getData().get("sys_user_sex")).hasSize(1);
    }

    // ==================== 辅助方法 ====================

    private static SystemDictDataAddVO buildAddVO(String dictType, String value, String label,
                                                   String cssClass, String listClass, String status, String remark) {
        SystemDictDataAddVO vo = new SystemDictDataAddVO();
        vo.setDictType(dictType);
        vo.setValue(value);
        vo.setLabel(label);
        vo.setCssClass(cssClass);
        vo.setListClass(listClass);
        vo.setStatus(status);
        vo.setRemark(remark);
        return vo;
    }

    private static SystemDictDataUpdateVO buildUpdateVO(Long id, String dictType, String value, String label,
                                                         String cssClass, String listClass, String status, String remark) {
        SystemDictDataUpdateVO vo = new SystemDictDataUpdateVO();
        vo.setId(id);
        vo.setDictType(dictType);
        vo.setValue(value);
        vo.setLabel(label);
        vo.setCssClass(cssClass);
        vo.setListClass(listClass);
        vo.setStatus(status);
        vo.setRemark(remark);
        return vo;
    }

    private static SystemDictDataDO buildDictData(Long id, String dictType, String label, String value, String status) {
        SystemDictDataDO data = new SystemDictDataDO();
        data.setId(id);
        data.setDictType(dictType);
        data.setLabel(label);
        data.setValue(value);
        data.setStatus(status);
        return data;
    }
}
