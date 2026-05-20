package com.xd11cc.single.controller;

import com.xd11cc.single.convert.AuthClientConfigConvert;
import com.xd11cc.single.entity.base.ResponseVO;
import com.xd11cc.single.entity.domain.AuthClientConfigDO;
import com.xd11cc.single.entity.vo.AuthClientConfigListVO;
import com.xd11cc.single.service.IAuthClientConfigService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

/**
* @author xd11cc
* @date 2026-05-20
*/
@RestController
@RequestMapping("/auth/client/config")
@Api(tags = "授权应用配置管理")
public class AuthClientConfigController {

    @Autowired
    private IAuthClientConfigService authClientConfigService;

    @GetMapping("/list")
    @ApiOperation("获取授权配置信息")
    public ResponseVO<List<AuthClientConfigListVO>> list(){
        List<AuthClientConfigListVO> list = new ArrayList<>();
        for (AuthClientConfigDO authClientConfigDO : authClientConfigService.list()) {
            list.add(AuthClientConfigConvert.CONVERT.do2listVO(authClientConfigDO));
        }
        return ResponseVO.success(list);
    }
}