package com.xd11cc.single.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xd11cc.single.entity.domain.SystemDeptDO;
import com.xd11cc.single.entity.vo.SystemDeptAddVO;
import com.xd11cc.single.entity.vo.SystemDeptQueryVO;
import com.xd11cc.single.entity.vo.SystemDeptTreeVO;
import com.xd11cc.single.entity.vo.SystemDeptUpdateVO;

import java.util.List;
import java.util.Set;

/**
 * @author xd11cc
 * @date 2026-05-26
 */
public interface ISystemDeptService extends IService<SystemDeptDO> {

    /**
     * 新增部门
     * @param systemDeptAddVO
     * @return
     */
    int add(SystemDeptAddVO systemDeptAddVO);

    /**
     * 删除部门
     * @param id
     * @return
     */
    int deleteById(Long id);

    /**
     * 修改部门
     * @param systemDeptUpdateVO
     * @return
     */
    int modifyById(SystemDeptUpdateVO systemDeptUpdateVO);

    /**
     * 查询部门列表
     * @param systemDeptQueryVO
     * @return
     */
    List<SystemDeptDO> getList(SystemDeptQueryVO systemDeptQueryVO);

    /**
     * 查询部门树形结构
     * @param systemDeptQueryVO
     * @return
     */
    List<SystemDeptTreeVO> getTreeList(SystemDeptQueryVO systemDeptQueryVO);

    /**
     * 获取指定部门及所有下级部门ID
     * @param deptId
     * @return
     */
    Set<Long> getSubDeptIds(Long deptId);
}
