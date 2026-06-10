package com.xd11cc.single.config.pay.impl.wechat;

import com.alibaba.fastjson2.JSON;
import com.github.binarywang.wxpay.bean.order.WxPayAppOrderResult;
import com.github.binarywang.wxpay.bean.request.WxPayUnifiedOrderRequest;
import com.github.binarywang.wxpay.bean.request.WxPayUnifiedOrderV3Request;
import com.github.binarywang.wxpay.bean.result.WxPayUnifiedOrderV3Result;
import com.github.binarywang.wxpay.bean.result.enums.TradeTypeEnum;
import com.github.binarywang.wxpay.constant.WxPayConstants;
import com.github.binarywang.wxpay.exception.WxPayException;
import com.xd11cc.single.config.annotation.PayClientCode;
import com.xd11cc.single.entity.dto.PayOrderRespDTO;
import com.xd11cc.single.entity.dto.PayOrderUnifiedReqDTO;
import com.xd11cc.single.enums.PayChannelEnum;
import com.xd11cc.single.enums.PayOrderDisplayModeEnum;

/**
 * @author xd11cc
 * @date 2026-06-09 09:48:20
 * @description 微信支付【App 支付】的 PayClient 实现类
 * 文档：<a href="https://pay.weixin.qq.com/wiki/doc/apiv3/open/pay/chapter2_5_3.shtml">App 支付</a>
 */
@PayClientCode(PayChannelEnum.WX_APP)
public class WxAppPayClient extends AbstractWxPayClient{

    public WxAppPayClient(Long channelId, WxPayClientConfig config) {
        super(channelId, PayChannelEnum.WX_APP.getCode(), config);
    }

    @Override
    protected void doInit() {
        super.doInit(WxPayConstants.TradeType.APP);
    }

    @Override
    protected PayOrderRespDTO doUnifiedOrder2(PayOrderUnifiedReqDTO reqDTO) throws WxPayException {
        // 1. 构建 WxPayUnifiedOrderRequest 对象
        WxPayUnifiedOrderRequest request = buildUnifiedOrderRequestV2(reqDTO);

        // 2. 执行请求
        WxPayAppOrderResult response = client.createOrder(request);

        // 3. 构建结果
        return PayOrderRespDTO.waitingOf(PayOrderDisplayModeEnum.APP.getMode(), JSON.toJSONString(response),
                reqDTO.getOutTradeNo(), response);
    }

    @Override
    protected PayOrderRespDTO doUnifiedOrder3(PayOrderUnifiedReqDTO reqDTO) throws WxPayException {
        // 1. 构建 WxPayUnifiedOrderV3Request 对象
        WxPayUnifiedOrderV3Request request = buildUnifiedOrderRequestV3(reqDTO);

        // 2. 执行请求
        WxPayUnifiedOrderV3Result.AppResult response = client.createOrderV3(TradeTypeEnum.APP, request);

        // 3. 构建结果
        return PayOrderRespDTO.waitingOf(PayOrderDisplayModeEnum.APP.getMode(), JSON.toJSONString(response),
                reqDTO.getOutTradeNo(), response);
    }
}
