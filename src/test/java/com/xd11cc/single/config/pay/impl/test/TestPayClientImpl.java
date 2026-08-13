package com.xd11cc.single.config.pay.impl.test;

import com.xd11cc.single.config.annotation.PayClientCode;
import com.xd11cc.single.enums.PayChannelEnum;

/**
 * PayClientScannerRegistrar 单测用桩客户端
 * <p>
 * 使用真实的 {@link PayChannelEnum#WALLET} 枚举值，避免对 {@code final} 枚举做反射补丁。
 * </p>
 */
@PayClientCode(PayChannelEnum.WALLET)
public class TestPayClientImpl extends PayClientTestStub {

    public TestPayClientImpl(Long channelId, TestPayClientConfig config) {
        super(channelId, config);
    }
}
