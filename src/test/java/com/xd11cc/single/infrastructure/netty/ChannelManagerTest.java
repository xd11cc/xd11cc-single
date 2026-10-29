package com.xd11cc.single.infrastructure.netty;

import com.xd11cc.single.entity.domain.SystemUserDO;
import com.xd11cc.single.entity.dto.LoginUserDTO;
import io.netty.channel.Channel;
import io.netty.channel.ChannelId;
import com.xd11cc.single.entity.dto.WsPushMessage;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.ChannelGroupFuture;
import io.netty.channel.group.ChannelMatcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;

class ChannelManagerTest {

    private final ChannelManager channelManager = new ChannelManager();

    @BeforeEach
    void setUp() throws Exception {
        replaceInternalStateWithEmpty();
    }

    // ==================== 辅助方法 ====================

    private Channel mockChannel(long id) {
        Channel channel = mock(Channel.class);
        io.netty.channel.ChannelId fixedId = io.netty.channel.DefaultChannelId.newInstance();
        org.mockito.BDDMockito.given(channel.id()).willReturn(fixedId);
        // 默认视为可写；积压用例里单独改为 false
        org.mockito.BDDMockito.given(channel.isWritable()).willReturn(true);
        // 写帧后生产代码会 addListener，必须返回非 null 的 future
        org.mockito.BDDMockito.given(channel.writeAndFlush(org.mockito.ArgumentMatchers.any()))
                .willReturn(mock(io.netty.channel.ChannelFuture.class));
        return channel;
    }

    private LoginUserDTO mockLoginUser(Long userId, Long tenantId) {
        LoginUserDTO dto = new LoginUserDTO();
        SystemUserDO userDO = new SystemUserDO();
        userDO.setId(userId);
        userDO.setTenantId(tenantId);
        dto.setSystemUserDO(userDO);
        dto.setUserId(userId);
        return dto;
    }

    @SuppressWarnings("unchecked")
    private void replaceInternalStateWithEmpty() throws Exception {
        Field tenantField = ChannelManager.class.getDeclaredField("tenantChannelsGroups");
        Field userField = ChannelManager.class.getDeclaredField("userChannelsGroups");
        Field countField = ChannelManager.class.getDeclaredField("currentConnCount");
        Field metadataField = ChannelManager.class.getDeclaredField("channelMetadataMap");
        tenantField.setAccessible(true);
        userField.setAccessible(true);
        countField.setAccessible(true);
        metadataField.setAccessible(true);
        tenantField.set(channelManager, new java.util.concurrent.ConcurrentHashMap<>());
        userField.set(channelManager, new java.util.concurrent.ConcurrentHashMap<>());
        countField.set(channelManager, new java.util.concurrent.atomic.AtomicInteger(0));
        metadataField.set(channelManager, new java.util.concurrent.ConcurrentHashMap<>());
    }

    // ==================== addChannel ====================

    @Test
    void addChannel_成功_返回true_并增加计数() throws Exception {
        Channel channel = mockChannel(1L);
        LoginUserDTO loginUser = mockLoginUser(1L, 10L);

        // 预建 ChannelGroup，避免 DefaultChannelGroup + mock Channel 的 compareTo 问题
        java.util.concurrent.ConcurrentHashMap<Long, ChannelGroup> tenantMap =
                (java.util.concurrent.ConcurrentHashMap<Long, ChannelGroup>) getField("tenantChannelsGroups");
        java.util.concurrent.ConcurrentHashMap<Long, ChannelGroup> userMap =
                (java.util.concurrent.ConcurrentHashMap<Long, ChannelGroup>) getField("userChannelsGroups");
        FakeChannelGroup tenantGroup = new FakeChannelGroup("tenant-10");
        FakeChannelGroup userGroup = new FakeChannelGroup("user-1");
        tenantMap.put(10L, tenantGroup);
        userMap.put(1L, userGroup);

        boolean result = channelManager.addChannel(channel, loginUser);

        assertThat(result).isTrue();
        assertThat(metadataMap()).hasSize(1);
        assertThat(userMap).containsKey(1L);
        assertThat(tenantGroup.size()).isEqualTo(1);
        assertThat(userGroup.size()).isEqualTo(1);
    }

