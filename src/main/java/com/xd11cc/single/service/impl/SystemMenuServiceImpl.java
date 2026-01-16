package com.xd11cc.single.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xd11cc.single.entity.domain.SystemMenuDO;
import com.xd11cc.single.entity.domain.SystemUserDO;
import com.xd11cc.single.entity.vo.MetaVO;
import com.xd11cc.single.entity.vo.RouterVO;
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
    public List<RouterVO> getRoutes(Long userId) {
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
    private static List<RouterVO> buildTreeMenu(List<SystemMenuDO> systemMenuDOS, Long rootId) {
        List<RouterVO> routerVOS = new ArrayList<>();
        systemMenuDOS.forEach(systemMenuDO -> {
            if (rootId.equals(systemMenuDO.getParentId())) {
                RouterVO routerVO = new RouterVO();
                routerVO.setName(systemMenuDO.getMenuName());
                routerVO.setPath(systemMenuDO.getPath());
                routerVO.setComponent(systemMenuDO.getComponent());
                if (MenuTypeEnum.DIR.getCode().equals(systemMenuDO.getMenuType())) {
                    routerVO.setRedirect("noRedirect");
                }
                // 构建其他元素
                MetaVO metaVO = MetaVO.builder()
                        .title(systemMenuDO.getMenuName())
                        .elIcon(systemMenuDO.getIcon())
                        .query(systemMenuDO.getQuery())
                        .hidden(systemMenuDO.isVisible())
                        .permission(systemMenuDO.getPermission())
                        .sort(systemMenuDO.getSort())
                        .build();
                routerVO.setMeta(metaVO);
                routerVO.setChildren(buildTreeMenu(systemMenuDOS, systemMenuDO.getId()));
                routerVOS.add(routerVO);
            }
        });
        // 排序
        routerVOS.sort(Comparator.comparing(r -> r.getMeta().getSort()));
        routerVOS.forEach(t ->t.getChildren().sort(Comparator.comparing(r -> r.getMeta().getSort())));
        return routerVOS;
    }
}
