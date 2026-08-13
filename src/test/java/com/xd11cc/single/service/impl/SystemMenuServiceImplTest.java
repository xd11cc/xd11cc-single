package com.xd11cc.single.service.impl;

import com.xd11cc.single.config.exception.ServiceException;
import com.xd11cc.single.entity.domain.SystemMenuDO;
import com.xd11cc.single.entity.domain.SystemUserDO;
import com.xd11cc.single.entity.vo.*;
import com.xd11cc.single.enums.MenuTypeEnum;
import com.xd11cc.single.enums.SystemErrorEnum;
import com.xd11cc.single.mapper.SystemMenuMapper;
import com.xd11cc.single.util.BaseUnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class SystemMenuServiceImplTest extends BaseUnitTest {

    @Mock
    private SystemMenuMapper baseMapper;

    @InjectMocks
    private SystemMenuServiceImpl menuService;

    // ==================== getPermission ====================

    @Test
    void getPermission_管理员用户_返回全部权限加通配符() {
        given(baseMapper.selectAllPermission()).willReturn(new java.util.HashSet<>(Collections.singleton("system:user:list")));

        Set<String> result = menuService.getPermission(1L);

        assertThat(result).contains("*:*:*", "system:user:list");
    }

    @Test
    void getPermission_非管理员用户_返回mapper查询权限() {
        Set<String> userPerm = new java.util.HashSet<>(Collections.singleton("system:user:list"));
        given(baseMapper.selectPermission(2L)).willReturn(userPerm);

        Set<String> result = menuService.getPermission(2L);

        assertThat(result).containsExactly("system:user:list");
    }

    // ==================== getRoutes ====================

    @Test
    void getRoutes_管理员用户_查询MENU和DIR类型() {
        // SQL 中根节点 parent_id = NULL，与 TreeUtils 约定一致（null → -1L 分组）
        SystemMenuDO parent = buildMenu(1L, null, MenuTypeEnum.DIR.getCode(), "system", "system", "Layout", 1);
        SystemMenuDO child = buildMenu(2L, 1L, MenuTypeEnum.MENU.getCode(), "user", "/user", "system/user/index", 2);
        // 管理员路径走 list() → baseMapper.selectList
        given(baseMapper.selectList(any())).willReturn(Arrays.asList(parent, child));

        List<RouteVO> routes = menuService.getRoutes(1L);

        assertThat(routes).hasSize(1);
        assertThat(routes.get(0).getId()).isEqualTo(1L);
        assertThat(routes.get(0).getPath()).isEqualTo("/system");
        assertThat(routes.get(0).getRedirect()).isEqualTo("noRedirect");
        assertThat(routes.get(0).getMeta().isAlwaysShow()).isTrue();
        assertThat(routes.get(0).getChildren()).hasSize(1);
        assertThat(routes.get(0).getChildren().get(0).getId()).isEqualTo(2L);
    }

    @Test
    void getRoutes_非管理员用户_走selectRoutes() {
        SystemMenuDO menu = buildMenu(1L, null, MenuTypeEnum.MENU.getCode(), "home", "home", "home/index", 1);
        // 非管理员路径走 baseMapper.selectRoutes
        given(baseMapper.selectRoutes(2L)).willReturn(Collections.singletonList(menu));

        List<RouteVO> routes = menuService.getRoutes(2L);

        assertThat(routes).hasSize(1);
        then(baseMapper).should().selectRoutes(2L);
        then(baseMapper).should(org.mockito.BDDMockito.never()).selectList(any());
    }

    @Test
    void getRoutes_DIR类型_设置noRedirect和路径前缀斜杠() {
        // 用非管理员路径（selectRoutes）避免与管理员路径的 selectList stub 冲突
        SystemMenuDO dir = buildMenu(1L, null, MenuTypeEnum.DIR.getCode(), "system", "system", "Layout", 1);
        given(baseMapper.selectRoutes(2L)).willReturn(Collections.singletonList(dir));

        List<RouteVO> routes = menuService.getRoutes(2L);

        assertThat(routes).hasSize(1);
        assertThat(routes.get(0).getPath()).isEqualTo("/system");
        assertThat(routes.get(0).getRedirect()).isEqualTo("noRedirect");
    }

    @Test
    void getRoutes_MENU类型_不设noRedirect() {
        SystemMenuDO menu = buildMenu(1L, null, MenuTypeEnum.MENU.getCode(), "home", "home", "home/index", 1);
        given(baseMapper.selectRoutes(2L)).willReturn(Collections.singletonList(menu));

        List<RouteVO> routes = menuService.getRoutes(2L);

        assertThat(routes).hasSize(1);
        assertThat(routes.get(0).getPath()).isEqualTo("home");
        assertThat(routes.get(0).getRedirect()).isNull();
    }

    // ==================== add ====================

    @Test
    void add_成功_返回插入行数() {
        SystemMenuAddVO vo = buildMenuAddVO(null, "user", MenuTypeEnum.MENU, "/user", "system/user/index", 1);
        given(baseMapper.insert(any(SystemMenuDO.class))).willReturn(1);

        int row = menuService.add(vo);

        assertThat(row).isEqualTo(1);
        then(baseMapper).should().insert(any(SystemMenuDO.class));
    }

    // ==================== deleteById ====================

    @Test
    void deleteById_有子节点_抛菜单存在子节点异常() {
        List<SystemMenuDO> children = Collections.singletonList(
                buildMenu(2L, 1L, MenuTypeEnum.MENU.getCode(), "child", "/child", "c/index", 1));
        given(baseMapper.selectList(any())).willReturn(children);

        ServiceException ex = assertThrows(ServiceException.class, () -> menuService.deleteById(1L));
        assertThat(ex.getErrorCode()).isEqualTo(SystemErrorEnum.MENU_HAVE_CHILDREN);
        then(baseMapper).should(org.mockito.BDDMockito.never()).deleteById(any());
    }

    @Test
    void deleteById_无子节点_正常删除() {
        given(baseMapper.selectList(any())).willReturn(Collections.emptyList());
        given(baseMapper.deleteById(1L)).willReturn(1);

        int row = menuService.deleteById(1L);

        assertThat(row).isEqualTo(1);
        then(baseMapper).should().deleteById(1L);
    }

    // ==================== modifyById ====================

    @Test
    void modifyById_成功_返回行数() {
        SystemMenuUpdateVO vo = buildMenuUpdateVO(1L, null, "new_name", MenuTypeEnum.MENU, "/new", "comp", 2);
        given(baseMapper.updateById(any(SystemMenuDO.class))).willReturn(1);

        int row = menuService.modifyById(vo);

        assertThat(row).isEqualTo(1);
        then(baseMapper).should().updateById(any(SystemMenuDO.class));
    }

    // ==================== getTreeList ====================

    @Test
    void getTreeList_委托mapper执行原生查询() {
        SystemMenuQueryVO vo = new SystemMenuQueryVO();
        List<SystemMenuTreeVO> expected = Collections.singletonList(
                buildTreeVO(1L, 0L, "系统管理", 1));
        given(baseMapper.selectTreeList(vo)).willReturn(expected);

        List<SystemMenuTreeVO> result = menuService.getTreeList(vo);

        assertThat(result).isSameAs(expected);
        then(baseMapper).should().selectTreeList(vo);
    }

    // ==================== 辅助方法 ====================

    private static SystemMenuDO buildMenu(Long id, Long parentId, String menuType,
                                          String name, String path, String component, Integer sort) {
        SystemMenuDO do_ = new SystemMenuDO();
        do_.setId(id);
        // parentId=null 表示根节点，与 SQL 中 parent_id=NULL 一致
        do_.setParentId(parentId);
        do_.setMenuType(menuType);
        do_.setMenuName(name);
        do_.setRouteName(name);
        do_.setPath(path);
        do_.setComponent(component);
        do_.setSort(sort);
        do_.setIcon("el-icon");
        do_.setVisible("1");
        return do_;
    }

    private static SystemMenuAddVO buildMenuAddVO(Long parentId, String menuName,
                                                   MenuTypeEnum type, String path,
                                                   String component, Integer sort) {
        SystemMenuAddVO vo = new SystemMenuAddVO();
        vo.setParentId(parentId);
        vo.setMenuName(menuName);
        vo.setMenuType(type.getCode());
        vo.setPath(path);
        vo.setComponent(component);
        vo.setSort(sort);
        return vo;
    }

    private static SystemMenuUpdateVO buildMenuUpdateVO(Long id, Long parentId, String menuName,
                                                         MenuTypeEnum type, String path,
                                                         String component, Integer sort) {
        SystemMenuUpdateVO vo = new SystemMenuUpdateVO();
        vo.setId(id);
        vo.setParentId(parentId);
        vo.setMenuName(menuName);
        vo.setMenuType(type.getCode());
        vo.setPath(path);
        vo.setComponent(component);
        vo.setSort(sort);
        return vo;
    }

    private static SystemMenuTreeVO buildTreeVO(Long id, Long parentId, String menuName, Integer sort) {
        SystemMenuTreeVO vo = new SystemMenuTreeVO();
        vo.setId(id);
        vo.setParentId(parentId);
        vo.setMenuName(menuName);
        vo.setSort(sort);
        return vo;
    }
}
