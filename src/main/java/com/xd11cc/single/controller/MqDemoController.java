package com.xd11cc.single.controller;

import com.xd11cc.single.common.mq.MqMessageSender;
import com.xd11cc.single.common.mq.comsumer.DemoDelayConsumer;
import com.xd11cc.single.common.mq.comsumer.DemoMessage;
import com.xd11cc.single.common.mq.enums.MqQueueEnum;
import com.xd11cc.single.entity.base.ResponseVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.TimeUnit;

/**
 * MQ 基础设施演示接口。
 * <p>
 * 仅用于本地验证「发送 → 消费 → 重试 → 失败落表 → 死信」全链路，业务接入后可删除。
 * </p>
 *
 * @author xd11cc
 */
@Api(tags = "MQ演示（临时）")
@RestController
@RequestMapping("/mq-demo")
public class MqDemoController {

    @Autowired
    private MqMessageSender mqMessageSender;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @ApiOperation("发送普通消息（tenantId 取自当前登录上下文）")
    @PostMapping("/send")
    public ResponseVO<String> send(@RequestBody DemoMessage message) {
        if (message.getBizId() == null) {
            message.setBizId("demo-" + System.currentTimeMillis());
        }
        mqMessageSender.send(MqQueueEnum.DEMO, message);
        return ResponseVO.success(message.getMessageId(), "已发送");
    }

    @ApiOperation("发送必定失败的消息（验证重试 + 失败落表 + 死信）")
    @PostMapping("/send-fail")
    public ResponseVO<String> sendFail(@RequestBody(required = false) DemoMessage message) {
        DemoMessage msg = message != null ? message : new DemoMessage();
        msg.setBizId("fail-" + System.currentTimeMillis());
        msg.setContent("演示失败消息");
        msg.setSimulateFail(true);
        mqMessageSender.send(MqQueueEnum.DEMO, msg);
        return ResponseVO.success(msg.getMessageId(), "已发送失败消息");
    }

    @ApiOperation("重复发送同一条消息（验证幂等：使用相同 messageId）")
    @PostMapping("/send-duplicate")
    public ResponseVO<String> sendDuplicate(@RequestParam(defaultValue = "3") int times) {
        // 复用同一个 messageId，模拟 broker 重投 / 人工重放
        String sharedMessageId = java.util.UUID.randomUUID().toString();
        String bizId = "dup-" + System.currentTimeMillis();
        for (int i = 0; i < times; i++) {
            DemoMessage copy = new DemoMessage();
            copy.setMessageId(sharedMessageId);
            copy.setBizId(bizId);
            copy.setContent("幂等演示");
            mqMessageSender.send(MqQueueEnum.DEMO, copy);
        }
        return ResponseVO.success(sharedMessageId, "已发送 " + times + " 条相同 messageId 的消息");
    }

    @ApiOperation("事务提交后发送（send 在事务中的行为验证）")
    @PostMapping("/send-after-commit")
    public ResponseVO<String> sendAfterCommit(@RequestBody(required = false) DemoMessage message,
                                              @RequestParam(defaultValue = "false") boolean rollback) {
        DemoMessage msg = message != null ? message : new DemoMessage();
        msg.setBizId("tx-" + System.currentTimeMillis());
        msg.setContent(rollback ? "事务将回滚" : "事务将提交");

        try {
            transactionTemplate.executeWithoutResult(status -> {
                mqMessageSender.send(MqQueueEnum.DEMO, msg);
                if (rollback) {
                    throw new IllegalStateException("【演示】模拟事务回滚");
                }
            });
        } catch (IllegalStateException e) {
            return ResponseVO.success(msg.getMessageId(), "事务已回滚，消息未发送: " + e.getMessage());
        }
        return ResponseVO.success(msg.getMessageId(), "事务已提交，消息已发送");
    }

    @ApiOperation("发送延迟消息（秒），由 DemoDelayConsumer 消费")
    @PostMapping("/send-delay")
    public ResponseVO<String> sendDelay(@RequestParam long seconds,
                                        @RequestBody(required = false) DemoMessage message) {
        DemoMessage msg = message != null ? message : new DemoMessage();
        if (msg.getBizId() == null) {
            msg.setBizId("delay-" + System.currentTimeMillis());
        }
        if (msg.getContent() == null) {
            msg.setContent("延迟 " + seconds + " 秒");
        }
        mqMessageSender.sendDelay(MqQueueEnum.DEMO_DELAY, msg, seconds, TimeUnit.SECONDS);
        return ResponseVO.success(msg.getMessageId(),
                "已发送延迟消息，" + seconds + " 秒后由 " + DemoDelayConsumer.QUEUE_NAME + " 消费");
    }

    @ApiOperation("延迟消息失败场景（验证延迟队列的重试 + 落表 + 死信）")
    @PostMapping("/send-delay-fail")
    public ResponseVO<String> sendDelayFail(@RequestParam(defaultValue = "3") long seconds) {
        DemoMessage msg = new DemoMessage();
        msg.setBizId("delay-fail-" + System.currentTimeMillis());
        msg.setContent("延迟 " + seconds + " 秒后必定失败");
        msg.setSimulateFail(true);
        mqMessageSender.sendDelay(MqQueueEnum.DEMO_DELAY, msg, seconds, TimeUnit.SECONDS);
        return ResponseVO.success(msg.getMessageId(), "已发送延迟失败消息");
    }
}
