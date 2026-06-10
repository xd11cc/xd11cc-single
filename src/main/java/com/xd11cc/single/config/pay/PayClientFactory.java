package com.xd11cc.single.config.pay;

import com.alipay.api.domain.PayConfig;

/**
 * @author xd11cc
 * @date 2026-06-08 09:24:23
 * @description 支付客户端的工厂
 */
public interface PayClientFactory {

    /**
     * 获得支付客户端
     * @param channelId 渠道编号
     * @return 支付客户端
     */
    PayClient getPayClient(Long channelId);

    /**
     * 创建支付客户端
     * @param channelId 渠道编号
     * @param channelCode 渠道编码
     * @param config 支付配置
     * @return 支付客户端
     * @param <Config>
     */
    <Config extends PayClientConfig> PayClient createOrUpdatePayClient(Long channelId, String channelCode,
                                                                 Config config);
}
