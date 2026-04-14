package com.xd11cc.single.config.netty;

import com.xd11cc.single.config.properties.NettyProperties;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.cors.CorsConfig;
import io.netty.handler.codec.http.cors.CorsConfigBuilder;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.timeout.IdleStateHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * @author xd11cc
 * @date 2026-03-26 18:27:17
 * @description webSocket通道初始化器
 */
@Component
public class WebSocketChannelInitializer extends ChannelInitializer<SocketChannel> {

    @Autowired
    private NettyProperties nettyProperties;
    @Autowired
    private WebSocketServerHandler webSocketServerHandler;
    @Autowired
    private WebSocketAuthHandler webSocketAuthHandler;

    @Override
    protected void initChannel(SocketChannel socketChannel) {
        ChannelPipeline pipeline = socketChannel.pipeline();
        // 心跳检测（读空闲 30s，写空闲 60s）
        pipeline.addLast(new IdleStateHandler(
                nettyProperties.getReaderIdleTime(),
                nettyProperties.getWriterIdleTime(),
                0, TimeUnit.SECONDS));
        // HTTP 编解码器（WebSocket 握手依赖HTTP）
        pipeline.addLast(new HttpServerCodec());
        // 聚合 HTTP 消息
        pipeline.addLast(new HttpObjectAggregator(nettyProperties.getMaxFrameLength()));
        pipeline.addLast(webSocketAuthHandler);
        // WebSocket 协议处理器
        pipeline.addLast(new WebSocketServerProtocolHandler(
                nettyProperties.getWebSocketPath(), // WebSocket路径
                null, // 子协议
                true, // 允许扩展
                nettyProperties.getMaxFrameLength() * 10, // 最大帧大小
                false, // 不允许异步握手
                true)); // 自动处理PING/PONG
        // 自定义业务处理器
        pipeline.addLast(webSocketServerHandler);
    }
}
