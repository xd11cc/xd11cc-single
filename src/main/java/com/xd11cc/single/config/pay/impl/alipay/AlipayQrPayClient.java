package com.xd11cc.single.config.pay.impl.alipay;

import com.alipay.api.AlipayApiException;
import com.alipay.api.domain.AlipayTradePrecreateModel;
import com.alipay.api.request.AlipayTradePrecreateRequest;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.xd11cc.single.config.annotation.PayClientCode;
import com.xd11cc.single.entity.dto.PayOrderRespDTO;
import com.xd11cc.single.entity.dto.PayOrderUnifiedReqDTO;
import com.xd11cc.single.enums.PayChannelEnum;
import com.xd11cc.single.enums.PayOrderDisplayModeEnum;

import java.util.Objects;

import static com.xd11cc.single.config.pay.impl.alipay.AlipayPayClientConfig.MODE_CERTIFICATE;

/**
 * @author xd11cc
 * @date 2026-06-08 14:21:08
 * @description 支付宝【扫码支付】的 PayClient 实现类
 * 文档：<a href="https://opendocs.alipay.com/apis/02890k">扫码支付</a>
 */
@PayClientCode(PayChannelEnum.ALIPAY_QR)
public class AlipayQrPayClient extends AbstractAlipayPayClient {

    public AlipayQrPayClient(Long channelId, AlipayPayClientConfig config) {
        super(channelId, PayChannelEnum.ALIPAY_QR.getCode(), config);
    }

    @Override
    protected PayOrderRespDTO doUnifiedOrder(PayOrderUnifiedReqDTO reqDTO) throws AlipayApiException {
        // 1.1 构造 AlipayTradePrecreateModel 请求
        AlipayTradePrecreateModel model = new AlipayTradePrecreateModel();
        // 1️⃣通用参数
        model.setOutTradeNo(reqDTO.getOutTradeNo());
        model.setSubject(reqDTO.getSubject());
        model.setBody(reqDTO.getBody());
        model.setTotalAmount(formatAmount(reqDTO.getPrice()));
        model.setProductCode("FACE_TO_FACE_PAYMENT"); // 销售产品码，目前扫码支付场景下仅支持 FACE_TO_FACE_PAYMENT
        // 2️⃣个性化参数【无】
        // 3️⃣支付宝扫码支付只有一种展示，考虑到前端可能希望二维码扫描后，手机打开
        String displayMode = PayOrderDisplayModeEnum.QR_CODE.getMode();

        // 1.2 构造 AlipayTradePrecreateRequest 请求
        AlipayTradePrecreateRequest request = new AlipayTradePrecreateRequest();
        request.setBizModel(model);
        request.setNotifyUrl(reqDTO.getNotifyUrl());
        request.setReturnUrl(reqDTO.getReturnUrl());

        // 2.1 执行请求
        AlipayTradePrecreateResponse response;
        if (Objects.equals(config.getMode(), MODE_CERTIFICATE)){
            // 证书模式
            response = client.certificateExecute(request);
        }else {
            response = client.execute(request);
        }
        // 2.2 处理结果
        if (!response.isSuccess()){
            return buildClosedPayOrderRespDTO(reqDTO, response);
        }
        return PayOrderRespDTO.waitingOf(displayMode, response.getQrCode(),
                reqDTO.getOutTradeNo(), response);
    }
}
