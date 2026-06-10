package com.xd11cc.single.config.pay.impl.wechat;

import com.xd11cc.single.config.annotation.PayClientCode;
import com.xd11cc.single.enums.PayChannelEnum;

/**
 * @author xd11cc
 * @date 2026-06-09 09:42:21
 * @description 微信支付【小程序】的 PayClient 实现类
 * 由于公众号和小程序的微信支付逻辑一致，所以直接进行继承
 * 文档：<a href="https://pay.weixin.qq.com/wiki/doc/apiv3/apis/chapter3_5_1.shtml">JSAPI 下单</>
 */
@PayClientCode(PayChannelEnum.WX_LITE)
public class WxLitePayClient extends WxPubPayClient {

    public WxLitePayClient(Long channelId, WxPayClientConfig config) {
        super(channelId, PayChannelEnum.WX_LITE.getCode(), config);
    }
}
