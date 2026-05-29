package com.xd11cc.single.controller;

import com.xd11cc.single.entity.base.PageResult;
import com.xd11cc.single.entity.base.ResponseVO;
import com.xd11cc.single.entity.dto.SystemNoticeUserDTO;
import com.xd11cc.single.entity.vo.SystemNoticeSendVO;
import com.xd11cc.single.entity.vo.SystemNoticeUserQueryVO;
import com.xd11cc.single.service.ISystemNoticeUserService;
import com.xd11cc.single.utils.PageUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * @author xd11cc
 * @date 2026-05-29
 */
@RestController
@RequestMapping("/system/notice-user")
@Api(tags = "用户通知")
@Validated
public class SystemNoticeUserController {

    @Autowired
    private ISystemNoticeUserService systemNoticeUserService;

    @PostMapping("/myPage")
    @ApiOperation("我的通知分页")
    public ResponseVO<PageResult<SystemNoticeUserDTO>> myPage(@Valid @RequestBody SystemNoticeUserQueryVO queryVO) {
        return PageUtils.page(queryVO, () -> systemNoticeUserService.getMyNoticeList(queryVO));
    }

    @PostMapping("/send")
    @ApiOperation("发送用户间消息")
    public ResponseVO<Void> send(@Valid @RequestBody SystemNoticeSendVO sendVO) {
        systemNoticeUserService.send(sendVO);
        return ResponseVO.success(null, "发送成功");
    }

    @PostMapping("/markAsRead/{noticeId}")
    @ApiOperation("标记已读")
    public ResponseVO<Void> markAsRead(@PathVariable("noticeId") Long noticeId) {
        systemNoticeUserService.markAsRead(noticeId);
        return ResponseVO.success(null, "操作成功");
    }

    @PostMapping("/markAllAsRead")
    @ApiOperation("全部标记已读")
    public ResponseVO<Void> markAllAsRead(@RequestParam(value = "type", required = false) Integer type) {
        systemNoticeUserService.markAllAsRead(type);
        return ResponseVO.success(null, "操作成功");
    }

    @GetMapping("/unreadCount")
    @ApiOperation("未读数统计")
    public ResponseVO<Map<String, Integer>> unreadCount() {
        return ResponseVO.success(systemNoticeUserService.getUnreadCount());
    }
}
