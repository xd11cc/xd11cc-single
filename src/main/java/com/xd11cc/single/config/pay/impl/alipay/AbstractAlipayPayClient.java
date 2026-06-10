package com.xd11cc.single.config.pay.impl.alipay;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayConfig;
import com.alipay.api.AlipayResponse;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradeFastpayRefundQueryModel;
import com.alipay.api.domain.AlipayTradeRefundModel;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.internal.util.AntCertificationUtil;
import com.alipay.api.internal.util.codec.Base64;
import com.alipay.api.request.AlipayTradeFastpayRefundQueryRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.response.AlipayTradeFastpayRefundQueryResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.xd11cc.single.config.pay.impl.AbstractPayClient;
import com.xd11cc.single.entity.dto.PayOrderRespDTO;
import com.xd11cc.single.entity.dto.PayOrderUnifiedReqDTO;
import com.xd11cc.single.entity.dto.PayRefundRespDTO;
import com.xd11cc.single.entity.dto.PayRefundUnifiedReqDTO;
import com.xd11cc.single.enums.PayOrderStatusEnum;
import com.xd11cc.single.utils.ObjectUtils;
import lombok.SneakyThrows;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

import static com.xd11cc.single.config.pay.impl.alipay.AlipayPayClientConfig.MODE_PUBLIC_KEY;
import static com.xd11cc.single.config.pay.impl.alipay.AlipayPayClientConfig.MODE_CERTIFICATE;

/**
 * @author xd11cc
 * @date 2026-06-08 10:17:11
 * @description 支付宝抽象类，实现支付宝统一的接口、以及部分实现（退款）
 */
public abstract class AbstractAlipayPayClient extends AbstractPayClient<AlipayPayClientConfig> {

    protected DefaultAlipayClient client;

    public AbstractAlipayPayClient(Long channelId, String channelCode, AlipayPayClientConfig config) {
        super(channelId, channelCode, config);
    }

    @Override
    @SneakyThrows
    protected void doInit() {
        AlipayConfig alipayConfig = new AlipayConfig();
        BeanUtil.copyProperties(config, alipayConfig, false);
        this.client = new DefaultAlipayClient(alipayConfig);
    }

    // ========= 支付相关 =========

    /**
     * 构造支付关闭的 {@link PayOrderRespDTO} 对象
     * @param reqDTO
     * @param response
     * @return
     */
    protected PayOrderRespDTO buildClosedPayOrderRespDTO(PayOrderUnifiedReqDTO reqDTO, AlipayResponse response) {
        Assert.isFalse(response.isSuccess());
        return PayOrderRespDTO.closedOf(response.getSubCode(), response.getSubMsg(),
                reqDTO.getOutTradeNo(), response);
    }


    @Override
    protected PayOrderRespDTO doGetOrder(String outTradeNo) throws Throwable {
        // 1.1 构建 AlipayTradeRefundModel 请求
        AlipayTradeRefundModel model = new AlipayTradeRefundModel();
        model.setOutTradeNo(outTradeNo);
        // 1.2 构建 AlipayTradeQueryRequest 请求
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        request.setBizModel(model);
        AlipayTradeQueryResponse response;
        if (Objects.equals(config.getMode(), MODE_CERTIFICATE)){
            // 证书模式
            response = client.certificateExecute(request);
        }else {
            response = client.execute(request);
        }
        if (!response.isSuccess()){ // 不成功，说明订单不存在
            return PayOrderRespDTO.closedOf(response.getSubCode(), response.getSubMsg(),
                    outTradeNo, response);
        }
        // 2.2 解析订单的状态
        Integer status = parseStatus(response.getTradeStatus());
        Assert.notNull(status, () ->{
            throw new IllegalArgumentException(StrUtil.format("body({}) 的 trade_status 不正确", response.getBody()));
        });
        return PayOrderRespDTO.of(status, response.getTradeNo(), response.getBuyerUserId(), LocalDateTimeUtil.of(response.getSendPayDate()),
                outTradeNo, response);
    }

