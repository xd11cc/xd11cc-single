package com.xd11cc.single.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xd11cc.single.entity.domain.SystemUserDO;
import com.xd11cc.single.entity.vo.SystemUserAddVO;
import com.xd11cc.single.entity.vo.SystemUserDetailVO;
import com.xd11cc.single.entity.vo.SystemUserQueryVO;
import com.xd11cc.single.entity.vo.SystemUserUpdateVO;

import java.util.List;

/**
 * @Author: xd11cc
 * @Date: 2025/6/14 00:25
 **/
public interface ISystemUserService extends IService<SystemUserDO> {

    /**
     * 根据用户名查询用户信息
     * @param username
     * @return
     */
    SystemUserDO getByUsername(String username);

    /**
     * 根据邮箱查询用户信息
     * @param email
     * @return
     */
    SystemUserDO getByEmail(String email);

    /**
     * 新增用户
     * @param systemUserAddVO
     * @return
     */
    int add(SystemUserAddVO systemUserAddVO);

    /**
     * 批量删除用户
     * @param ids
     * @return
     */
    int deleteByIds(List<Long> ids);

    /**
     * 修改用户
     * @param systemUserUpdateVO
     * @return
     */
    int modifyById(SystemUserUpdateVO systemUserUpdateVO);

    /**
     * 查询用户列表
     * @param systemUserQueryVO
     * @return
     */
    List<SystemUserDO> getList(SystemUserQueryVO systemUserQueryVO);

    /**
     * 重置密码
     * @param userId
     * @param newPassword
     * @return
     */
    int resetPassword(Long userId, String newPassword);

    /**
     * 根据id查询用户详情（含角色id列表）
     * @param userId
     * @return
     */
    SystemUserDetailVO getDetailById(Long userId);
}
