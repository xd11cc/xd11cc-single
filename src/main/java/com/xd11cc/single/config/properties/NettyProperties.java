package com.xd11cc.single.config.properties;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author xd11cc
 * @date 2026-03-30 22:01:49
 * @description
 */
@Data
@NoArgsConstructor
@ConfigurationProperties("netty.websocket")
@Component
public class NettyProperties {

    /**
     * 服务端口
     */
    private int port;

    /**
     * 读空闲时间（超过此时间无任何数据接收，触发读空闲事件）
     */
    private int readerIdleTime = 60;

    /**
     * 写空闲时间（超过此时间无写出，主动发送PING探活）
     */
    private int writerIdleTime = 25;

    /**
     * WebSocket路径
     */
    private String webSocketPath = "/ws";

    /**
     * 消息最大长度
     */
    private int maxFrameLength = 65536;
}
