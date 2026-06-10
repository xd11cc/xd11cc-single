package com.xd11cc.single.config.pay.impl.alipay;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.alipay.api.domain.AlipayTradePayModel;
import com.alipay.api.request.AlipayTradePayRequest;
import com.alipay.api.response.AlipayTradePayResponse;
import com.xd11cc.single.config.annotation.PayClientCode;
import com.xd11cc.single.config.exception.ServiceException;
import com.xd11cc.single.entity.dto.PayOrderRespDTO;
import com.xd11cc.single.entity.dto.PayOrderUnifiedReqDTO;
import com.xd11cc.single.enums.PayChannelEnum;
import com.xd11cc.single.enums.PayOrderDisplayModeEnum;
import com.xd11cc.single.enums.SystemErrorEnum;

import java.time.LocalDateTime;
import java.util.Objects;

import static com.xd11cc.single.config.pay.impl.alipay.AlipayPayClientConfig.MODE_CERTIFICATE;

/**
 * @author xd11cc
 * @date 2026-06-08 14:55:48
 * @description 支付宝【条码支付】的 PayClient 实现类
 * 文档：<a href="https://opendocs.alipay.com/open/194/105072">当面付</a>
 */
@PayClientCode(PayChannelEnum.ALIPAY_BAR)
public class AlipayBarPayClient extends AbstractAlipayPayClient {

    public AlipayBarPayClient(Long channelId, AlipayPayClientConfig config) {
        super(channelId, PayChannelEnum.ALIPAY_BAR.getCode(), config);
    }

    @Override
    protected PayOrderRespDTO doUnifiedOrder(PayOrderUnifiedReqDTO reqDTO) throws Throwable {
        String authCode = MapUtil.getStr(reqDTO.getChannelExtras(), "auth_code");
        if (StrUtil.isEmpty(authCode)) {
            throw new ServiceException(SystemErrorEnum.BAD_REQUEST1, new Object[]{"条形码不能为空"});
        }
        // 1.1 构建 AlipayTradePayModel 请求
        AlipayTradePayModel model = new AlipayTradePayModel();
        // 1️⃣通用的参数
        model.setOutTradeNo(reqDTO.getOutTradeNo());
        model.setSubject(reqDTO.getSubject());
        model.setBody(reqDTO.getBody());
        model.setTotalAmount(formatAmount(reqDTO.getPrice()));
        model.setScene("bar_code"); // 当面付条码支付场景
        // 2️⃣个性化参数
        model.setAuthCode(authCode);
        // 3️⃣支付宝条码支付只有一种提示
        String displayMode = PayOrderDisplayModeEnum.BAR_CODE.getMode();

        // 1.2 构建 AlipayTradePayRequest 请求
        AlipayTradePayRequest request = new AlipayTradePayRequest();
        request.setBizModel(model);
        request.setNotifyUrl(reqDTO.getNotifyUrl());
        request.setReturnUrl(reqDTO.getReturnUrl());

        // 2.1 执行请求
        AlipayTradePayResponse response;
        if (Objects.equals(config.getMode(), MODE_CERTIFICATE)){
            // 证书模式
            response = client.certificateExecute(request);
        }else {
            response = client.execute(request);
        }
        // 处理结果
        if (!response.isSuccess()) {
            return buildClosedPayOrderRespDTO(reqDTO, response);
        }
        if ("10000".equals(response.getCode())) { // 免密支付
            LocalDateTime successTime = LocalDateTimeUtil.of(response.getGmtPayment());
            PayOrderRespDTO payOrderRespDTO = PayOrderRespDTO.successOf(response.getTradeNo(), response.getBuyerUserId(), successTime,
                    response.getOutTradeNo(), response);
            payOrderRespDTO.setDisplayMode(displayMode);
            payOrderRespDTO.setDisplayContent("");
            return payOrderRespDTO;
        }
        // 大额支付，需要用户输入密码，所以返回 waiting。此时，前端一般会进行轮询
        return PayOrderRespDTO.waitingOf(displayMode, "",
                reqDTO.getOutTradeNo(), response);
    }
}