    @Override
    protected PayOrderRespDTO doParseOrderNotify(Map<String, String> params, String body, Map<String, String> headers) throws Throwable {
        // 1. 校验回调参数
        verifyNotifyData(params);

        // 2.解析订单状态
        // 额外说明：支付宝不仅仅支付成功会回调，再各种触发支付单数据变化时，都会进行回调，所以这里 status 的解析会写的比较复杂
        Map<String, String> bodyObj = HttpUtil.decodeParamMap(body, StandardCharsets.UTF_8);
        Integer status = this.parseStatus(bodyObj.get("trade_status"));
        // 特殊逻辑：支付宝没有退款成功的状态，所以，如果有退款金额，我们认为是退款成功
        if (MapUtil.getDouble(bodyObj, "refund_fee", 0D) > 0){
            status = PayOrderStatusEnum.REFUND.getStatus();
        }
        Assert.notNull(status, (Supplier<Throwable>) () ->{
            throw new IllegalArgumentException(StrUtil.format("body({}) 的 trade_status 不正确", body));
        });
        return PayOrderRespDTO.of(status, bodyObj.get("trade_no"), bodyObj.get("seller_id"), parseTime(params.get("gmt_payment")),
                bodyObj.get("out_trade_no"), body);


    }

    private Integer parseStatus(String tradeStatus) {
        return Objects.equals("WAIT_BUYER_PAY", tradeStatus) ? PayOrderStatusEnum.WAITING.getStatus()
                : ObjectUtils.equalsAny(tradeStatus, "TRADE_FINISHED", "TRADE_SUCCESS") ? PayOrderStatusEnum.SUCCESS.getStatus()
                : Objects.equals("TRADE_CLOSED", tradeStatus) ? PayOrderStatusEnum.CLOSED.getStatus() : null;
    }

    // ======== 退款相关 =========

    /**
     * 支付宝统一的退款接口
     * @param reqDTO 退款请求 request DTO
     * @return 退款请求 response
     */
    @Override
    protected PayRefundRespDTO doUnifiedRefund(PayRefundUnifiedReqDTO reqDTO) throws AlipayApiException {
        // 1.1 构造 AlipayTradeRefundModel 请求
        AlipayTradeRefundModel model = new AlipayTradeRefundModel();
        model.setOutTradeNo(reqDTO.getOutTradeNo());
        model.setOutRequestNo(reqDTO.getOutRefundNo());
        model.setRefundAmount(formatAmount(reqDTO.getRefundPrice()));
        model.setRefundReason(reqDTO.getReason());
        // 1.2 构造 AlipayTradeRefundRequest 请求
        AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();
        request.setBizModel(model);

        // 2.1 执行请求
        AlipayTradeRefundResponse response;
        if (Objects.equals(config.getMode(), MODE_CERTIFICATE)){
            response = client.certificateExecute(request);
        }else {
            response = client.execute(request);
        }
        if (!response.isSuccess()){
            // 当出现 ACQ.SYSTEM_ERROR，退款可能成功也可能失败。返回 WAIT 状态，后续 job 会轮询
            if (ObjectUtils.equalsAny(response.getBuyerLogonId(), "ACQ:SYSTEM_ERROR", "SYSTEM_ERROR")){
                return PayRefundRespDTO.waitingOf(null, reqDTO.getOutRefundNo(), response);
            }
            return PayRefundRespDTO.failureOf(response.getSubCode(), response.getSubMsg(), reqDTO.getOutRefundNo(), response);
        }
        // 2.2 创建返回结果
        // 支付宝只要退款调用返回 success，就认为退款成功，不需要回调。具体可见 parseNotify 方法的说明。
        // 另外，支付宝没有退款单号，所以不用设置
        return PayRefundRespDTO.successOf(null, LocalDateTimeUtil.of(response.getGmtRefundPay()),
                reqDTO.getOutRefundNo(), response);
    }

