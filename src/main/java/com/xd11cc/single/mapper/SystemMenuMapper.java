package com.xd11cc.single.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xd11cc.single.entity.domain.SystemMenuDO;

import java.util.List;

/**
 * @Author: xd11cc
 * @Date: 2025/6/19 13:36
 **/
public interface SystemMenuMapper extends BaseMapper<SystemMenuDO> {

    List<SystemMenuDO> selectTreeMenu(Long userId);
}
