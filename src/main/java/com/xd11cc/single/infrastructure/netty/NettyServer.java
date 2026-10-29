package com.xd11cc.single.infrastructure.netty;

import com.xd11cc.single.common.properties.NettyProperties;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.channel.WriteBufferWaterMark;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * @author xd11cc
 * @date 2026-03-26 18:18:43
 * @description netty webSocket 服务端核心类
 */
@Slf4j
@Component
public class NettyServer {

    // 处理连接请求，线程固定1
    private EventLoopGroup bossGroup = new NioEventLoopGroup(1);
    // 处理读写请求，线程数默认CPU核心数 * 2
    private EventLoopGroup workerGroup = new NioEventLoopGroup();

    @Autowired
    private WebSocketChannelInitializer webSocketChannelInitializer;
    @Autowired
    private NettyProperties nettyProperties;

    @PostConstruct
    public void start() {
        // 同步绑定：端口不可用时让 Spring 上下文启动失败。
        // 早先是「丢进线程池异步启动」，绑定异常抛在任务线程里，
        // 应用照常启动成功但 WebSocket 静默不可用——比启动失败更难排查。
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(webSocketChannelInitializer)
                    // 优化TCP参数
                    .option(ChannelOption.SO_BACKLOG, 1024) // 连接队列大小
                    .childOption(ChannelOption.SO_KEEPALIVE, true) // 开启TCP保活
                    .childOption(ChannelOption.TCP_NODELAY, true) // 禁用Nagle算法，降低延迟
                    .childOption(ChannelOption.SO_REUSEADDR, true) // 允许端口复用
                    // 写缓冲水位线：超过高水位的连接会被标记为不可写，推送时跳过
                    .childOption(ChannelOption.WRITE_BUFFER_WATER_MARK,
                            new WriteBufferWaterMark(nettyProperties.getWriteBufferLowWaterMark(),
                                    nettyProperties.getWriteBufferHighWaterMark()));

            ChannelFuture future = bootstrap.bind(nettyProperties.getPort()).sync();
            log.info("netty webSocket server 启动成功，端口： {}，写缓冲水位：{}/{} 字节，"
                            + "消息限流：{}条/{}秒（{}）",
                    nettyProperties.getPort(),
                    nettyProperties.getWriteBufferLowWaterMark(),
                    nettyProperties.getWriteBufferHighWaterMark(),
                    nettyProperties.getRateLimit().getCount(),
                    nettyProperties.getRateLimit().getIntervalSecond(),
                    nettyProperties.getRateLimit().isEnabled() ? "启用" : "关闭");
            future.channel().closeFuture().addListener(f -> {
                shutdownGracefully();
                log.info("netty webSocket server 已关闭");
            });
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            shutdownGracefully();
            throw new IllegalStateException(
                    "WebSocket 服务启动被中断，端口：" + nettyProperties.getPort(), e);
        } catch (Exception e) {
            shutdownGracefully();
            throw new IllegalStateException(
                    "WebSocket 服务启动失败，端口 " + nettyProperties.getPort() + " 不可用（可能被占用）", e);
        }
    }

    @PreDestroy
    public void stop() {
        log.info("netty webSocket server 开始关闭");
        shutdownGracefully();
        log.info("netty webSocket server 关闭完成");
    }

    /**
     * 优雅关闭EventLoopGroup
     */
    private void shutdownGracefully() {
        if (null != bossGroup) {
            bossGroup.shutdownGracefully();
        }
        if (null != workerGroup) {
            workerGroup.shutdownGracefully();
        }
    }
}
