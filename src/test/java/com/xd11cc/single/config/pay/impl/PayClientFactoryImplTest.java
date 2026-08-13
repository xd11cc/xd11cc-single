package com.xd11cc.single.config.pay.impl;

import com.xd11cc.single.config.pay.PayClient;
import com.xd11cc.single.config.pay.impl.test.PayClientNoMatchingConstructorStub;
import com.xd11cc.single.config.pay.impl.test.PayClientTestStub;
import com.xd11cc.single.config.pay.impl.test.TestPayClientConfig;
import com.xd11cc.single.enums.PayChannelEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * PayClientFactoryImpl 单测
 * <p>
 * 测试 seam：{@link PayClientFactoryImpl#registerClientClass}（静态），
 * {@link PayClientFactoryImpl#getPayClient}，
 * {@link PayClientFactoryImpl#createOrUpdatePayClient}（公开）。
 * {@code createPayClient} 是 private 且依赖 Hutool 反射，通过反射直接验证。
 * </p>
 * <p>
 * 使用真实的 {@link PayChannelEnum#WALLET} 枚举值 + 测试桩子类化，
 * 避免对 {@code final} 枚举做反射补丁。
 * </p>
 */
@ExtendWith(MockitoExtension.class)
class PayClientFactoryImplTest {

    private PayClientFactoryImpl factory;

    @Mock
    private com.xd11cc.single.config.pay.PayClientConfig config;

    private TestPayClientConfig testConfig;

    // ==================== 生命周期 ====================

    @BeforeEach
    void setUp() throws Exception {
        resetStaticClientClassMap();
        factory = new PayClientFactoryImpl();
        testConfig = new TestPayClientConfig();
    }

    // ==================== getPayClient 空状态 ====================

    @Test
    void getPayClient_未创建_返回null() {
        PayClient client = factory.getPayClient(999L);
        assertThat(client).isNull();
    }

    // ==================== registerClientClass + createOrUpdatePayClient - 新建 ====================

    @Test
    void createOrUpdatePayClient_新建_返回实例并缓存() throws Exception {
        PayClientFactoryImpl.registerClientClass(PayChannelEnum.WALLET, PayClientTestStub.class);

        PayClient client = factory.createOrUpdatePayClient(1L, PayChannelEnum.WALLET.getCode(), testConfig);

        assertThat(client).isInstanceOf(PayClientTestStub.class);
        assertThat(factory.getPayClient(1L)).isSameAs(client);
    }

    // ==================== createOrUpdatePayClient - 相同 channel 不重复创建 ====================

    @Test
    void createOrUpdatePayClient_相同channel_仅创建一次() throws Exception {
        PayClientFactoryImpl.registerClientClass(PayChannelEnum.WALLET, PayClientTestStub.class);

        PayClient first = factory.createOrUpdatePayClient(1L, PayChannelEnum.WALLET.getCode(), testConfig);
        PayClient second = factory.createOrUpdatePayClient(1L, PayChannelEnum.WALLET.getCode(), testConfig);
        PayClient third = factory.createOrUpdatePayClient(1L, PayChannelEnum.WALLET.getCode(), testConfig);

        assertThat(second).isSameAs(first);
        assertThat(third).isSameAs(first);
    }

    // ==================== createOrUpdatePayClient - 不同 channel 各自独立 ====================

    @Test
    void createOrUpdatePayClient_不同channel_各自独立() throws Exception {
        PayClientFactoryImpl.registerClientClass(PayChannelEnum.WALLET, PayClientTestStub.class);

        PayClient client1 = factory.createOrUpdatePayClient(1L, PayChannelEnum.WALLET.getCode(), testConfig);
        PayClient client2 = factory.createOrUpdatePayClient(2L, PayChannelEnum.WALLET.getCode(), testConfig);

        assertThat(client1).isInstanceOf(PayClientTestStub.class);
        assertThat(client2).isInstanceOf(PayClientTestStub.class);
        assertThat(client1).isNotSameAs(client2);
        assertThat(factory.getPayClient(1L)).isSameAs(client1);
        assertThat(factory.getPayClient(2L)).isSameAs(client2);
    }

    // ==================== createOrUpdatePayClient - 错误渠道编码 ====================

    @Test
    void createOrUpdatePayClient_未知渠道编码_抛IllegalArgumentException() {
        assertThatThrownBy(() -> factory.createOrUpdatePayClient(1L, "unknown_channel", testConfig))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // ==================== createOrUpdatePayClient - 渠道枚举存在但 class 未注册 ====================

    @Test
    void createOrUpdatePayClient_渠道枚举存在但class未注册_抛IllegalArgumentException() {
        // WALLET 枚举值存在，但 @BeforeEach 已清空 clientClass，模拟"映射缺失"
        assertThatThrownBy(() -> factory.createOrUpdatePayClient(1L, PayChannelEnum.WALLET.getCode(), testConfig))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // ==================== createOrUpdatePayClient - 构造函数不匹配（P0 构造失败路径） ====================

    @Test
    void createOrUpdatePayClient_构造函数不匹配_向上抛出且不污染clientsMap() {
        PayClientFactoryImpl.registerClientClass(PayChannelEnum.WALLET, PayClientNoMatchingConstructorStub.class);

        // 注册的类只有无参构造，ReflectUtil.newInstance(channelId, config) 无法匹配
        assertThatThrownBy(() -> factory.createOrUpdatePayClient(1L, PayChannelEnum.WALLET.getCode(), testConfig))
                .isInstanceOf(Exception.class); // Hutool 抛 NoSuchMethodException 或 IllegalArgumentException，不吞

        // 验证 clients map 未被半成品实例污染
        assertThat(factory.getPayClient(1L)).isNull();
    }

    // ==================== createPayClient (private) - 反射直接验证 ====================

    @Test
    void createPayClient_反射访问_返回正确类型() throws Exception {
        PayClientFactoryImpl.registerClientClass(PayChannelEnum.WALLET, PayClientTestStub.class);

        java.lang.reflect.Method createMethod = PayClientFactoryImpl.class.getDeclaredMethod(
                "createPayClient", Long.class, String.class, com.xd11cc.single.config.pay.PayClientConfig.class);
        createMethod.setAccessible(true);
        @SuppressWarnings("unchecked")
        com.xd11cc.single.config.pay.impl.AbstractPayClient<com.xd11cc.single.config.pay.PayClientConfig> client =
                (com.xd11cc.single.config.pay.impl.AbstractPayClient<com.xd11cc.single.config.pay.PayClientConfig>) createMethod.invoke(
                        factory, 10L, PayChannelEnum.WALLET.getCode(), testConfig);

        assertThat(client).isInstanceOf(PayClientTestStub.class);
        assertThat(client).isInstanceOf(PayClient.class);
    }

    // ==================== 辅助方法 ====================

    /**
     * 通过反射重置 PayClientFactoryImpl 的静态 {@code clientClass} map，
     * 避免用例间状态污染。
     */
    private static void resetStaticClientClassMap() throws Exception {
        Field field = PayClientFactoryImpl.class.getDeclaredField("clientClass");
        field.setAccessible(true);
        @SuppressWarnings("unchecked")
        ConcurrentHashMap<PayChannelEnum, Class<? extends PayClient<?>>> map =
                (ConcurrentHashMap<PayChannelEnum, Class<? extends PayClient<?>>>) field.get(null);
        map.clear();
    }
}
