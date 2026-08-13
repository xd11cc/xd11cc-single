package com.xd11cc.single.config.netty;

import com.xd11cc.single.entity.domain.SystemUserDO;
import com.xd11cc.single.entity.dto.LoginUserDTO;
import io.netty.channel.Channel;
import io.netty.channel.ChannelId;
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
        Field globalField = ChannelManager.class.getDeclaredField("globalChannelGroup");
        Field countField = ChannelManager.class.getDeclaredField("currentConnCount");
        Field metadataField = ChannelManager.class.getDeclaredField("channelMetadataMap");
        tenantField.setAccessible(true);
        userField.setAccessible(true);
        globalField.setAccessible(true);
        countField.setAccessible(true);
        metadataField.setAccessible(true);
        tenantField.set(channelManager, new java.util.concurrent.ConcurrentHashMap<>());
        userField.set(channelManager, new java.util.concurrent.ConcurrentHashMap<>());
        // 使用 FakeChannelGroup 替换 globalChannelGroup，避免 DefaultChannelGroup + mock Channel 的 compareTo 问题
        globalField.set(channelManager, new FakeChannelGroup("global"));
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
        assertThat(channelManager.getOnlineCount()).isEqualTo(1);
        assertThat(channelManager.isUserOnline(1L)).isTrue();
        assertThat(channelManager.getOnlineUserIds()).containsExactly(1L);
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
        assertThat(channelManager.getOnlineCount()).isEqualTo(1);
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

        assertThat(channelManager.getOnlineCount()).isEqualTo(0);
        assertThat(channelManager.isUserOnline(1L)).isFalse();
    }

    @Test
    void removeChannel_不存在的通道_不做处理() {
        Channel channel = mockChannel(1L);
        // Should not throw
        channelManager.removeChannel(channel);
        assertThat(channelManager.getOnlineCount()).isEqualTo(0);
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

        assertThat(channelManager.getOnlineCount()).isEqualTo(2);
        assertThat(channelManager.getOnlineUserIds()).containsExactlyInAnyOrder(1L, 2L);

        channelManager.removeChannel(tenant1Channel);
        assertThat(channelManager.getOnlineCount()).isEqualTo(1);
        assertThat(channelManager.isUserOnline(1L)).isFalse();
        assertThat(channelManager.isUserOnline(2L)).isTrue();
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

        assertThat(channelManager.getOnlineCount()).isEqualTo(2);
        assertThat(channelManager.isUserOnline(1L)).isTrue();
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

        assertThat(channelManager.getOnlineCount()).isEqualTo(1);
        assertThat(channelManager.isUserOnline(1L)).isTrue();
    }

    // ==================== getChannelMetadata ====================

    @Test
    void getChannelMetadata_存在_返回元数据() throws Exception {
        Channel channel = mockChannel(1L);
        LoginUserDTO loginUser = mockLoginUser(1L, 10L);

        java.util.concurrent.ConcurrentHashMap<Long, ChannelGroup> tenantMap =
                (java.util.concurrent.ConcurrentHashMap<Long, ChannelGroup>) getField("tenantChannelsGroups");
        java.util.concurrent.ConcurrentHashMap<Long, ChannelGroup> userMap =
                (java.util.concurrent.ConcurrentHashMap<Long, ChannelGroup>) getField("userChannelsGroups");
        tenantMap.put(10L, new FakeChannelGroup("tenant-10"));
        userMap.put(1L, new FakeChannelGroup("user-1"));

        channelManager.addChannel(channel, loginUser);

        ChannelManager.ChannelExtMetadata metadata = channelManager.getChannelMetadata(channel);

        assertThat(metadata).isNotNull();
        assertThat(metadata.getTenantId()).isEqualTo(10L);
        assertThat(metadata.getUserId()).isEqualTo(1L);
    }

    @Test
    void getChannelMetadata_不存在_返回null() {
        Channel channel = mockChannel(1L);
        assertThat(channelManager.getChannelMetadata(channel)).isNull();
    }

    // ==================== Reflection helper ====================

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
