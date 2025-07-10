package com.xd11cc.single.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xd11cc.single.convert.SystemMenuConvert;
import com.xd11cc.single.entity.domain.SystemMenuDO;
import com.xd11cc.single.entity.domain.SystemUserDO;
import com.xd11cc.single.entity.vo.TreeMenuVO;
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
    public List<TreeMenuVO> getTreeMenu(Long userId) {
        List<SystemMenuDO> treeMenuVOS = baseMapper.selectTreeMenu(userId);
        return buildTreeMenu(treeMenuVOS, null);
    }

    /**
     * 构建菜单树形结构
     * @param systemMenuDOS
     * @return
     */
    private static List<TreeMenuVO> buildTreeMenu(List<SystemMenuDO> systemMenuDOS, Long rootId) {
        List<TreeMenuVO> treeMenuVOS = new ArrayList<>();
        systemMenuDOS.forEach(systemMenuDO -> {
            if (Objects.equals(systemMenuDO.getParentId(), rootId)) {
                TreeMenuVO treeMenuVO = SystemMenuConvert.INSTANCE.do2TreeVO(systemMenuDO);
                treeMenuVO.setChildren(buildTreeMenu(systemMenuDOS, systemMenuDO.getId()));
                treeMenuVOS.add(treeMenuVO);
            }
        });
        // 排序
        treeMenuVOS.sort(Comparator.comparing(TreeMenuVO::getSort));
        treeMenuVOS.forEach(t ->t.getChildren().sort(Comparator.comparing(TreeMenuVO::getSort)));
        return treeMenuVOS;
    }
}
