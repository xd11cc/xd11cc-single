package com.xd11cc.single.config.pay.impl.wechat;

import com.github.binarywang.wxpay.bean.order.WxPayMwebOrderResult;
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
 * @date 2026-06-09 10:30:29
 * @description 微信支付（H5 网页）的 PayClient 实现类
 * 文档：<a href="https://pay.weixin.qq.com/wiki/doc/apiv3/apis/chapter3_3_1.shtml">H5下单API</>
 */
@PayClientCode(PayChannelEnum.WX_WAP)
public class WxWapPayClient extends AbstractWxPayClient {

    public WxWapPayClient(Long channelId, WxPayClientConfig config) {
        super(channelId, PayChannelEnum.WX_WAP.getCode(), config);
    }

    @Override
    protected void doInit() {
        super.doInit(WxPayConstants.TradeType.MWEB);
    }

    @Override
    protected PayOrderRespDTO doUnifiedOrder2(PayOrderUnifiedReqDTO reqDTO) throws Exception {
        // 1. 构建 WxPayUnifiedOrderRequest 对象
        WxPayUnifiedOrderRequest request = buildUnifiedOrderRequestV2(reqDTO);

        // 2. 执行请求
        WxPayMwebOrderResult response = client.createOrder(request);

        // 3. 构建结果
        return PayOrderRespDTO.waitingOf(PayOrderDisplayModeEnum.URL.getMode(), response.getMwebUrl(),
                reqDTO.getOutTradeNo(), response);
    }

    @Override
    protected PayOrderRespDTO doUnifiedOrder3(PayOrderUnifiedReqDTO reqDTO) throws WxPayException {
        // 1. 构建 WxPayUnifiedOrderV3Request 对象
        WxPayUnifiedOrderV3Request request = buildUnifiedOrderRequestV3(reqDTO);

        // 2. 执行请求
        String response = client.createOrderV3(TradeTypeEnum.H5, request);

        // 3. 构建结果
        return PayOrderRespDTO.waitingOf(PayOrderDisplayModeEnum.URL.getMode(), response,
                reqDTO.getOutTradeNo(), response);
    }
}