    @Test
    void addChannel_重复添加_返回false() throws Exception {
        Channel channel = mockChannel(1L);
        LoginUserDTO loginUser = mockLoginUser(1L, 10L);

        java.util.concurrent.ConcurrentHashMap<Long, ChannelGroup> tenantMap =
                (java.util.concurrent.ConcurrentHashMap<Long, ChannelGroup>) getField("tenantChannelsGroups");
        java.util.concurrent.ConcurrentHashMap<Long, ChannelGroup> userMap =
                (java.util.concurrent.ConcurrentHashMap<Long, ChannelGroup>) getField("userChannelsGroups");
        FakeChannelGroup tenantGroup = new FakeChannelGroup("tenant-10");
        FakeChannelGroup userGroup = new FakeChannelGroup("user-1");
        tenantMap.put(10L, tenantGroup);
        userMap.put(1L, userGroup);

        boolean first = channelManager.addChannel(channel, loginUser);
        boolean second = channelManager.addChannel(channel, loginUser);

        assertThat(first).isTrue();
        assertThat(second).isFalse();
        assertThat(metadataMap()).hasSize(1);
    }

    @Test
    void addChannel_参数为空_返回false() {
        // channel 为 null
        assertThat(channelManager.addChannel(null, mockLoginUser(1L, 10L))).isFalse();
    }

    // ==================== removeChannel ====================

    @Test
    void removeChannel_移除通道_减少计数() throws Exception {
        Channel channel = mockChannel(1L);
        LoginUserDTO loginUser = mockLoginUser(1L, 10L);

        java.util.concurrent.ConcurrentHashMap<Long, ChannelGroup> tenantMap =
                (java.util.concurrent.ConcurrentHashMap<Long, ChannelGroup>) getField("tenantChannelsGroups");
        java.util.concurrent.ConcurrentHashMap<Long, ChannelGroup> userMap =
                (java.util.concurrent.ConcurrentHashMap<Long, ChannelGroup>) getField("userChannelsGroups");
        FakeChannelGroup tenantGroup = new FakeChannelGroup("tenant-10");
        FakeChannelGroup userGroup = new FakeChannelGroup("user-1");
        tenantMap.put(10L, tenantGroup);
        userMap.put(1L, userGroup);

        channelManager.addChannel(channel, loginUser);

        channelManager.removeChannel(channel);

        assertThat(metadataMap()).isEmpty();
        assertThat(userGroups()).doesNotContainKey(1L);
    }

    @Test
    void removeChannel_不存在的通道_不做处理() throws Exception {
        Channel channel = mockChannel(1L);
        // Should not throw
        channelManager.removeChannel(channel);
        assertThat(metadataMap()).isEmpty();
    }

    // ==================== 多租户隔离 ====================

