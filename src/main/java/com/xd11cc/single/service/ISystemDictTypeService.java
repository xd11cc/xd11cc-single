package com.xd11cc.single.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xd11cc.single.entity.domain.SystemDictTypeDO;
import com.xd11cc.single.entity.vo.SystemDictTypeAddVO;
import com.xd11cc.single.entity.vo.SystemDictTypeQueryVO;

import javax.validation.Valid;
import java.util.List;

/**
 * @author xd11cc
 * @date 2026-01-22 22:30:14
 */
public interface ISystemDictTypeService extends IService<SystemDictTypeDO> {

    List<SystemDictTypeDO> getList(SystemDictTypeQueryVO systemDictTypeQueryVO);

    int add(@Valid SystemDictTypeAddVO systemDictTypeAddVO);

    int deleteByIds(List<Long> ids);
}
