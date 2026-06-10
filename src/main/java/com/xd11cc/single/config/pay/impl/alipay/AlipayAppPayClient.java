package com.xd11cc.single.config.pay.impl.alipay;

import com.alipay.api.domain.AlipayTradeAppPayModel;
import com.alipay.api.request.AlipayTradeAppPayRequest;
import com.alipay.api.response.AlipayTradeAppPayResponse;
import com.xd11cc.single.config.annotation.PayClientCode;
import com.xd11cc.single.entity.dto.PayOrderRespDTO;
import com.xd11cc.single.entity.dto.PayOrderUnifiedReqDTO;
import com.xd11cc.single.enums.PayChannelEnum;
import com.xd11cc.single.enums.PayOrderDisplayModeEnum;

/**
 * @author xd11cc
 * @date 2026-06-08 14:37:11
 * @description 支付宝【App 支付】的 PayClient 实现类
 * 文档：<a href="https://opendocs.alipay.com/open/02e7gq">App 支付</a>
 */
@PayClientCode(PayChannelEnum.ALIPAY_APP)
public class AlipayAppPayClient extends AbstractAlipayPayClient {

    public AlipayAppPayClient(Long channelId, AlipayPayClientConfig config) {
        super(channelId, PayChannelEnum.ALIPAY_APP.getCode(), config);
    }

    @Override
    protected PayOrderRespDTO doUnifiedOrder(PayOrderUnifiedReqDTO reqDTO) throws Throwable {
        // 1.1 构建 AlipayTradeAppPayModel 请求
        AlipayTradeAppPayModel model = new AlipayTradeAppPayModel();
        // 1️⃣通用的参数
        model.setOutTradeNo(reqDTO.getOutTradeNo());
        model.setSubject(reqDTO.getSubject());
        model.setBody(reqDTO.getBody());
        model.setTotalAmount(formatAmount(reqDTO.getPrice()));
        model.setTimeExpire(formatTime(reqDTO.getExpireTime()));
        model.setProductCode("QUICK_MSECURITY_PAY"); // 产品销售码：无线快捷支付产品
        // 2️⃣个性化的参数【无】
        // 3️⃣支付宝扫码支付只有一种展示
        String displayMode = PayOrderDisplayModeEnum.APP.getMode();

        // 1.2 构建 AlipayTradeAppPayRequest
        AlipayTradeAppPayRequest request = new AlipayTradeAppPayRequest();
        request.setBizModel(model);
        request.setNotifyUrl(reqDTO.getNotifyUrl());
        request.setReturnUrl(reqDTO.getReturnUrl());

        // 2.1 执行请求
        AlipayTradeAppPayResponse response = client.sdkExecute(request);
        // 2.2 处理结果
        if (!response.isSuccess()) {
            return buildClosedPayOrderRespDTO(reqDTO, response);
        }
        return PayOrderRespDTO.waitingOf(displayMode, response.getBody(),
                reqDTO.getOutTradeNo(), response);
    }
}