    @Test
    void 多租户隔离_不同租户通道独立管理() throws Exception {
        Channel tenant1Channel = mockChannel(1L);
        Channel tenant2Channel = mockChannel(2L);
        LoginUserDTO user1 = mockLoginUser(1L, 10L);
        LoginUserDTO user2 = mockLoginUser(2L, 20L);

        java.util.concurrent.ConcurrentHashMap<Long, ChannelGroup> tenantMap =
                (java.util.concurrent.ConcurrentHashMap<Long, ChannelGroup>) getField("tenantChannelsGroups");
        java.util.concurrent.ConcurrentHashMap<Long, ChannelGroup> userMap =
                (java.util.concurrent.ConcurrentHashMap<Long, ChannelGroup>) getField("userChannelsGroups");
        FakeChannelGroup tenant1Group = new FakeChannelGroup("tenant-10");
        FakeChannelGroup tenant2Group = new FakeChannelGroup("tenant-20");
        FakeChannelGroup user1Group = new FakeChannelGroup("user-1");
        FakeChannelGroup user2Group = new FakeChannelGroup("user-2");
        tenantMap.put(10L, tenant1Group);
        tenantMap.put(20L, tenant2Group);
        userMap.put(1L, user1Group);
        userMap.put(2L, user2Group);

        channelManager.addChannel(tenant1Channel, user1);
        channelManager.addChannel(tenant2Channel, user2);

        assertThat(metadataMap()).hasSize(2);
        assertThat(userGroups().keySet()).containsExactlyInAnyOrder(1L, 2L);

        channelManager.removeChannel(tenant1Channel);
        assertThat(metadataMap()).hasSize(1);
        assertThat(userGroups()).doesNotContainKey(1L);
        assertThat(userGroups()).containsKey(2L);
    }

    // ==================== 多端登录 ====================

    @Test
    void 多端登录_同一用户多个通道_均计入在线() throws Exception {
        Channel channel1 = mockChannel(1L);
        Channel channel2 = mockChannel(2L);
        LoginUserDTO loginUser = mockLoginUser(1L, 10L);

        java.util.concurrent.ConcurrentHashMap<Long, ChannelGroup> tenantMap =
                (java.util.concurrent.ConcurrentHashMap<Long, ChannelGroup>) getField("tenantChannelsGroups");
        java.util.concurrent.ConcurrentHashMap<Long, ChannelGroup> userMap =
                (java.util.concurrent.ConcurrentHashMap<Long, ChannelGroup>) getField("userChannelsGroups");
        FakeChannelGroup tenantGroup = new FakeChannelGroup("tenant-10");
        FakeChannelGroup userGroup = new FakeChannelGroup("user-1");
        tenantMap.put(10L, tenantGroup);
        userMap.put(1L, userGroup);

        channelManager.addChannel(channel1, loginUser);
        channelManager.addChannel(channel2, loginUser);

        assertThat(metadataMap()).hasSize(2);
        assertThat(userGroups()).containsKey(1L);
    }

    @Test
    void 多端登录_移除一个通道_用户仍在线() throws Exception {
        Channel channel1 = mockChannel(1L);
        Channel channel2 = mockChannel(2L);
        LoginUserDTO loginUser = mockLoginUser(1L, 10L);

        java.util.concurrent.ConcurrentHashMap<Long, ChannelGroup> tenantMap =
                (java.util.concurrent.ConcurrentHashMap<Long, ChannelGroup>) getField("tenantChannelsGroups");
        java.util.concurrent.ConcurrentHashMap<Long, ChannelGroup> userMap =
                (java.util.concurrent.ConcurrentHashMap<Long, ChannelGroup>) getField("userChannelsGroups");
        FakeChannelGroup tenantGroup = new FakeChannelGroup("tenant-10");
        FakeChannelGroup userGroup = new FakeChannelGroup("user-1");
        tenantMap.put(10L, tenantGroup);
        userMap.put(1L, userGroup);

        channelManager.addChannel(channel1, loginUser);
        channelManager.addChannel(channel2, loginUser);

        channelManager.removeChannel(channel1);

        assertThat(metadataMap()).hasSize(1);
        assertThat(userGroups()).containsKey(1L);
    }

    // ==================== Reflection helper ====================

    // ==================== pushToUser（同租户不变式）====================

    @Test
    void pushToUser_同租户_推送成功并返回true() throws Exception {
        Channel channel = mockChannel(1L);
        ChannelGroup group = mockUserGroupWith(channel, 1L);
        putMetadata(channel, 10L, 1L);

        boolean result = channelManager.pushToUser(1L, 10L, WsPushMessage.of("BUSINESS", "hello"));

        assertThat(result).isTrue();
        // 现在是逐通道写，断言落在通道上
        then(channel).should().writeAndFlush(any(TextWebSocketFrame.class));
    }

