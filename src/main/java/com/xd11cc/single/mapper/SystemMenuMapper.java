package com.xd11cc.single.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xd11cc.single.entity.domain.SystemMenuDO;
import com.xd11cc.single.entity.vo.SystemMenuQueryVO;
import com.xd11cc.single.entity.vo.SystemMenuTreeVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Author: xd11cc
 * @Date: 2025/6/19 13:36
 **/
public interface SystemMenuMapper extends BaseMapper<SystemMenuDO> {

    List<SystemMenuDO> selectRoutes(Long userId);

    List<SystemMenuTreeVO> selectTreeList(@Param("data") SystemMenuQueryVO systemMenuQueryVO);
}
