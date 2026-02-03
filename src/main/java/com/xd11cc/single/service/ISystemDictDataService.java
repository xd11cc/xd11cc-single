package com.xd11cc.single.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xd11cc.single.entity.domain.SystemDictDataDO;
import com.xd11cc.single.entity.vo.SystemDictDataAddVO;
import com.xd11cc.single.entity.vo.SystemDictDataQueryVO;
import com.xd11cc.single.entity.vo.SystemDictDataUpdateVO;

import javax.validation.Valid;
import java.util.List;


/**
 * @author xd11cc
 * @date 2026-01-27 16:18:06
 * @description
 */
public interface ISystemDictDataService extends IService<SystemDictDataDO> {

    int add(SystemDictDataAddVO systemDictDataAddVO);

    int deleteByIds(List<Long> ids);

    int modifyById(@Valid SystemDictDataUpdateVO systemDictDataUpdateVO);

    List<SystemDictDataDO> getList(SystemDictDataQueryVO systemDictDataQueryVO);

    List<SystemDictDataDO> getCache(String dictType);
}
