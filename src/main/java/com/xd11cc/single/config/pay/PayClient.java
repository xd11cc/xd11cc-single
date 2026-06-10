package com.xd11cc.single.config.pay;

import com.xd11cc.single.entity.dto.PayOrderRespDTO;
import com.xd11cc.single.entity.dto.PayOrderUnifiedReqDTO;
import com.xd11cc.single.entity.dto.PayRefundRespDTO;
import com.xd11cc.single.entity.dto.PayRefundUnifiedReqDTO;

import java.util.Map;

/**
 * @author xd11cc
 * @date 2026-06-05 09:41:52
 * @description 支付客户端，用于对接各种渠道支付 SDK，实现发起支付，退款等功能
 */
public interface PayClient<Config> {

    /**
     * 获取渠道编号
     * @return
     */
    Long getId();

    /**
     * 获取渠道配置
     * @return
     */
    Config getConfig();

    // ==== 支付相关 ====

    /**
     * 调用支付渠道，统一下单
     * @param reqDTO 下单信息
     * @return 支付订单信息
     */
    PayOrderRespDTO unifiedOrder(PayOrderUnifiedReqDTO reqDTO);

    /**
     * 解析 Order 回调参数
     * @param params HTTP 回调接口 content type 为 application/x-www-form-urlencoded 的所有参数
     * @param body HTTP 回调接口的 request body
     * @param headers HTTP 回调接口的 request headers
     * @return 支付订单信息
     */
    PayOrderRespDTO parseOrderNotify(Map<String, String> params, String body, Map<String, String> headers);

    /**
     * 获得支付订单信息
     * @param outTradeNo 外部订单号
     * @return 支付订单信息
     */
    PayOrderRespDTO getOrder(String outTradeNo);

    // ==== 退款相关 ====

    /**
     * 调用支付渠道，进行退款
     */
    PayRefundRespDTO unifiedRefund(PayRefundUnifiedReqDTO reqDTO);

    /**
     * 解析 refund 回调数据
     * @param params HTTP 回调接口 content type 为 application/x-www-form-urlencoded 的所有参数
     * @param body HTTP 回调接口的 request body
     * @param headers HTTP 回调接口的 request headers
     * @return 退款订单信息
     */
    PayRefundRespDTO parseRefundNotify(Map<String, String> params, String body, Map<String, String> headers);

    /**
     * 获得退款订单信息
     * @param outTradeNo 外部订单号
     * @param outRefundNo 外部退款号
     * @return 退款订单信息
     */
    PayRefundRespDTO getRefund(String outTradeNo, String outRefundNo);
}
