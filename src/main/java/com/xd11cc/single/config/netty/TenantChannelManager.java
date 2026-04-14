package com.xd11cc.single.config.netty;

import com.xd11cc.single.entity.dto.LoginUserDTO;
import com.xd11cc.single.utils.StringUtils;
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
public class TenantChannelManager {

    // 租户id -> 该租户所有通道（租户广播）
    private final ConcurrentHashMap<Long, ChannelGroup> tenantChannelsGroups = new ConcurrentHashMap<>();
    // 用户id -> 该用户所有通道（多端登陆，用户级推送）
    private final ConcurrentHashMap<Long, ChannelGroup> userChannelsGroups = new ConcurrentHashMap<>();
    // 全局所有通道（全局广播）
    private final ChannelGroup globalChannelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
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
        if (StringUtils.isNull(tenantId) || StringUtils.isNull(userId) || StringUtils.isNull(channel)) {
            log.warn(" [netty] 添加通道失败，参数为空：tenantId={}, userId={}, channel={}", tenantId, userId, channel);
            return false;
        }
        // 已存在不重复添加
        if (channelMetadataMap.containsKey(channel)) {
            log.debug(" [netty] 通道已存在无需添加，channel:{}", channel.id());
            return false;
        }
        try {
            // 添加到租户组
            tenantChannelsGroups.computeIfAbsent(tenantId, k ->
                    new DefaultChannelGroup(GlobalEventExecutor.INSTANCE)).add(channel);
            // 添加到用户组
            userChannelsGroups.computeIfAbsent(userId, k ->
                    new DefaultChannelGroup(GlobalEventExecutor.INSTANCE)).add(channel);
            // 添加到全局组
            globalChannelGroup.add(channel);
            // 记录元数据
            channelMetadataMap.put(channel, new ChannelExtMetadata(tenantId, userId));
            // 连接数 +1
            currentConnCount.incrementAndGet();
            log.info(" [netty] 通道添加成功，租户：{}，用户：{}，通道：{}，当前连接数：{}", tenantId, userId, channel.id(), currentConnCount.get());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 移除通道（连接断开/异常时调用）
     * @param channel
     */
    public void removeChannel(Channel channel) {
        if (StringUtils.isNull(channel)) return;

        ChannelExtMetadata metadata = channelMetadataMap.remove(channel);
        if (metadata == null) {
            log.debug(" [netty] 通道无元数据，无需清理：channel:{}", channel.id());
            return;
        }
        try {
            Long tenantId = metadata.getTenantId();
            // 清理租户通道组
            ChannelGroup tenantGroup = tenantChannelsGroups.get(tenantId);
            if (StringUtils.isNotNull(tenantGroup)) {
                tenantGroup.remove(channel);
                if (tenantGroup.isEmpty()) {
                    tenantChannelsGroups.remove(tenantId);
                    log.debug(" [netty] 租户{}通道组为空，已移除", tenantId);
                }
            }
            Long userId = metadata.getUserId();
            // 清理用户通道组
            ChannelGroup userGroup = userChannelsGroups.get(userId);
            if (StringUtils.isNotNull(userGroup)) {
                userGroup.remove(channel);
                if (userGroup.isEmpty()) {
                    userChannelsGroups.remove(userId);
                    log.debug(" [netty] 用户{}通道组为空，已移除", userId);
                }
            }

            // 清理全局通道组
            globalChannelGroup.remove(channel);
            // 连接数 -1
            currentConnCount.decrementAndGet();
            log.info(" [netty] 通道移除成功，租户：{}，用户：{}，通道：{}，当前连接数：{}", tenantId, userId, channel.id(), currentConnCount.get());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 精确到用户推送
     * @param userId
     * @param message
     */
    public void pushToUser(Long userId, String message){
        if (StringUtils.isNull(userId) || StringUtils.isNull(message)) {
            log.warn(" [netty] 消息推送失败：参数为空userId={}, message={}", userId, message);
            return;
        }
        ChannelGroup userChannels = userChannelsGroups.get(userId);
        if (StringUtils.isNull(userChannels)) {
            log.debug(" [netty] 用户{}无在线通道，推送失败", userId);
            return;
        }
        // 构建webSocket帧
        TextWebSocketFrame frame = new TextWebSocketFrame(message);
        // 异步推送+异常处理
        userChannels.writeAndFlush(frame).addListener(future -> {
            if (future.isSuccess()) {
                log.debug(" [netty] 推送用户{}成功，消息长度{}", userId, message.length());
            }else {
                log.error(" [netty] 推送用户{}失败", userId, future.cause());
            }
        });
    }

    /**
     * 租户内广播
     * @param tenantId
     * @param message
     */
    public void broadcastToTenant(Long tenantId, String message) {
        if (StringUtils.isNull(tenantId) || StringUtils.isNull(message)) {
            log.warn(" [netty] 租户广播失败：参数为空tenantId={}, message={}", tenantId, message);
            return;
        }
        ChannelGroup tenantChannels = tenantChannelsGroups.get(tenantId);
        if (StringUtils.isNull(tenantChannels)) {
            log.debug(" [netty] 租户{}无在线通道，广播失败", tenantId);
            return;
        }
        TextWebSocketFrame frame = new TextWebSocketFrame(message);
        tenantChannels.writeAndFlush(frame).addListener(future -> {
            if (future.isSuccess()) {
                log.debug(" [netty] 租户{}广播成功，消息长度{}，通道数{}", tenantId, message.length(), tenantChannels.size());
            }else {
                log.error(" [netty] 租户{}广播失败", tenantId, future.cause());
            }
        });
    }

    /**
     * 全局广播
     * @param message
     */
    public void broadcastToGlobal(String message) {
        if (StringUtils.isNull(message)) {
            log.warn(" [netty] 全局广播失败：消息为空");
            return;
        }
        if (globalChannelGroup.isEmpty()) {
            log.debug(" [netty] 全局无在线通道，广播失败");
            return;
        }
        TextWebSocketFrame frame = new TextWebSocketFrame(message);
        globalChannelGroup.writeAndFlush(frame).addListener(future -> {
            if (future.isSuccess()) {
                log.debug(" [netty] 全局广播成功，消息长度{}，通道数{}", message.length(), globalChannelGroup.size());
            }else {
                log.error(" [netty] 全局广播失败", future.cause());
            }
        });
    }

    /**
     * 获取通道元数据
     * @param channel
     * @return
     */
    public ChannelExtMetadata getChannelMetadata(Channel channel) {
        return channelMetadataMap.get(channel);
    }
}
