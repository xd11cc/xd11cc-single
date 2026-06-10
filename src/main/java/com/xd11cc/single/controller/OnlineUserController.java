package com.xd11cc.single.controller;

import com.xd11cc.single.config.RedisCache;
import com.xd11cc.single.constants.CacheConstants;
import com.xd11cc.single.entity.base.ResponseVO;
import com.xd11cc.single.entity.dto.LoginUserDTO;
import com.xd11cc.single.entity.vo.OnlineUserVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * @author xd11cc
 * @date 2026-05-28
 */
@RestController
@RequestMapping("/system/onlineUser")
@Api(tags = "在线用户")
public class OnlineUserController {

    @Autowired
    private RedisCache redisCache;

    @GetMapping("/list")
    @ApiOperation("在线用户列表")
    public ResponseVO<List<OnlineUserVO>> list(@RequestParam(required = false) String username) {
        Set<String> keys = redisCache.keys(CacheConstants.LOGIN_TOKEN_KEY);
        if (keys == null || keys.isEmpty()) {
            return ResponseVO.success(Collections.emptyList());
        }
        List<OnlineUserVO> list = new ArrayList<>();
        for (String key : keys) {
            LoginUserDTO loginUser = redisCache.getCacheObject(key, false);
            if (loginUser == null) {
                continue;
            }
            if (StringUtils.isNotEmpty(username) && !loginUser.getUsername().contains(username)) {
                continue;
            }
            OnlineUserVO vo = new OnlineUserVO();
            vo.setTokenId(loginUser.getToken());
            vo.setUserId(loginUser.getUserId());
            vo.setUsername(loginUser.getUsername());
            vo.setIpAddr(loginUser.getIpAddr());
            vo.setBrowser(loginUser.getBrowser());
            vo.setOs(loginUser.getOs());
            vo.setLoginTime(loginUser.getLoginTime() != null ? new Date(loginUser.getLoginTime()) : null);
            list.add(vo);
        }
        list.sort(Comparator.comparing(OnlineUserVO::getLoginTime, Comparator.nullsLast(Comparator.reverseOrder())));
        return ResponseVO.success(list);
    }

    @GetMapping("/forceLogout/{tokenId}")
    @ApiOperation("强退用户")
    @PreAuthorize("@ss.hasPermission('system:onlineUser:forceLogout')")
    public ResponseVO<Void> forceLogout(@PathVariable String tokenId) {
        LoginUserDTO loginUser = redisCache.getCacheObject(CacheConstants.LOGIN_TOKEN_KEY + tokenId);
        if (loginUser != null) {
            redisCache.removeCacheObject(CacheConstants.LOGIN_TOKEN_KEY + tokenId);
            redisCache.removeCacheObject(CacheConstants.LOGIN_USER_KEY + loginUser.getUserId());
        }
        return ResponseVO.success();
    }
}
