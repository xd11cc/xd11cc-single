package com.xd11cc.single.config.pay.impl;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ReflectUtil;
import com.xd11cc.single.config.pay.PayClient;
import com.xd11cc.single.config.pay.PayClientConfig;
import com.xd11cc.single.config.pay.PayClientFactory;
import com.xd11cc.single.enums.PayChannelEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author xd11cc
 * @date 2026-06-08 09:23:36
 * @description 支付客户端的实现类
 */
@Slf4j
@Component
public class PayClientFactoryImpl implements PayClientFactory {

    /**
     * 支付客户端map
     *
     * key：渠道编号
     */
    private final ConcurrentHashMap<Long, AbstractPayClient<?>> clients = new ConcurrentHashMap<>();

    /**
     * 支付客户端 Class Map
     */
    private static final ConcurrentHashMap<PayChannelEnum, Class<? extends PayClient<?>>> clientClass = new ConcurrentHashMap<>();

    /**
     * 供 PayClientScannerRegistrar 调用的注册入口
     * @param payChannelEnum
     * @param clazz
     */
    public static void registerClientClass(PayChannelEnum payChannelEnum, Class<?> clazz) {
        @SuppressWarnings("unchecked")
        Class<? extends PayClient<?>> payClientClass = (Class<? extends PayClient<?>>) clazz;
        clientClass.put(payChannelEnum, payClientClass);
    }

    //    public PayClientFactoryImpl() {
//        // 微信支付客户端
//        clientClass.put(WX_PUB, WxPubPayClient.class);
//        clientClass.put(WX_LITE, WxLitePayClient.class);
//        clientClass.put(WX_APP, WxAppPayClient.class);
//        clientClass.put(WX_BAR, WxBarPayClient.class);
//        clientClass.put(WX_NATIVE, WxNativePayClient.class);
//        clientClass.put(WX_WAP, WxWapPayClient.class);
//        // 支付包支付客户端
//        clientClass.put(ALIPAY_WAP, AlipayWapPayClient.class);
//        clientClass.put(ALIPAY_QR, AlipayPayClient.class);
//        clientClass.put(ALIPAY_APP, AlipayAppPayClient.class);
//        clientClass.put(ALIPAY_PC, AlipayPcPayClient.class);
//        clientClass.put(ALIPAY_BAR, AlipayBarPayClient.class);
//         钱包支付客户端 todo
//        clientClass.put(WALLET, WalletPayClient.class);
//    }

    @Override
    public PayClient getPayClient(Long channelId) {
        AbstractPayClient<?> client = clients.get(channelId);
        if (client == null) {
            log.error("[getPayClient][渠道编号({}) 找不到客户端号]", channelId);
        }
        return client;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <Config extends PayClientConfig> PayClient createOrUpdatePayClient(Long channelId, String channelCode,
                                                                              Config config) {
        AbstractPayClient<Config> client = (AbstractPayClient<Config>) clients.get(channelId);
        if (client == null) {
            client = (AbstractPayClient<Config>) clients.computeIfAbsent(channelId, id ->{
                AbstractPayClient<Config> payClient = this.createPayClient(channelId, channelCode, config);
                payClient.init();
                return payClient;
            });
        }else {
            client.refresh(config);
        }
        return client;
    }

    @SuppressWarnings("unchecked")
    private <Config extends PayClientConfig> AbstractPayClient<Config> createPayClient(Long channelId, String channelCode,
                                                                                       Config config) {
        PayChannelEnum channelEnum = PayChannelEnum.getByCode(channelCode);
        Assert.notNull(channelEnum, String.format("支付渠道%s为空", channelCode));
        Class<?> payClientClass = clientClass.get(channelEnum);
        Assert.notNull(payClientClass, String.format("支付渠道%s Class 为空", channelCode));
        return (AbstractPayClient<Config>) ReflectUtil.newInstance(payClientClass, channelId, config);
    }
}
