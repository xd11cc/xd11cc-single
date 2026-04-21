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

import java.net.SocketAddress;


/**
 * @author xd11cc
 * @date 2026-03-26 18:47:45
 * @description webSocket业务处理器
 */
@Slf4j
@Component
@ChannelHandler.Sharable
public class WebSocketServerHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    private static final AttributeKey<Integer> READ_IDLE_COUNT = AttributeKey.valueOf("READ_IDLE_COUNT");
    // 最大读空闲次数
    private static final int MAX_READ_IDLE_COUNT = 3;

    private static final AttributeKey<Integer> RESPONSE_COUNT = AttributeKey.valueOf("RESPONSE_COUNT");
    // 最大响应PONG次数
    private static final int MAX_RESPONSE_COUNT = 5;

    @Autowired
    private ChannelManager channelManager;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) {
        try {
            // 处理收到的消息，路由到不同推送维度
            String message = msg.text();
            log.info("[netty]接收前端消息：{}", message);
            // 心跳响应
            if ("PING".equals(message)) {
                int count = ctx.channel().attr(RESPONSE_COUNT).get() == null ? 0 : ctx.channel().attr(RESPONSE_COUNT).get();
                count++;
                ctx.channel().attr(RESPONSE_COUNT).set(count);
                // 当前端稳定发送ping，清空读空闲次数
                if (count >= MAX_RESPONSE_COUNT && null != ctx.channel().attr(READ_IDLE_COUNT).get()) {
                    ctx.channel().attr(READ_IDLE_COUNT).set(0);
                }
                ctx.writeAndFlush(new TextWebSocketFrame("PONG"));
                return;
            }
            // 解析消息
            NettyMessageDTO nettyMessageDTO = null;
            try {
                nettyMessageDTO = JSONObject.parseObject(message, NettyMessageDTO.class);
            } catch (Exception e) {
                log.error("[netty] 消息解析失败，内容:{}", message, e);
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
            log.error("[netty] 消息处理失败", e);
            ctx.writeAndFlush(new TextWebSocketFrame("处理失败：" + e.getMessage()));
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        // 空闲事件：读空闲关闭，写空闲发心跳
        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            Channel channel = ctx.channel();
            SocketAddress remoteAddress = channel.remoteAddress();
            if (state == IdleState.READER_IDLE) {
                // 读空闲：累计次数，超过阈值关闭
                int count = channel.attr(READ_IDLE_COUNT).get() == null ? 0 : channel.attr(READ_IDLE_COUNT).get();
                count++;
                channel.attr(READ_IDLE_COUNT).set(count);
                log.warn("[netty] 读空闲触发 ｜ 远程地址:{} | 当前次数:{}/{}", remoteAddress, count, MAX_READ_IDLE_COUNT);
                if (count >= MAX_READ_IDLE_COUNT) {
                    log.error("[netty] 读空闲次数超限，关闭连接 ｜ 远程地址：{}", remoteAddress);
                    channel.close();
                }
            } else if (state == IdleState.WRITER_IDLE) {
                // 写空闲发送心跳PING
                ctx.writeAndFlush(new PingWebSocketFrame()).addListener(future -> {
                    if (!future.isSuccess()) {
                        log.error("[netty] 发送心跳PING失败", future.cause());
                        channel.close();
                    }else {
                        log.info("[netty] 发送心跳成功");
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
        log.info("[netty] 通道关闭:{}", ctx.channel().id());
        channelManager.removeChannel(ctx.channel());
        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // 异常处理：清理通道+关闭连接
        log.error("[netty] 通道异常:{}", ctx.channel().id(), cause);
        channelManager.removeChannel(ctx.channel());
        ctx.close();
    }
}