    @Test
    void pushToUser_跨租户_拒绝且不写帧() throws Exception {
        Channel channel = mockChannel(1L);
        ChannelGroup group = mockUserGroupWith(channel, 1L);
        // 目标用户属于租户 20，发送方来自租户 10
        putMetadata(channel, 20L, 1L);

        boolean result = channelManager.pushToUser(1L, 10L, WsPushMessage.of("BUSINESS", "hello"));

        assertThat(result).isFalse();
        then(group).should(never()).writeAndFlush(any());
    }

    @Test
    void pushToUser_连接积压_跳过且返回false() throws Exception {
        Channel channel = mockChannel(1L);
        given(channel.isWritable()).willReturn(false);
        ChannelGroup group = mockUserGroupWith(channel, 1L);
        putMetadata(channel, 10L, 1L);

        boolean result = channelManager.pushToUser(1L, 10L, WsPushMessage.of("BUSINESS", "hello"));

        assertThat(result).isFalse();
        then(channel).should(never()).writeAndFlush(any());
    }

    @Test
    void pushToUser_部分连接积压_只写给可写的() throws Exception {
        Channel healthy = mockChannel(1L);
        Channel backedUp = mockChannel(2L);
        given(backedUp.isWritable()).willReturn(false);
        ChannelGroup group = mockUserGroupWith(java.util.Arrays.asList(healthy, backedUp), 1L);
        putMetadata(healthy, 10L, 1L);
        putMetadata(backedUp, 10L, 1L);

        boolean result = channelManager.pushToUser(1L, 10L, WsPushMessage.of("BUSINESS", "hello"));

        assertThat(result).isTrue();
        then(healthy).should().writeAndFlush(any(TextWebSocketFrame.class));
        then(backedUp).should(never()).writeAndFlush(any());
    }

    @Test
    void pushToUser_目标离线_返回false() {
        boolean result = channelManager.pushToUser(999L, 10L, WsPushMessage.of("BUSINESS", "hello"));

        assertThat(result).isFalse();
    }

    /**
     * 构造一个已注册到 userChannelsGroups 的 mock 通道组
     */
    @SuppressWarnings("unchecked")
    private ChannelGroup mockUserGroupWith(Channel channel, Long userId) throws Exception {
        return mockUserGroupWith(java.util.Collections.singletonList(channel), userId);
    }

    private ChannelGroup mockUserGroupWith(java.util.List<Channel> channels, Long userId) throws Exception {
        ChannelGroup group = mock(ChannelGroup.class);
        given(group.isEmpty()).willReturn(false);
        // 每次调用都返回新的迭代器：pushToUser 内部会遍历两次（取租户元数据 + 写帧）
        given(group.iterator()).willAnswer(inv -> channels.iterator());
        given(group.writeAndFlush(any())).willReturn(mock(ChannelGroupFuture.class));
        ((java.util.concurrent.ConcurrentHashMap<Long, ChannelGroup>) getField("userChannelsGroups"))
                .put(userId, group);
        return group;
    }

    @SuppressWarnings("unchecked")
    private void putMetadata(Channel channel, Long tenantId, Long userId) throws Exception {
        ((java.util.concurrent.ConcurrentHashMap<Channel, ChannelManager.ChannelExtMetadata>) getField("channelMetadataMap"))
                .put(channel, new ChannelManager.ChannelExtMetadata(tenantId, userId));
    }

    @SuppressWarnings("unchecked")
    private java.util.Map<Channel, ChannelManager.ChannelExtMetadata> metadataMap() throws Exception {
        return (java.util.Map<Channel, ChannelManager.ChannelExtMetadata>) getField("channelMetadataMap");
    }

