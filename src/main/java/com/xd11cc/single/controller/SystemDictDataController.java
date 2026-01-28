package com.xd11cc.single.controller;

import com.xd11cc.single.entity.base.ResponseVO;
import com.xd11cc.single.entity.vo.SystemDictDataAddVO;
import com.xd11cc.single.service.ISystemDictDataService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * @author xd11cc
 * @date 2026-01-28 15:26:11
 * @description
 */
@Api(tags = "字典数据管理")
@Validated
@RestController
@RequestMapping("/dict/data")
public class SystemDictDataController {

    @Autowired
    private ISystemDictDataService systemDictDataService;

    public ResponseVO add(@Valid @RequestBody SystemDictDataAddVO systemDictDataAddVO){

        return ResponseVO.success();
    }
}
