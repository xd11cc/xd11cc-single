package com.xd11cc.single.config.pay.impl.wechat;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.github.binarywang.wxpay.bean.order.WxPayMpOrderResult;
import com.github.binarywang.wxpay.bean.request.WxPayUnifiedOrderRequest;
import com.github.binarywang.wxpay.bean.request.WxPayUnifiedOrderV3Request;
import com.github.binarywang.wxpay.bean.result.WxPayUnifiedOrderV3Result;
import com.github.binarywang.wxpay.bean.result.enums.TradeTypeEnum;
import com.github.binarywang.wxpay.constant.WxPayConstants;
import com.github.binarywang.wxpay.exception.WxPayException;
import com.xd11cc.single.config.annotation.PayClientCode;
import com.xd11cc.single.config.exception.ServiceException;
import com.xd11cc.single.entity.dto.PayOrderRespDTO;
import com.xd11cc.single.entity.dto.PayOrderUnifiedReqDTO;
import com.xd11cc.single.enums.PayChannelEnum;
import com.xd11cc.single.enums.PayOrderDisplayModeEnum;
import com.xd11cc.single.enums.SystemErrorEnum;

/**
 * @author xd11cc
 * @date 2026-06-09 08:50:49
 * @description 微信支付（公众号）的 PayClient 实现类
 * 文档：<a href="https://pay.weixin.qq.com/wiki/doc/apiv3/apis/chapter3_1_1.shtml">JSAPI 下单</>
 */
@PayClientCode(PayChannelEnum.WX_PUB)
public class WxPubPayClient extends AbstractWxPayClient {

    @SuppressWarnings("unused") // 反射会调用到，所以不能删除
    public WxPubPayClient(Long channelId, WxPayClientConfig config) {
        super(channelId, PayChannelEnum.WX_PUB.getCode(), config);
    }

    public WxPubPayClient(Long channelId, String channelCode, WxPayClientConfig config) {
        super(channelId, channelCode, config);
    }

    @Override
    protected void doInit() {
        super.doInit(WxPayConstants.TradeType.JSAPI);
    }

    @Override
    protected PayOrderRespDTO doUnifiedOrder2(PayOrderUnifiedReqDTO reqDTO) throws WxPayException {
        // 1. 构建 WxPayUnifiedOrderRequest 对象
        WxPayUnifiedOrderRequest request = buildUnifiedOrderRequestV2(reqDTO)
                .setOpenid(getOpenid(reqDTO));

        // 2. 执行请求
        WxPayMpOrderResult response = client.createOrder(request);

        // 3. 构建结果
        return PayOrderRespDTO.waitingOf(PayOrderDisplayModeEnum.APP.getMode(), JSON.toJSONString(response),
                reqDTO.getOutTradeNo(), response);
    }

    @Override
    protected PayOrderRespDTO doUnifiedOrder3(PayOrderUnifiedReqDTO reqDTO) throws WxPayException {
        // 1. 构建 WxPayUnifiedOrderV3Request 对象
        WxPayUnifiedOrderV3Request request = buildUnifiedOrderRequestV3(reqDTO)
                .setPayer(new WxPayUnifiedOrderV3Request.Payer().setOpenid(getOpenid(reqDTO)));

        // 2. 执行请求
        WxPayUnifiedOrderV3Result.JsapiResult response = client.createOrderV3(TradeTypeEnum.JSAPI, request);

        // 3. 构建结果
        return PayOrderRespDTO.waitingOf(PayOrderDisplayModeEnum.APP.getMode(), JSON.toJSONString(response),
                reqDTO.getOutTradeNo(), response);
    }

    // =========== 各种工具方法 ==============

    private String getOpenid(PayOrderUnifiedReqDTO reqDTO) {
        String openid = MapUtil.getStr(reqDTO.getChannelExtras(), "openid");
        if (StrUtil.isEmpty(openid)) {
            throw new ServiceException(SystemErrorEnum.BAD_REQUEST1, new Object[]{"支付请求的 openid 不能为空！"});
        }
        return openid;
    }
}
