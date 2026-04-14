package com.xd11cc.single.config.netty;

import com.alibaba.fastjson2.JSONObject;
import com.xd11cc.single.entity.dto.NettyMessageDTO;
import com.xd11cc.single.enums.WebSocketEnum;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.ScheduledFuture;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;


/**
 * @author xd11cc
 * @date 2026-03-26 18:47:45
 * @description webSocket业务处理器
 */
@Slf4j
@Component
@ChannelHandler.Sharable
public class WebSocketServerHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    private static final AttributeKey<ScheduledFuture<?>> CLOSE_TASK_KEY = AttributeKey.valueOf("close_task");

    @Autowired
    private ChannelManager channelManager;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) {
        try {
            // 处理收到的消息，路由到不同推送维度
            String message = msg.text();
            // 心跳响应
            if ("PING".equals(message)) {
                ctx.writeAndFlush(new TextWebSocketFrame("PONG"));
                return;
            }
            // 解析消息
            NettyMessageDTO nettyMessageDTO = null;
            try {
                nettyMessageDTO = JSONObject.parseObject(message, NettyMessageDTO.class);
            } catch (Exception e) {
                if ("PONG".equals(message)) {
                    ScheduledFuture<?> closeTask = ctx.channel().attr(CLOSE_TASK_KEY).getAndSet(null);
                    if (null != closeTask && !closeTask.isDone()) {
                        // 取消任务，连接保留
                        closeTask.cancel(false);
                    }
                    return;
                }
                log.error(" [netty] 消息解析失败，内容:{}", message, e);
                ctx.writeAndFlush(new TextWebSocketFrame("消息格式错误" + e.getMessage()));
                return;
            }
            // 路由消息推送
            if (nettyMessageDTO == null || nettyMessageDTO.getWebSocketEnum() == null) {
                ctx.writeAndFlush(new TextWebSocketFrame("消息内容为空"));
                return;
            }
            WebSocketEnum type = nettyMessageDTO.getWebSocketEnum();
            switch (type) {
                case USER:
                    channelManager.pushToUser(nettyMessageDTO.getUserId(), nettyMessageDTO.getContent());
                    break;
                case TENANT:
                    channelManager.broadcastToTenant(nettyMessageDTO.getTenantId(), nettyMessageDTO.getContent());
                    break;
                case GLOBAL:
                    channelManager.broadcastToGlobal(nettyMessageDTO.getContent());
                    break;
                default:
                    ctx.writeAndFlush(new TextWebSocketFrame("不支持的消息类型" + type));
            }
        } catch (Exception e) {
            log.error(" [netty] 消息处理失败", e);
            ctx.writeAndFlush(new TextWebSocketFrame("处理失败：" + e.getMessage()));
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        // 空闲事件：读空闲关闭，写空闲发心跳
        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            Channel channel = ctx.channel();
            if (state == IdleState.READER_IDLE) {
                // 发送原生PING帧，等待PONG响应
                channel.writeAndFlush(new TextWebSocketFrame("PING")).addListener(future -> {
                    if (!future.isSuccess()) {
                        log.error(" [netty] 发送检测PING失败，关闭连接");
                        // 关闭连接，出发channelInactive清理
                        channel.close();
                    }
                    // 5秒内未收到PONG则关闭
                    ScheduledFuture<?> closeTask = channel.eventLoop().schedule(() -> {
                        if (channel.isActive()) {
                            log.warn(" [netty] PING检测无响应，关闭连接:{}", channel.id());
                            channel.close();
                        }
                    }, 5, TimeUnit.SECONDS);
                    // 将任务绑定到channel，后续收到PONG取消
                    log.info(" [netty] 检测到PING信息...");
                    channel.attr(CLOSE_TASK_KEY).set(closeTask);
                });
            } else if (state == IdleState.WRITER_IDLE) {
                // 写空闲发送心跳PING
                ctx.writeAndFlush(new TextWebSocketFrame("PING")).addListener(future -> {
                    if (!future.isSuccess()) {
                        log.error(" [netty] 发送心跳PING失败", future.cause());
                        channel.close();
                    }
                });
            }
        } else {
            // 透传其他事件
            ctx.fireUserEventTriggered(evt);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info(" [netty] 通道关闭:{}", ctx.channel().id());
        // 连接关闭，取消任务
        ScheduledFuture<?> closeTask = ctx.channel().attr(CLOSE_TASK_KEY).getAndSet(null);
        if (closeTask != null) {
            closeTask.cancel(false);
        }
        channelManager.removeChannel(ctx.channel());
        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // 异常处理：清理通道+关闭连接
        log.error(" [netty] 通道异常:{}", ctx.channel().id(), cause);
        channelManager.removeChannel(ctx.channel());
        ctx.close();
    }
}
