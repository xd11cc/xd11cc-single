package com.xd11cc.single.config.netty;

import com.xd11cc.single.config.properties.NettyProperties;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.Executor;

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
    private EventLoopGroup workerGroup = new NioEventLoopGroup(Runtime.getRuntime().availableProcessors());

    @Autowired
    private WebSocketChannelInitializer webSocketChannelInitializer;
    @Autowired
    private NettyProperties nettyProperties;
    @Autowired
    private Executor nettyTaskExecutor;

    @PostConstruct
    public void start() {
        // 异步启动，避免阻塞Spring上下文初始化
        nettyTaskExecutor.execute(() -> {
            try {
                ServerBootstrap bootstrap = new ServerBootstrap();
                bootstrap.group(bossGroup, workerGroup)
                        .channel(NioServerSocketChannel.class)
                        .handler(new LoggingHandler(LogLevel.INFO))
                        .childHandler(webSocketChannelInitializer)
                        // 优化TCP参数
                        .option(ChannelOption.SO_BACKLOG, 1024) // 连接队列大小
                        .childOption(ChannelOption.SO_KEEPALIVE, true) // 开启TCP爆火
                        .childOption(ChannelOption.TCP_NODELAY, true) // 禁用Nagle算法，降低延迟
                        .childOption(ChannelOption.SO_REUSEADDR, true); // 允许端口复用

                ChannelFuture future = bootstrap.bind(nettyProperties.getPort()).sync();
                log.info("netty webSocket server 启动成功，端口： {}", nettyProperties.getPort());
                future.channel().closeFuture().addListener(f ->{
                    shutdownGracefully();
                    log.info("netty webSocket server 已关闭");
                });
            } catch (InterruptedException e) {
                log.error("netty webSocket server 启动失败", e);
                Thread.currentThread().interrupt();
                shutdownGracefully();
            } catch (Exception e) {
                log.error("netty webSocket server 启动异常", e);
                shutdownGracefully();
            }
        });
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
