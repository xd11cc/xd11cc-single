package com.xd11cc.single.controller;

import com.xd11cc.single.common.annotation.OperateLog;
import com.xd11cc.single.entity.base.PageResult;
import com.xd11cc.single.entity.base.ResponseVO;
import com.xd11cc.single.entity.domain.MqFailMessageDO;
import com.xd11cc.single.entity.vo.MqFailMessageQueryVO;
import com.xd11cc.single.entity.vo.MqFailReplayEditVO;
import com.xd11cc.single.enums.OperateTypeEnum;
import com.xd11cc.single.service.IMqFailMessageService;
import com.xd11cc.single.utils.PageUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * MQ 失败消息管理。
 * <p>查询消费重试仍失败的消息，支持重新投递与忽略。</p>
 *
 * @author xd11cc
 */
@Api(tags = "MQ失败消息管理")
@Validated
@RestController
@RequestMapping("/system/mq-fail")
public class MqFailMessageController {

    @Autowired
    private IMqFailMessageService mqFailMessageService;

    @ApiOperation("MQ失败消息分页")
    @PostMapping("/page")
    @PreAuthorize("@ss.hasPermission('system:mqFail:list')")
    public ResponseVO<PageResult<MqFailMessageDO>> page(@Valid @RequestBody MqFailMessageQueryVO queryVO) {
        return PageUtils.page(queryVO, () -> mqFailMessageService.getList(queryVO));
    }

    @ApiOperation("重新投递失败消息")
    @PostMapping("/replay/{id}")
    @PreAuthorize("@ss.hasPermission('system:mqFail:replay')")
    @OperateLog(module = "MQ失败消息", operateType = OperateTypeEnum.UPDATE)
    public ResponseVO<Void> replay(@PathVariable @NotNull Long id) {
        mqFailMessageService.replay(id);
        return ResponseVO.success(null, "重放成功");
    }

    @ApiOperation("编辑消息体后重新投递")
    @PostMapping("/replay-edit/{id}")
    @PreAuthorize("@ss.hasPermission('system:mqFail:replay')")
    @OperateLog(module = "MQ失败消息", operateType = OperateTypeEnum.UPDATE)
    public ResponseVO<Void> replayWithEdit(@PathVariable @NotNull Long id,
                                           @RequestBody @Valid MqFailReplayEditVO replayEditVO) {
        mqFailMessageService.replayWithEdit(id, replayEditVO.getData());
        return ResponseVO.success(null, "重放成功");
    }

    @ApiOperation("忽略失败消息")
    @PostMapping("/ignore/{id}")
    @PreAuthorize("@ss.hasPermission('system:mqFail:ignore')")
    @OperateLog(module = "MQ失败消息", operateType = OperateTypeEnum.UPDATE)
    public ResponseVO<Void> ignore(@PathVariable @NotNull Long id) {
        mqFailMessageService.ignore(id);
        return ResponseVO.success(null, "已忽略");
    }
}
