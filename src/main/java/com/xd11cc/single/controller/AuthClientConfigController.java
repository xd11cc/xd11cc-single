package com.xd11cc.single.controller;

import com.xd11cc.single.convert.AuthClientConfigConvert;
import com.xd11cc.single.entity.base.ResponseVO;
import com.xd11cc.single.entity.domain.AuthClientConfigDO;
import com.xd11cc.single.entity.vo.AuthClientConfigAddVO;
import com.xd11cc.single.entity.vo.AuthClientConfigListVO;
import com.xd11cc.single.entity.vo.AuthClientConfigQueryVO;
import com.xd11cc.single.entity.vo.AuthClientConfigUpdateVO;
import com.xd11cc.single.service.IAuthClientConfigService;
import com.xd11cc.single.utils.PageUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author xd11cc
 * @date 2026-05-20
 */
@Validated
@RestController
@RequestMapping("/auth/client/config")
@Api(tags = "授权应用配置管理")
public class AuthClientConfigController {

    @Autowired
    private IAuthClientConfigService authClientConfigService;

    @GetMapping("/list")
    @ApiOperation("获取授权配置信息（登录页）")
    public ResponseVO<List<AuthClientConfigListVO>> list() {
        List<AuthClientConfigListVO> list = authClientConfigService.list().stream()
                .map(AuthClientConfigConvert.CONVERT::do2listVO)
                .collect(Collectors.toList());
        return ResponseVO.success(list);
    }

    @ApiOperation("新增授权应用")
    @PostMapping("/add")
    @PreAuthorize("@ss.hasPermission('auth:clientConfig:add')")
    public ResponseVO<Integer> add(@Valid @RequestBody AuthClientConfigAddVO authClientConfigAddVO) {
        int row = authClientConfigService.add(authClientConfigAddVO);
        if (row > 0) {
            return ResponseVO.success(row, "新增成功");
        } else {
            return ResponseVO.fail("新增失败");
        }
    }

    @ApiOperation("删除授权应用")
    @GetMapping("/removeByIds/{ids}")
    @PreAuthorize("@ss.hasPermission('auth:clientConfig:delete')")
    public ResponseVO<Integer> removeByIds(@PathVariable("ids") List<Long> ids) {
        int row = authClientConfigService.deleteByIds(ids);
        if (row > 0) {
            return ResponseVO.success(row, "删除成功");
        } else {
            return ResponseVO.fail("删除失败");
        }
    }

    @ApiOperation("修改授权应用")
    @PostMapping("/modifyById")
    @PreAuthorize("@ss.hasPermission('auth:clientConfig:update')")
    public ResponseVO<Integer> modifyById(@Valid @RequestBody AuthClientConfigUpdateVO authClientConfigUpdateVO) {
        int row = authClientConfigService.modifyById(authClientConfigUpdateVO);
        if (row > 0) {
            return ResponseVO.success(row, "修改成功");
        } else {
            return ResponseVO.fail("修改失败");
        }
    }

    @ApiOperation("授权应用分页")
    @PostMapping("/page")
    public ResponseVO<List<AuthClientConfigDO>> page(@Valid @RequestBody AuthClientConfigQueryVO authClientConfigQueryVO) {
        return PageUtils.page(authClientConfigQueryVO, () -> authClientConfigService.getPageList(authClientConfigQueryVO));
    }
}
