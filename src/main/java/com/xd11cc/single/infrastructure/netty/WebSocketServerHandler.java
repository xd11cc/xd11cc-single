package com.xd11cc.single.infrastructure.netty;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.xd11cc.single.common.properties.NettyProperties;
import com.xd11cc.single.entity.dto.LoginUserDTO;
import com.xd11cc.single.entity.dto.NettyMessageDTO;
import com.xd11cc.single.entity.dto.WsPushMessage;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
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

    private final WsMessageRelay wsMessageRelay;

    /**
     * 用于握手完成时注册、断连/异常时清理通道；业务转发交给 relay
     */
    private final ChannelManager channelManager;

    private final NettyProperties nettyProperties;

    private final WsFrameGuard wsFrameGuard;

    public WebSocketServerHandler(WsMessageRelay wsMessageRelay,
                                  ChannelManager channelManager,
                                  NettyProperties nettyProperties,
                                  WsFrameGuard wsFrameGuard) {
        this.wsMessageRelay = wsMessageRelay;
        this.channelManager = channelManager;
        this.nettyProperties = nettyProperties;
        this.wsFrameGuard = wsFrameGuard;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) {
        try {
            // 每连接总闸：心跳与非法帧同样计数，防止靠垃圾帧无限索取响应
            if (!wsFrameGuard.allow(ctx.channel())) {
                log.warn("[netty] 连接帧速率超限，断开 channel={}", ctx.channel().id());
                ctx.close();
                return;
            }

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

            // 解析消息
            NettyMessageDTO nettyMessageDTO = null;
            try {
                nettyMessageDTO = JSONObject.parseObject(message, NettyMessageDTO.class);
            } catch (Exception e) {
                log.error("[netty] 消息解析失败，内容:{}", message, e);
                sendError(ctx, "消息格式错误");
                return;
            }
            // 报文只有两个字段：目标用户 + 载荷；不存在「广播类型」这种表达方式
            if (nettyMessageDTO == null) {
                sendError(ctx, "消息内容为空");
                return;
            }
            if (nettyMessageDTO.getUserId() == null) {
                sendError(ctx, "缺少目标用户");
                return;
            }
            // 只有「解析成功且目标明确」的业务帧才重置读空闲，
            // 否则客户端靠发垃圾帧就能无限保活
            ctx.channel().attr(READ_IDLE_COUNT).set(0);
            LoginUserDTO loginUser = ctx.channel().attr(ChannelManager.LOGIN_USER).get();
            if (loginUser == null || loginUser.getUserId() == null) {
                sendError(ctx, "未认证的连接");
                return;
            }
            // 转发交给 relay：身份注入与租户校验都在那里，handler 不参与
            wsMessageRelay.relay(loginUser, nettyMessageDTO.getUserId(), nettyMessageDTO.getData());
        } catch (Exception e) {
            log.error("[netty] 消息处理失败", e);
            sendError(ctx, "处理失败");
        }
    }

    /**
     * 发送标准化错误帧。
     * <p>
     * 与业务推送使用同一信封格式，客户端只需一套分发逻辑；
     * 早先发裸文本会导致前端 JSON 解析失败后被静默丢弃。
     * </p>
     */
    private void sendError(ChannelHandlerContext ctx, String message) {
        WsPushMessage error = WsPushMessage.of("ERROR",
                java.util.Collections.singletonMap("message", message));
        ctx.writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(error)));
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        // 握手完成才注册通道：此时才是真正可收发的 WebSocket 连接
        if (evt instanceof WebSocketServerProtocolHandler.HandshakeComplete) {
            registerChannel(ctx);
            return;
        }
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
                log.warn("[netty] 读空闲触发 ｜ 远程地址:{} | 当前次数:{}/{}", remoteAddress, count, nettyProperties.getReadIdleCloseCount());
                if (count >= nettyProperties.getReadIdleCloseCount()) {
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

    /**
     * 握手完成后把通道纳入推送注册表。
     * <p>
     * 注册失败直接关闭连接：与其让客户端「连上了但永远收不到消息」，
     * 不如让它断开重连并暴露问题。
     * </p>
     */
    private void registerChannel(ChannelHandlerContext ctx) {
        LoginUserDTO loginUser = ctx.channel().attr(ChannelManager.LOGIN_USER).get();
        if (loginUser == null) {
            log.error("[netty] 握手完成但缺少登录用户，关闭连接 channel={}", ctx.channel().id());
            ctx.close();
            return;
        }
        if (!channelManager.addChannel(ctx.channel(), loginUser)) {
            log.error("[netty] 通道注册失败，关闭连接 channel={}, userId={}",
                    ctx.channel().id(), loginUser.getUserId());
            ctx.close();
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
