package com.xd11cc.single.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xd11cc.single.entity.domain.SystemUserDO;
import com.xd11cc.single.entity.vo.LoginPasswordVO;
import com.xd11cc.single.entity.vo.LoginResultVO;

/**
 * @Author: xd11cc
 * @Date: 2025/6/14 00:25
 **/
public interface ISystemUserService extends IService<SystemUserDO> {

    LoginResultVO loginByPassword(LoginPasswordVO loginPasswordVO);

    /**
     * 根据用户查询用户信息
     * @param username
     * @return
     */
    SystemUserDO getByUsername(String username);
}