    @Override
    protected PayRefundRespDTO doParseRefundNotify(Map<String, String> params, String body, Map<String, String> headers) throws Throwable {
        // 补充说明：支付宝退款时，没有回调，这点和微信支付是不同的。并且，退款分成部分退款、和全部退款。
        // ① 部分退款：是会有回调，但是它回调的是订单状态的同步回调，不是退款订单的回调
        // ② 全部退款：Wap 支付有订单状态的同步回调，但是 PC/扫码又没有
        // 所以，这里在解析时，即使是退款导致的订单状态同步，我们也忽略不做为“退款同步”，而是订单的回调。
        // 实际上，支付宝退款只要发起成功，就可以认为退款成功，不需要等待回调。
        throw new UnsupportedOperationException("支付宝无退款回调");
    }

    @Override
    protected PayRefundRespDTO doGetRefund(String outTradeNo, String outRefundNo) throws Throwable {
        // 1.1 构造 AlipayTradeFastpayRefundQueryModel 请求
        AlipayTradeFastpayRefundQueryModel model = new AlipayTradeFastpayRefundQueryModel();
        model.setOutTradeNo(outTradeNo);
        model.setOutRequestNo(outRefundNo);
        model.setQueryOptions(Collections.singletonList("gmt_refund_pay"));
        // 1.2 构造 AlipayTradeFastpayRefundQueryRequest 请求
        AlipayTradeFastpayRefundQueryRequest request = new AlipayTradeFastpayRefundQueryRequest();
        request.setBizModel(model);

        // 2.1 执行请求
        AlipayTradeFastpayRefundQueryResponse response;
        if (Objects.equals(config.getMode(), MODE_CERTIFICATE)){
            response = client.certificateExecute(request);
        }else {
            response = client.execute(request);
        }
        if (!response.isSuccess()){
            // 明确不存在的情况，应该就是失败，可进行关闭
            if (ObjectUtils.equalsAny(response.getSubCode(), "TRADE_NOT_EXIST", "TRADE_FINISHED", "ACQ:TRADE_NOT_EXIST")){
                return PayRefundRespDTO.failureOf(outRefundNo, response);
            }
            // 可能存在"ACQ.SYSTEM_ERROR"系统错误等情况，所以返回 WAIT 继续等待
            return PayRefundRespDTO.waitingOf(null, outRefundNo, response);
        }
        // 2.2 创建返回结果
        if (Objects.equals(response.getRefundStatus(), "REFUND_SUCCESS")){
            return PayRefundRespDTO.successOf(null, LocalDateTimeUtil.of(response.getGmtRefundPay()),
                    outRefundNo, response);
        }
        return PayRefundRespDTO.waitingOf(null, outRefundNo, response);
    }

    /**
     * 校验回调参数
     * @param params 回调参数
     */
    protected void verifyNotifyData(Map<String, String> params) throws AlipayApiException {
        boolean verify;
        if (Objects.equals(config.getMode(), MODE_PUBLIC_KEY)){
            verify = AlipaySignature.rsaCheckV1(params, config.getAlipayPublicKey(),
                    StandardCharsets.UTF_8.name(), config.getSignType());
        }else if (Objects.equals(config.getMode(), MODE_CERTIFICATE)){
            // 由于 rsaCertCheckV1 的第二个参数是 path 所以不能这么调用！！！通过阅读源码，发现可以采用如下方式！
            X509Certificate cert = AntCertificationUtil.getCertFromContent(config.getAlipayPublicCertContent());
            String publicKey = Base64.encodeBase64String(cert.getPublicKey().getEncoded());
            verify = AlipaySignature.rsaCheckV1(params, publicKey,
                    StandardCharsets.UTF_8.name(), config.getSignType());

        }else {
            throw new IllegalArgumentException("未知的公钥类型：" + config.getMode());
        }
        Assert.isTrue(verify, "验签结果不通过");
    }

    // ========= 各种工具 =========
    protected static String formatAmount(Integer amount) {
        return BigDecimal.valueOf(amount, 2).toString();
    }

    protected String formatTime(LocalDateTime time) {
        return LocalDateTimeUtil.format(time, DatePattern.NORM_DATETIME_PATTERN);
    }

    protected LocalDateTime parseTime(String str) {
        return LocalDateTimeUtil.parse(str, DatePattern.NORM_DATETIME_FORMATTER);
    }
}
