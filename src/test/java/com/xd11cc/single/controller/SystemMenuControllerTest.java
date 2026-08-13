package com.xd11cc.single.controller;

import com.xd11cc.single.entity.base.ResponseVO;
import com.xd11cc.single.entity.vo.SystemMenuAddVO;
import com.xd11cc.single.entity.vo.SystemMenuQueryVO;
import com.xd11cc.single.entity.vo.SystemMenuTreeVO;
import com.xd11cc.single.entity.vo.SystemMenuUpdateVO;
import com.xd11cc.single.service.ISystemMenuService;
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
class SystemMenuControllerTest {

    @Mock
    private ISystemMenuService systemMenuService;

    @InjectMocks
    private SystemMenuController menuController;

    // ==================== add ====================

    @Test
    void add_成功_返回行数() {
        SystemMenuAddVO vo = buildAddVO(null, "用户管理", 1, "/system/user", null, "user", null, "el-icon-user", "C", "0", "sys:user:list", "1", "备注");
        given(systemMenuService.add(vo)).willReturn(1);

        ResponseVO<Integer> result = menuController.add(vo);

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData()).isEqualTo(1);
        assertThat(result.getMsg()).isEqualTo("新增成功");
    }

    // ==================== removeById ====================

    @Test
    void removeById_成功_返回删除行数() {
        given(systemMenuService.deleteById(1L)).willReturn(1);

        ResponseVO<Integer> result = menuController.removeById(1L);

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData()).isEqualTo(1);
        assertThat(result.getMsg()).isEqualTo("删除成功");
    }

    // ==================== modifyById ====================

    @Test
    void modifyById_成功_返回行数() {
        SystemMenuUpdateVO vo = buildUpdateVO(1L, "用户管理", 1, "/system/user", null, "user", null, "el-icon-user", "C", "0", "sys:user:list", "1", "备注");
        given(systemMenuService.modifyById(vo)).willReturn(1);

        ResponseVO<Integer> result = menuController.modifyById(vo);

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData()).isEqualTo(1);
        assertThat(result.getMsg()).isEqualTo("修改成功");
    }

    // ==================== treeList ====================

    @Test
    void treeList_查询_返回树形列表() {
        SystemMenuQueryVO vo = new SystemMenuQueryVO();
        List<SystemMenuTreeVO> menus = Collections.singletonList(buildTreeVO(1L, "用户管理", null, 1, "/system/user"));
        given(systemMenuService.getTreeList(vo)).willReturn(menus);

        ResponseVO<List<SystemMenuTreeVO>> result = menuController.menuTree(vo);

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData()).hasSize(1);
        assertThat(result.getData().get(0).getMenuName()).isEqualTo("用户管理");
    }

    // ==================== 辅助方法 ====================

    private static SystemMenuAddVO buildAddVO(Long parentId, String menuName, Integer sort,
                                              String path, String component, String routeName,
                                              String query, String icon, String menuType,
                                              String status, String permission, String visible, String remark) {
        SystemMenuAddVO vo = new SystemMenuAddVO();
        vo.setParentId(parentId);
        vo.setMenuName(menuName);
        vo.setSort(sort);
        vo.setPath(path);
        vo.setComponent(component);
        vo.setRouteName(routeName);
        vo.setQuery(query);
        vo.setIcon(icon);
        vo.setMenuType(menuType);
        vo.setStatus(status);
        vo.setPermission(permission);
        vo.setVisible(visible);
        vo.setRemark(remark);
        return vo;
    }

    private static SystemMenuUpdateVO buildUpdateVO(Long id, String menuName, Integer sort,
                                                    String path, String component, String routeName,
                                                    String query, String icon, String menuType,
                                                    String status, String permission, String visible, String remark) {
        SystemMenuUpdateVO vo = new SystemMenuUpdateVO();
        vo.setId(id);
        vo.setMenuName(menuName);
        vo.setSort(sort);
        vo.setPath(path);
        vo.setComponent(component);
        vo.setRouteName(routeName);
        vo.setQuery(query);
        vo.setIcon(icon);
        vo.setMenuType(menuType);
        vo.setStatus(status);
        vo.setPermission(permission);
        vo.setVisible(visible);
        vo.setRemark(remark);
        return vo;
    }

    private static SystemMenuTreeVO buildTreeVO(Long id, String menuName, Long parentId, Integer sort, String path) {
        SystemMenuTreeVO vo = new SystemMenuTreeVO();
        vo.setId(id);
        vo.setMenuName(menuName);
        vo.setParentId(parentId);
        vo.setSort(sort);
        vo.setPath(path);
        return vo;
    }
}
