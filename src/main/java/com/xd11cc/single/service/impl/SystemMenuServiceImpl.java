package com.xd11cc.single.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xd11cc.single.entity.domain.SystemMenuDO;
import com.xd11cc.single.entity.domain.SystemUserDO;
import com.xd11cc.single.entity.vo.MetaVO;
import com.xd11cc.single.entity.vo.RouteVO;
import com.xd11cc.single.enums.MenuTypeEnum;
import com.xd11cc.single.mapper.SystemMenuMapper;
import com.xd11cc.single.service.ISystemMenuService;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @Author: xd11cc
 * @Date: 2025/6/19 13:35
 **/
@Service
public class SystemMenuServiceImpl extends ServiceImpl<SystemMenuMapper, SystemMenuDO> implements ISystemMenuService {

    @Override
    public Set<String> getPermissionMenu(SystemUserDO systemUserDO) {
        return Collections.emptySet();
    }

    @Override
    public List<RouteVO> getRoutes(Long userId) {
        List<SystemMenuDO> systemMenuDOS = null;
        if (SystemUserDO.isAdmin(userId)){
            systemMenuDOS = list();
        }else {
            systemMenuDOS = baseMapper.selectTreeMenu(userId);
        }
        return buildTreeMenu(systemMenuDOS, null);
    }

    /**
     * 构建菜单树形结构
     * @param systemMenuDOS
     * @return
     */
    private static List<RouteVO> buildTreeMenu(List<SystemMenuDO> systemMenuDOS, Long rootId) {
        List<RouteVO> routeVOS = new ArrayList<>();
        systemMenuDOS.forEach(systemMenuDO -> {
            if (Objects.equals(rootId, systemMenuDO.getParentId())) {
                RouteVO routeVO = new RouteVO();
                routeVO.setId(systemMenuDO.getId());
                routeVO.setParentId(systemMenuDO.getParentId());
                routeVO.setName(systemMenuDO.getRouteName());
                routeVO.setPath(systemMenuDO.getPath());
                routeVO.setComponent(systemMenuDO.getComponent());
                if (MenuTypeEnum.DIR.getCode().equals(systemMenuDO.getMenuType())) {
                    routeVO.setRedirect("noRedirect");
                }
                routeVO.setSort(systemMenuDO.getSort());
                // 构建其他元素
                MetaVO metaVO = MetaVO.builder()
                        .title(systemMenuDO.getMenuName())
                        .icon(systemMenuDO.getIcon())
                        .query(systemMenuDO.getQuery())
                        .visible(systemMenuDO.isVisible())
                        .permission(systemMenuDO.getPermission())
                        .alwaysShow(true)
                        .build();
                routeVO.setMeta(metaVO);
                routeVO.setChildren(buildTreeMenu(systemMenuDOS, systemMenuDO.getId()));
                routeVOS.add(routeVO);
            }
        });
        // 排序
        routeVOS.sort(Comparator.comparing(RouteVO::getSort));
        routeVOS.forEach(t ->t.getChildren().sort(Comparator.comparing(RouteVO::getSort)));
        return routeVOS;
    }
}
