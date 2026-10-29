package com.xd11cc.single.infrastructure.netty;

import com.alibaba.fastjson2.JSON;
import com.xd11cc.single.entity.dto.LoginUserDTO;
import com.xd11cc.single.entity.dto.WsPushMessage;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author xd11cc
 * @date 2026-03-26 18:41:54
 * @description 通道管理器
 */
@Slf4j
@Component
public class ChannelManager {

    /**
     * 连接上的登录用户属性。
     * <p>
     * 认证处理器写入、业务处理器读取发送者身份；两处在同一包内，故用包级可见，
     * 避免身份信息被复制到多个地方。
     * </p>
     */
    static final io.netty.util.AttributeKey<LoginUserDTO> LOGIN_USER =
            io.netty.util.AttributeKey.valueOf("LOGIN_USER");

    // 租户id -> 该租户所有通道（租户广播）
    private final ConcurrentHashMap<Long, ChannelGroup> tenantChannelsGroups = new ConcurrentHashMap<>();
    // 用户id -> 该用户所有通道（多端登陆，用户级推送）
    private final ConcurrentHashMap<Long, ChannelGroup> userChannelsGroups = new ConcurrentHashMap<>();
    // 通道 -> 租户/用户元数据（反向映射，用于断开时清理）
    private final ConcurrentHashMap<Channel, ChannelExtMetadata> channelMetadataMap = new ConcurrentHashMap<>();
    // 当前连接数
    private final AtomicInteger currentConnCount = new AtomicInteger(0);

    /**
     * 通道元数据
     */
    @Data
    @AllArgsConstructor
    public static class ChannelExtMetadata {
        private final Long tenantId;
        private final Long userId;
    }

    /**
     * 添加通道（建立连接时立即调用）
     * @param channel
     * @param loginUserDTO
     */
    public boolean addChannel(Channel channel, LoginUserDTO loginUserDTO) {
        Long tenantId = loginUserDTO.getSystemUserDO().getTenantId();
        Long userId = loginUserDTO.getUserId();
        if (tenantId == null || userId == null || channel == null) {
            log.warn("[netty] 添加通道失败，参数为空：tenantId={}, userId={}, channel={}", tenantId, userId, channel);
            return false;
        }
        // 已存在不重复添加
        if (channelMetadataMap.containsKey(channel)) {
            log.debug("[netty] 通道已存在无需添加，channel:{}", channel.id());
            return false;
        }
        try {
            // 添加到租户组
            tenantChannelsGroups.computeIfAbsent(tenantId, k ->
                    new DefaultChannelGroup(GlobalEventExecutor.INSTANCE)).add(channel);
            // 添加到用户组
            userChannelsGroups.computeIfAbsent(userId, k ->
                    new DefaultChannelGroup(GlobalEventExecutor.INSTANCE)).add(channel);
            // 记录元数据
            channelMetadataMap.put(channel, new ChannelExtMetadata(tenantId, userId));
            // 连接数 +1
            currentConnCount.incrementAndGet();
            log.info("[netty] 通道添加成功，租户：{}，用户：{}，通道：{}，当前连接数：{}", tenantId, userId, channel.id(), currentConnCount.get());
            return true;
        } catch (Exception e) {
            // 注册失败会让连接「已连上但永远收不到推送」，必须留下线索
            log.error("[netty] 通道添加异常，租户：{}，用户：{}", tenantId, userId, e);
            return false;
        }
    }

    /**
     * 移除通道（连接断开/异常时调用）
     * @param channel
     */
    public void removeChannel(Channel channel) {
        if (channel == null) return;

        ChannelExtMetadata metadata = channelMetadataMap.remove(channel);
        if (metadata == null) {
            log.debug("[netty] 通道无元数据，无需清理：channel:{}", channel.id());
            return;
        }
        Long tenantId = metadata.getTenantId();
        // 清理租户通道组（原子操作避免竞态）
        tenantChannelsGroups.computeIfPresent(tenantId, (k, group) -> {
            group.remove(channel);
            return group.isEmpty() ? null : group;
        });

        Long userId = metadata.getUserId();
        // 清理用户通道组（原子操作避免竞态）
        userChannelsGroups.computeIfPresent(userId, (k, group) -> {
            group.remove(channel);
            return group.isEmpty() ? null : group;
        });

        // 连接数 -1
        currentConnCount.decrementAndGet();
        log.info("[netty] 通道移除成功，租户：{}，用户：{}，通道：{}，当前连接数：{}", tenantId, userId, channel.id(), currentConnCount.get());
    }