    @SuppressWarnings("unchecked")
    private java.util.Map<Long, ChannelGroup> userGroups() throws Exception {
        return (java.util.Map<Long, ChannelGroup>) getField("userChannelsGroups");
    }

    private Object getField(String fieldName) throws Exception {
        Field field = ChannelManager.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(channelManager);
    }

    // ==================== Fake ChannelGroup ====================

    /**
     * Netty 4.1 的 ChannelGroup 接口定义：
     * extends Set&lt;Channel&gt;, Comparable&lt;ChannelGroup&gt;
     * 核心方法：name / find / write / flush / writeAndFlush / close / disconnect / deregister / newCloseFuture
     */
    private static class FakeChannelGroup implements ChannelGroup {

        private final String name;
        private final List<Channel> channels = new ArrayList<>();

        FakeChannelGroup(String name) {
            this.name = name;
        }

        // ==================== Set<Channel> ====================

        @Override
        public int size() { return channels.size(); }

        @Override
        public boolean isEmpty() { return channels.isEmpty(); }

        @Override
        public boolean add(Channel channel) { return channels.add(channel); }

        @Override
        public boolean remove(Object obj) { return channels.remove(obj); }

        @Override
        public boolean contains(Object obj) { return channels.contains(obj); }

        @Override
        public java.util.Iterator<Channel> iterator() { return channels.iterator(); }

        @Override
        public Object[] toArray() { return channels.toArray(); }

        @Override
        public <T> T[] toArray(T[] a) { return channels.toArray(a); }

        @Override
        public boolean containsAll(java.util.Collection<?> c) { return channels.containsAll(c); }

        @Override
        public boolean addAll(java.util.Collection<? extends Channel> c) { return channels.addAll(c); }

        @Override
        public boolean removeAll(java.util.Collection<?> c) { return channels.removeAll(c); }

        @Override
        public boolean retainAll(java.util.Collection<?> c) { return channels.retainAll(c); }

        @Override
        public void clear() { channels.clear(); }

        // ==================== Comparable<ChannelGroup> ====================

        @Override
        public int compareTo(ChannelGroup o) { return 0; }

        // ==================== ChannelGroup ====================

        @Override
        public String name() { return name; }

        @Override
        public Channel find(ChannelId id) { return null; }

        @Override
        public ChannelGroupFuture write(Object msg) { return null; }

        @Override
        public ChannelGroupFuture write(Object msg, ChannelMatcher matcher) { return null; }

        @Override
        public ChannelGroupFuture write(Object msg, ChannelMatcher matcher, boolean promise) { return null; }

        @Override
        public ChannelGroup flush() { return this; }

        @Override
        public ChannelGroup flush(ChannelMatcher matcher) { return this; }

        @Override
        public ChannelGroupFuture writeAndFlush(Object msg) { return null; }

        @Override
        public ChannelGroupFuture flushAndWrite(Object msg) { return null; }

        @Override
        public ChannelGroupFuture writeAndFlush(Object msg, ChannelMatcher matcher) { return null; }

        @Override
        public ChannelGroupFuture writeAndFlush(Object msg, ChannelMatcher matcher, boolean promise) { return null; }

        @Override
        public ChannelGroupFuture flushAndWrite(Object msg, ChannelMatcher matcher) { return null; }

        @Override
        public ChannelGroupFuture disconnect() { return null; }

        @Override
        public ChannelGroupFuture disconnect(ChannelMatcher matcher) { return null; }

        @Override
        public ChannelGroupFuture close() { return null; }

        @Override
        public ChannelGroupFuture close(ChannelMatcher matcher) { return null; }

        @Override
        public ChannelGroupFuture deregister() { return null; }

        @Override
        public ChannelGroupFuture deregister(ChannelMatcher matcher) { return null; }

        @Override
        public ChannelGroupFuture newCloseFuture() { return null; }

        @Override
        public ChannelGroupFuture newCloseFuture(ChannelMatcher matcher) { return null; }
    }
}
