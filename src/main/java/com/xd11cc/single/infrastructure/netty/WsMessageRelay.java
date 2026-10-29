package com.xd11cc.single.infrastructure.netty;

import com.xd11cc.single.entity.dto.LoginUserDTO;
import com.xd11cc.single.entity.dto.WsPushMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 客户端消息转发。
 * <p>
 * 把「谁发的、发给谁」这层语义从传输处理器里拿出来：handler 只负责解析帧与回执，
 * 本类负责组装信封、写入可信的发送者身份、按发送方租户转发。
 * </p>
 * <p>
 * 这样做的直接收益是<b>可测</b>——不需要 {@code EmbeddedChannel} 和通道属性，
 * 直接构造 {@link LoginUserDTO} 调 {@link #relay} 即可钉住行为。
 * </p>
 *
 * @author xd11cc
 */
@Slf4j
@Component
public class WsMessageRelay {

    private final ChannelManager channelManager;

    private final WsRateLimiter wsRateLimiter;

    public WsMessageRelay(ChannelManager channelManager, WsRateLimiter wsRateLimiter) {
        this.channelManager = channelManager;
        this.wsRateLimiter = wsRateLimiter;
    }

    /**
     * 转发一条客户端消息给同租户的另一个用户。
     * <p>
     * 发送者身份取自连接上的登录信息，<b>不采信报文里的自述身份</b>；
     * 载荷原样透传，本类不解析其结构。
     * </p>
     *
     * @param from     发送者（来自连接属性）
     * @param toUserId 目标用户
     * @param payload  业务载荷
     * @return 是否真正写入目标通道
     */
    public boolean relay(LoginUserDTO from, Long toUserId, Object payload) {
        if (from == null || from.getUserId() == null || from.getSystemUserDO() == null) {
            log.warn("[netty] 转发失败：发送者身份缺失");
            return false;
        }
        if (toUserId == null) {
            log.warn("[netty] 转发失败：目标用户为空，sender={}", from.getUserId());
            return false;
        }

        Long tenantId = from.getSystemUserDO().getTenantId();

        // 防刷屏：按发送者限流，超限静默丢弃（不回执，避免替攻击者放大流量）
        if (!wsRateLimiter.tryAcquire(tenantId, from.getUserId())) {
            return false;
        }

        WsPushMessage envelope = WsPushMessage.of("BUSINESS", payload);
        envelope.setFrom(new WsPushMessage.Sender(from.getUserId(), from.getUsername()));

        // 租户校验与投递由 ChannelManager 负责，跨租户会被丢弃
        return channelManager.pushToUser(toUserId, tenantId, envelope);
    }
}
