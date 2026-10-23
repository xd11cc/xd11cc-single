package com.xd11cc.single.controller;

import com.xd11cc.single.entity.base.PageResult;
import com.xd11cc.single.entity.base.ResponseVO;
import com.xd11cc.single.entity.domain.SystemDictTypeDO;
import com.xd11cc.single.entity.vo.SystemDictTypeAddVO;
import com.xd11cc.single.entity.vo.SystemDictTypeQueryVO;
import com.xd11cc.single.entity.vo.SystemDictTypeUpdateVO;
import com.xd11cc.single.service.ISystemDictTypeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class SystemDictTypeControllerTest {

    @Mock
    private ISystemDictTypeService systemDictTypeService;

    @InjectMocks
    private SystemDictTypeController dictTypeController;

    // ==================== add ====================

    @Test
    void add_成功_返回行数() {
        SystemDictTypeAddVO vo = buildAddVO("sys_user_status", "用户状态", "备注");
        given(systemDictTypeService.add(vo)).willReturn(1);

        ResponseVO<Integer> result = dictTypeController.add(vo);

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData()).isEqualTo(1);
        assertThat(result.getMsg()).isEqualTo("新增成功");
    }

    @Test
    void add_失败_返回失败消息() {
        SystemDictTypeAddVO vo = buildAddVO("sys_user_status", "用户状态", "备注");
        given(systemDictTypeService.add(vo)).willReturn(0);

        ResponseVO<Integer> result = dictTypeController.add(vo);

        assertThat(result.getCode()).isEqualTo(500);
        assertThat(result.getData()).isNull();
        assertThat(result.getMsg()).isEqualTo("新增失败");
    }

    // ==================== removeByIds ====================

    @Test
    void removeByIds_成功_返回删除行数() {
        given(systemDictTypeService.deleteByIds(Arrays.asList(1L, 2L))).willReturn(2);

        ResponseVO<Integer> result = dictTypeController.removeByIds(Arrays.asList(1L, 2L));

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData()).isEqualTo(2);
        assertThat(result.getMsg()).isEqualTo("删除成功");
    }

    // ==================== modifyById ====================

    @Test
    void modifyById_成功_返回true() {
        SystemDictTypeUpdateVO vo = buildUpdateVO(1L, "sys_user_status", "用户状态", "备注");
        SystemDictTypeDO dictTypeDO = new SystemDictTypeDO();
        dictTypeDO.setId(1L);
        given(systemDictTypeService.updateById(any(SystemDictTypeDO.class))).willReturn(true);

        ResponseVO<Boolean> result = dictTypeController.modifyById(vo);

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData()).isTrue();
        assertThat(result.getMsg()).isEqualTo("修改成功");
    }

    @Test
    void modifyById_失败_返回失败消息() {
        SystemDictTypeUpdateVO vo = buildUpdateVO(1L, "sys_user_status", "用户状态", "备注");
        given(systemDictTypeService.updateById(any(SystemDictTypeDO.class))).willReturn(false);

        ResponseVO<Boolean> result = dictTypeController.modifyById(vo);

        assertThat(result.getCode()).isEqualTo(500);
        assertThat(result.getData()).isNull();
        assertThat(result.getMsg()).isEqualTo("修改失败");
    }

    // ==================== page ====================

    @Test
    void page_查询_返回分页结果() {
        SystemDictTypeQueryVO vo = new SystemDictTypeQueryVO();
        vo.setCurrentPage(1);
        vo.setPageSize(10);
        List<SystemDictTypeDO> types = Collections.singletonList(buildDictType(1L, "sys_user_status", "用户状态"));
        given(systemDictTypeService.getList(vo)).willReturn(types);

        ResponseVO<PageResult<SystemDictTypeDO>> result = dictTypeController.page(vo);

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData()).isNotNull();
        assertThat(result.getData().getRows()).hasSize(1);
    }

    // ==================== list ====================

    @Test
    void list_查询所有字典类型() {
        List<SystemDictTypeDO> types = Arrays.asList(
                buildDictType(1L, "sys_user_status", "用户状态"),
                buildDictType(2L, "sys_notice_type", "通知类型"));
        given(systemDictTypeService.list()).willReturn(types);

        ResponseVO<List<SystemDictTypeDO>> result = dictTypeController.list();

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData()).hasSize(2);
        assertThat(result.getData().get(0).getDictType()).isEqualTo("sys_user_status");
    }

    // ==================== 辅助方法 ====================

    private static SystemDictTypeAddVO buildAddVO(String dictType, String dictName, String remark) {
        SystemDictTypeAddVO vo = new SystemDictTypeAddVO();
        vo.setDictType(dictType);
        vo.setDictName(dictName);
        vo.setRemark(remark);
        return vo;
    }

    private static SystemDictTypeUpdateVO buildUpdateVO(Long id, String dictType, String dictName, String remark) {
        SystemDictTypeUpdateVO vo = new SystemDictTypeUpdateVO();
        vo.setId(id);
        vo.setDictType(dictType);
        vo.setDictName(dictName);
        vo.setRemark(remark);
        return vo;
    }

    private static SystemDictTypeDO buildDictType(Long id, String dictType, String dictName) {
        SystemDictTypeDO type = new SystemDictTypeDO();
        type.setId(id);
        type.setDictType(dictType);
        type.setDictName(dictName);
        return type;
    }
}
