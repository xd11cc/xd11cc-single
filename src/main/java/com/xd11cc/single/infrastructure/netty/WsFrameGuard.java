package com.xd11cc.single.infrastructure.netty;

import com.xd11cc.single.common.properties.NettyProperties;
import io.netty.channel.Channel;
import io.netty.util.AttributeKey;
import org.springframework.stereotype.Component;

/**
 * 每连接帧速率守卫。
 * <p>
 * 与 {@link WsRateLimiter} 互补，两者拦住的是不同东西：
 * </p>
 * <ul>
 *     <li>{@link WsRateLimiter} 按发送者限流，只作用于<b>解析成功且身份合法</b>的业务消息</li>
 *     <li>本类按连接做总闸，<b>心跳与非法帧同样计数</b></li>
 * </ul>
 * <p>
 * 为什么需要后者：非法帧、缺字段帧、心跳都不会经过按发送者限流，客户端可以靠它们
 * 无限索取 PONG / ERROR 响应，成本极低。这里用内存计数（不查 Redis）挡住——
 * 对垃圾帧不值得付出一次 Redis 往返。
 * </p>
 * <p>
 * 状态挂在 channel 属性上，随连接回收，无需额外的清理逻辑。
 * </p>
 *
 * @author xd11cc
 */
@Component
public class WsFrameGuard {

    /**
     * 当前窗口的计数状态，随通道存活
     */
    private static final AttributeKey<Window> FRAME_WINDOW = AttributeKey.valueOf("FRAME_WINDOW");

    private final NettyProperties nettyProperties;

    public WsFrameGuard(NettyProperties nettyProperties) {
        this.nettyProperties = nettyProperties;
    }

    /**
     * 记录一帧并判断是否放行。
     * <p>
     * 入站处理始终在通道所属 EventLoop 线程上执行，因此窗口状态无需加锁。
     * </p>
     *
     * @param channel 连接通道
     * @return true 放行；false 超限，调用方应断开连接
     */
    public boolean allow(Channel channel) {
        NettyProperties.FrameLimit config = nettyProperties.getFrameLimit();
        if (config == null || !config.isEnabled()) {
            return true;
        }

        long now = System.currentTimeMillis();
        long windowMillis = config.getIntervalSecond() * 1000L;

        Window window = channel.attr(FRAME_WINDOW).get();
        if (window == null || now - window.windowStart >= windowMillis) {
            // 新窗口：重新计数
            window = new Window(now);
            channel.attr(FRAME_WINDOW).set(window);
        }

        window.count++;
        return window.count <= config.getCount();
    }

    /**
     * 窗口内的帧计数
     */
    private static final class Window {

        private final long windowStart;

        private int count;

        private Window(long windowStart) {
            this.windowStart = windowStart;
        }
    }
}
