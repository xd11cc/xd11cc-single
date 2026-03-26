package com.xd11cc.single.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xd11cc.single.convert.SystemMenuConvert;
import com.xd11cc.single.entity.domain.SystemMenuDO;
import com.xd11cc.single.entity.domain.SystemUserDO;
import com.xd11cc.single.entity.vo.*;
import com.xd11cc.single.enums.MenuTypeEnum;
import com.xd11cc.single.enums.SystemErrorEnum;
import com.xd11cc.single.enums.SystemVisibleEnum;
import com.xd11cc.single.config.exception.ServiceException;
import com.xd11cc.single.mapper.SystemMenuMapper;
import com.xd11cc.single.service.ISystemMenuService;
import com.xd11cc.single.utils.StringUtils;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @Author: xd11cc
 * @Date: 2025/6/19 13:35
 **/
@Service
public class SystemMenuServiceImpl extends ServiceImpl<SystemMenuMapper, SystemMenuDO> implements ISystemMenuService {

    @Override
    public Set<String> getPermission(Long userId) {
        boolean admin = SystemUserDO.isAdmin(userId);
        if (admin) {
            Set<String> allPermission = baseMapper.selectAllPermission();
            allPermission.add("*:*:*");
            return allPermission;
        }
        return baseMapper.selectPermission(userId);
    }

    @Override
    public List<RouteVO> getRoutes(Long userId) {
        List<SystemMenuDO> systemMenuDOS = null;
        if (SystemUserDO.isAdmin(userId)){
            systemMenuDOS = list(new LambdaQueryWrapper<SystemMenuDO>().in(SystemMenuDO::getMenuType,
                    Arrays.asList(MenuTypeEnum.MENU.getCode(), MenuTypeEnum.DIR.getCode())));
        }else {
            systemMenuDOS = baseMapper.selectRoutes(userId);
        }
        return buildTreeMenu(systemMenuDOS, null);
    }

    @Override
    public int add(SystemMenuAddVO systemMenuAddVO) {
        SystemMenuDO systemMenuDO = SystemMenuConvert.INSTANCE.addVO2DO(systemMenuAddVO);
        return baseMapper.insert(systemMenuDO);
    }

    @Override
    public int deleteById(Long id) {
        LambdaQueryWrapper<SystemMenuDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SystemMenuDO::getParentId, id);
        List<SystemMenuDO> systemMenuDOS = baseMapper.selectList(wrapper);
        if (StringUtils.isNotEmpty(systemMenuDOS)){
            throw new ServiceException(SystemErrorEnum.MENU_HAVE_CHILDREN);
        }
        return baseMapper.deleteById(id);
    }

    @Override
    public int modifyById(SystemMenuUpdateVO systemMenuUpdateVO) {
        SystemMenuDO systemMenuDO = SystemMenuConvert.INSTANCE.updateVO2DO(systemMenuUpdateVO);
        return baseMapper.updateById(systemMenuDO);
    }

    @Override
    public List<SystemMenuTreeVO> getTreeList(SystemMenuQueryVO systemMenuQueryVO) {
        return baseMapper.selectTreeList(systemMenuQueryVO);
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
                routeVO.setSort(systemMenuDO.getSort());
                // 构建其他元素
                MetaVO metaVO = MetaVO.builder()
                        .title(systemMenuDO.getMenuName())
                        .elIcon(systemMenuDO.getIcon())
                        .query(systemMenuDO.getQuery())
                        .hidden(SystemVisibleEnum.HIDDEN.getCode().equals(systemMenuDO.getVisible()))
                        .permission(systemMenuDO.getPermission())
                        .build();
                if (MenuTypeEnum.DIR.getCode().equals(systemMenuDO.getMenuType())) {
                    routeVO.setRedirect("noRedirect");
                    routeVO.setPath("/" + systemMenuDO.getPath());
                    metaVO.setAlwaysShow(true);
                }
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
