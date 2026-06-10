package com.xd11cc.single.config.pay.impl.wechat;

import com.github.binarywang.wxpay.bean.order.WxPayNativeOrderResult;
import com.github.binarywang.wxpay.bean.request.WxPayUnifiedOrderRequest;
import com.github.binarywang.wxpay.bean.request.WxPayUnifiedOrderV3Request;
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
 * @date 2026-06-09 10:23:59
 * @description 微信支付【Native 二维码】的 PayClient 实现类
 * 文档：<a href="https://pay.weixin.qq.com/wiki/doc/apiv3/apis/chapter3_4_1.shtml">Native 下单</a>
 */
@PayClientCode(PayChannelEnum.WX_NATIVE)
public class WxNativePayClient extends AbstractWxPayClient{

    public WxNativePayClient(Long channelId, WxPayClientConfig config) {
        super(channelId, PayChannelEnum.WX_NATIVE.getCode(), config);
    }

    @Override
    protected void doInit() {
        super.doInit(WxPayConstants.TradeType.NATIVE);
    }

    @Override
    protected PayOrderRespDTO doUnifiedOrder2(PayOrderUnifiedReqDTO reqDTO) throws Exception {
        // 1. 构建 WxPayUnifiedOrderRequest 对象
        WxPayUnifiedOrderRequest request = buildUnifiedOrderRequestV2(reqDTO);

        // 2. 执行请求
        WxPayNativeOrderResult response = client.createOrder(request);

        // 3. 构建结果
        return PayOrderRespDTO.waitingOf(PayOrderDisplayModeEnum.QR_CODE.getMode(), response.getCodeUrl(),
                reqDTO.getOutTradeNo(), response);
    }

    @Override
    protected PayOrderRespDTO doUnifiedOrder3(PayOrderUnifiedReqDTO reqDTO) throws WxPayException {
        // 1. 构建 WxPayUnifiedOrderV3Request 对象
        WxPayUnifiedOrderV3Request request = buildUnifiedOrderRequestV3(reqDTO);

        // 2. 执行请求
        String response = client.createOrderV3(TradeTypeEnum.NATIVE, request);

        // 3. 构建结果
        return PayOrderRespDTO.waitingOf(PayOrderDisplayModeEnum.QR_CODE.getMode(), response,
                reqDTO.getOutTradeNo(), response);
    }
}
