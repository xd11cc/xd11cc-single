package com.xd11cc.single.common.properties;

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
     * 写缓冲低水位（字节）：低于此值视为可写
     */
    private int writeBufferLowWaterMark = 32 * 1024;

    /**
     * 写缓冲高水位（字节）：超过此值 isWritable() 变为 false，推送会跳过该连接
     */
    private int writeBufferHighWaterMark = 256 * 1024;

    /**
     * 连接帧速率守卫：每个连接每窗口最多收多少帧（含心跳与非法帧）
     */
    private FrameLimit frameLimit = new FrameLimit();

    /**
     * 消息限流配置（按发送者）
     */
    private RateLimit rateLimit = new RateLimit();

    /**
     * 连续读空闲多少次后断开连接。
     * <p>
     * 与 {@link #readerIdleTime} 共同决定死连接判定时长：
     * 总时长 ≈ readerIdleTime × readIdleCloseCount。
     * </p>
     */
    private int readIdleCloseCount = 3;

    /**
     * WebSocket路径
     */
    private String webSocketPath = "/ws";

    /**
     * 消息最大长度
     */
    private int maxFrameLength = 65536;

    /**
     * 连接帧速率守卫。
     * <p>
     * 与按发送者限流互补：限流只保护「解析成功且身份合法的业务消息」，
     * 而心跳与非法帧不经过限流，可能被用来廉价索取响应或维持连接。
     * 这里按连接做粗粒度总闸，超限直接断开。
     * </p>
     */
    @Data
    @NoArgsConstructor
    public static class FrameLimit {

        /**
         * 是否启用
         */
        private boolean enabled = true;

        /**
         * 每窗口允许的最大帧数（含 PING/PONG 与非法帧）
         */
        private int count = 100;

        /**
         * 窗口长度（秒）
         */
        private int intervalSecond = 10;
    }

    /**
     * 消息限流配置。
     * <p>
     * 按发送者维度限流：同一用户的多个连接共享一份额度，
     * 避免多开连接绕过限制。
     * </p>
     */
    @Data
    @NoArgsConstructor
    public static class RateLimit {

        /**
         * 是否启用限流
         */
        private boolean enabled = true;

        /**
         * 每个时间窗口允许的业务消息数
         */
        private int count = 20;

        /**
         * 时间窗口长度（秒）
         */
        private int intervalSecond = 10;
    }
}
