package com.xd11cc.single.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xd11cc.single.entity.domain.SystemPostDO;
import com.xd11cc.single.entity.vo.SystemPostAddVO;
import com.xd11cc.single.entity.vo.SystemPostQueryVO;
import com.xd11cc.single.entity.vo.SystemPostUpdateVO;

import java.util.List;

/**
 * @author xd11cc
 * @date 2026-05-27
 */
public interface ISystemPostService extends IService<SystemPostDO> {

    int add(SystemPostAddVO systemPostAddVO);

    int deleteByIds(List<Long> ids);

    int modifyById(SystemPostUpdateVO systemPostUpdateVO);

    List<SystemPostDO> getList(SystemPostQueryVO systemPostQueryVO);

    List<Long> getDeptIdsByPostId(Long postId);

    List<SystemPostDO> getListByDeptId(Long deptId);
}
