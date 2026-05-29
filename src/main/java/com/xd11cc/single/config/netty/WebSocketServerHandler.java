package com.xd11cc.single.config.netty;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.xd11cc.single.entity.dto.NettyMessageDTO;
import com.xd11cc.single.entity.dto.WsPushMessage;
import com.xd11cc.single.enums.WebSocketEnum;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.AttributeKey;
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

    @Autowired
    private ChannelManager channelManager;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) {
        try {
            String message = msg.text();

            // 心跳响应：客户端发 PING，回复 PONG
            if ("PING".equals(message)) {
                ctx.writeAndFlush(new TextWebSocketFrame("PONG"));
                return;
            }
            // 心跳确认：客户端回复 PONG（证明连接存活，但不重置读空闲计数）
            if ("PONG".equals(message)) {
                return;
            }

            // 收到业务消息才重置读空闲计数
            ctx.channel().attr(READ_IDLE_COUNT).set(0);
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
            String pushMessage = JSON.toJSONString(WsPushMessage.of("BUSINESS", nettyMessageDTO));
            switch (type) {
                case USER:
                    channelManager.pushToUser(nettyMessageDTO.getUserId(), pushMessage);
                    break;
                case TENANT:
                    channelManager.broadcastToTenant(nettyMessageDTO.getTenantId(), pushMessage);
                    break;
                case GLOBAL:
                    channelManager.broadcastToGlobal(pushMessage);
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
                // 写空闲发送文本心跳PING
                ctx.writeAndFlush(new TextWebSocketFrame("PING")).addListener(future -> {
                    if (!future.isSuccess()) {
                        log.error("[netty] 发送心跳PING失败", future.cause());
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