    /**
     * 精确到用户推送（同租户）。
     * <p>
     * {@code tenantId} 是发送方所属租户，用于校验目标用户是否同租户。
     * 跨租户推送一律丢弃并记 warn 日志——不向调用方区分「目标离线」与「租户不符」，
     * 避免把目标用户是否存在泄露给发送方。
     * </p>
     *
     * @param userId   目标用户
     * @param tenantId 发送方租户
     * @param message  推送信封
     * @return 是否真正写入通道（false 表示目标离线或租户不符）
     */
    public boolean pushToUser(Long userId, Long tenantId, WsPushMessage message) {
        if (userId == null || tenantId == null || message == null) {
            log.warn("[netty] 消息推送失败：参数为空userId={}, tenantId={}", userId, tenantId);
            return false;
        }
        ChannelGroup userChannels = userChannelsGroups.get(userId);
        if (userChannels == null || userChannels.isEmpty()) {
            log.debug("[netty] 用户{}无在线通道，推送跳过", userId);
            return false;
        }
        ChannelExtMetadata target = firstMetadata(userChannels);
        if (target == null || !tenantId.equals(target.getTenantId())) {
            log.warn("[netty] 跨租户推送已拒绝：发送方租户={}，目标用户={}，目标租户={}",
                    tenantId, userId, target == null ? null : target.getTenantId());
            return false;
        }
        int written = writeSkippingBackpressure(userChannels, serialize(message));
        if (written == 0) {
            log.debug("[netty] 用户{}的通道全部积压，本条消息丢弃", userId);
            return false;
        }
        return true;
    }

    /**
     * 取通道组中任一通道的元数据，用于判断整组的租户归属
     */
    private ChannelExtMetadata firstMetadata(ChannelGroup group) {
        for (Channel channel : group) {
            ChannelExtMetadata metadata = channelMetadataMap.get(channel);
            if (metadata != null) {
                return metadata;
            }
        }
        return null;
    }

    /**
     * 向通道组写入，跳过积压到高水位的慢连接。
     * <p>
     * 慢消费者不会让 {@code writeAndFlush} 失败，帧只是堆在出站缓冲区里；
     * 不设闸门的话，一个网络差的客户端就能把服务端堆内存拖垮。
     * 超过高水位的连接本就不该再灌数据，跳过即可（它通常会因为读空闲超时被断开）。
     * </p>
     * <p>
     * <b>每个通道必须构造独立的帧实例</b>：{@link TextWebSocketFrame} 背后是引用计数的 ByteBuf，
     * 同一实例写给多个通道会导致重复释放。{@code ChannelGroup.writeAndFlush} 内部做了复制，
     * 这里手写遍历就得自己保证。
     * </p>
     *
     * @return 实际写入的通道数
     */
    private int writeSkippingBackpressure(ChannelGroup group, String json) {
        int written = 0;
        int skipped = 0;
        for (Channel channel : group) {
            if (!channel.isWritable()) {
                skipped++;
                continue;
            }
            channel.writeAndFlush(new TextWebSocketFrame(json)).addListener(future -> {
                if (!future.isSuccess()) {
                    log.debug("[netty] 通道写出失败 channel={}", channel.id(), future.cause());
                }
            });
            written++;
        }
        if (skipped > 0) {
            log.warn("[netty] {} 个连接积压超过高水位，本次跳过；写出 {} 个", skipped, written);
        }
        return written;
    }

    /**
     * 序列化信封。放在实现内，调用方不需要知道线上格式。
     */
    private String serialize(WsPushMessage message) {
        return JSON.toJSONString(message);
    }

    /**
     * 租户内广播（服务端内部使用）
     *
     * @param tenantId 租户
     * @param message  推送信封
     */
    public void broadcastToTenant(Long tenantId, WsPushMessage message) {
        if (tenantId == null || message == null) {
            log.warn("[netty] 租户广播失败：参数为空tenantId={}", tenantId);
            return;
        }
        ChannelGroup tenantChannels = tenantChannelsGroups.get(tenantId);
        if (tenantChannels == null) {
            log.debug("[netty] 租户{}无在线通道，广播跳过", tenantId);
            return;
        }
        writeSkippingBackpressure(tenantChannels, serialize(message));
    }
}
