package com.xd11cc.single.config.pay.impl;

import com.alibaba.fastjson2.JSON;
import com.xd11cc.single.config.exception.PayClientException;
import com.xd11cc.single.config.exception.ServiceException;
import com.xd11cc.single.config.pay.PayClient;
import com.xd11cc.single.config.pay.PayClientConfig;
import com.xd11cc.single.entity.dto.PayOrderRespDTO;
import com.xd11cc.single.entity.dto.PayOrderUnifiedReqDTO;
import com.xd11cc.single.entity.dto.PayRefundRespDTO;
import com.xd11cc.single.entity.dto.PayRefundUnifiedReqDTO;
import com.xd11cc.single.utils.ValidationUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * @author xd11cc
 * @date 2026-06-05 16:54:05
 * @description 支付客户端的抽象类，提供模版方法，减少子类的代码冗余
 */
@Slf4j
public abstract class AbstractPayClient<Config extends PayClientConfig> implements PayClient<Config> {

    /**
     * 渠道编号
     */
    private final Long channelId;

    /**
     * 渠道编码
     */
    @SuppressWarnings("FieldCanBeLocal")
    private final String channelCode;

    /**
     * 支付配置
     */
    protected Config config;

    public AbstractPayClient(Long channelId, String channelCode, Config config) {
        this.channelId = channelId;
        this.channelCode = channelCode;
        this.config = config;
    }

    /**
     * 初始化
     */
    public final void init(){
        doInit();
        log.debug("[init][客户端({}) 初始化完成]", getId());
    }

    protected abstract void doInit();

    public final void refresh(Config config){
        // 判断是否更新
        if (config.equals(this.config)){
            return;
        }
        log.info("[refresh][客户端({})发生变化，重新初始化]", getId());
        this.config = config;
        // 初始化
        this.init();
    }

    @Override
    public Long getId() {
        return channelId;
    }

    @Override
    public Config getConfig() {
        return config;
    }

    // ============ 支付相关 ============

    @Override
    public final PayOrderRespDTO unifiedOrder(PayOrderUnifiedReqDTO reqDTO) {
        ValidationUtils.validate(reqDTO);
        // 执行统一下单
        PayOrderRespDTO respDTO;
        try {
            respDTO = doUnifiedOrder(reqDTO);
        } catch (ServiceException ex) {
            throw ex;
        } catch (Throwable ex){
            log.error("[unifiedOrder][客户端({}) request({}) 发起支付异常]",
                    getId(), JSON.toJSONString(reqDTO), ex);
            throw buildPayException(ex);
        }
        return respDTO;
    }

    protected abstract PayOrderRespDTO doUnifiedOrder(PayOrderUnifiedReqDTO reqDTO)
            throws Throwable;

    @Override
    public final PayOrderRespDTO parseOrderNotify(Map<String, String> params, String body, Map<String, String> headers) {
        try {
            return doParseOrderNotify(params, body, headers);
        } catch (ServiceException ex) {
            throw ex;
        } catch (Throwable ex){
           log.error("[parseOrderNotify][客户端({}) params({}) body({}) headers({}) 解析失败]",
                   getId(), JSON.toJSONString(params), body, headers, ex);
           throw buildPayException(ex);
        }
    }

    protected abstract PayOrderRespDTO doParseOrderNotify(Map<String, String> params, String body, Map<String, String> headers)
            throws Throwable;

    @Override
    public final PayOrderRespDTO getOrder(String outTradeNo) {
        try {
            return doGetOrder(outTradeNo);
        } catch (ServiceException ex) {
            throw ex;
        } catch (Throwable ex){
            log.error("[getOrder][客户端({}) outTradeNo({}) 查询支付单异常]",
                    getId(), outTradeNo, ex);
            throw buildPayException(ex);
        }
    }

    protected abstract PayOrderRespDTO doGetOrder(String outTradeNo)
            throws Throwable;

    // ============ 退款相关 =============

    @Override
    public final PayRefundRespDTO unifiedRefund(PayRefundUnifiedReqDTO reqDTO) {
        ValidationUtils.validate(reqDTO);
        // 执行统一退款
        PayRefundRespDTO respDTO;
        try {
            respDTO = doUnifiedRefund(reqDTO);
        } catch (ServiceException ex) {
            throw ex;
        } catch (Throwable ex){
            log.error("[unifiedRefund][客户端({}) request({}) 发起退款异常]",
                    getId(), JSON.toJSONString(reqDTO), ex);
            throw buildPayException(ex);
        }
        return respDTO;
    }

    protected abstract PayRefundRespDTO doUnifiedRefund(PayRefundUnifiedReqDTO reqDTO)
            throws Throwable;

    @Override
    public final PayRefundRespDTO parseRefundNotify(Map<String, String> params, String body, Map<String, String> headers) {
        try {
            return doParseRefundNotify(params, body, headers);
        } catch (ServiceException ex) {
            throw ex;
        } catch (Throwable ex){
            log.error("[parseRefundNotify][客户端({}) params({}) body({}) headers({}) 解析失败]",
                    getId(), params, body, headers, ex);
            throw buildPayException(ex);
        }
    }

    protected abstract PayRefundRespDTO doParseRefundNotify(Map<String, String> params, String body, Map<String, String> headers)
            throws Throwable;

    @Override
    public final PayRefundRespDTO getRefund(String outTradeNo, String outRefundNo) {
        try {
            return doGetRefund(outTradeNo, outRefundNo);
        } catch (ServiceException ex) {
            throw ex;
        } catch (Throwable ex){
            log.error("[unifiedRefund][客户端({}) outTradeNo({}) outRefundNo({}) 查询退款单异常]",
                    getId(), outTradeNo, outRefundNo, ex);
            throw buildPayException(ex);
        }
    }

    protected abstract PayRefundRespDTO doGetRefund(String outTradeNo, String outRefundNo)
            throws Throwable;

    // ===================各种工具方法======================

    private PayClientException buildPayException(Throwable ex) {
        if (ex instanceof PayClientException) {
            return (PayClientException) ex;
        }
        return new PayClientException(ex);
    }
}
