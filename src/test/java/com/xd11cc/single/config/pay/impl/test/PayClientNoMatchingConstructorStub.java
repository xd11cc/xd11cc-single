package com.xd11cc.single.config.pay.impl.test;

import com.xd11cc.single.config.pay.PayClient;
import com.xd11cc.single.config.pay.PayClientConfig;
import com.xd11cc.single.config.pay.impl.AbstractPayClient;
import com.xd11cc.single.entity.dto.PayOrderRespDTO;
import com.xd11cc.single.entity.dto.PayOrderUnifiedReqDTO;
import com.xd11cc.single.entity.dto.PayRefundRespDTO;
import com.xd11cc.single.entity.dto.PayRefundUnifiedReqDTO;

import java.util.Map;

/**
 * 故意不提供 {@code (Long, Config)} 构造函数的桩类，
 * 用于验证 {@code ReflectUtil.newInstance} 构造失败时的异常传播路径。
 */
public class PayClientNoMatchingConstructorStub extends AbstractPayClient<PayClientConfig> {

    public PayClientNoMatchingConstructorStub() {
        super(null, "no_match_stub", null);
    }

    @Override
    protected void doInit() {
        // no-op
    }

    @Override
    protected PayOrderRespDTO doUnifiedOrder(PayOrderUnifiedReqDTO reqDTO) throws Throwable {
        return null;
    }

    @Override
    protected PayOrderRespDTO doParseOrderNotify(Map<String, String> params, String body,
                                                  Map<String, String> headers) throws Throwable {
        return null;
    }

    @Override
    protected PayOrderRespDTO doGetOrder(String outTradeNo) throws Throwable {
        return null;
    }

    @Override
    protected PayRefundRespDTO doUnifiedRefund(PayRefundUnifiedReqDTO reqDTO) throws Throwable {
        return null;
    }

    @Override
    protected PayRefundRespDTO doParseRefundNotify(Map<String, String> params, String body,
                                                    Map<String, String> headers) throws Throwable {
        return null;
    }

    @Override
    protected PayRefundRespDTO doGetRefund(String outTradeNo, String outRefundNo) throws Throwable {
        return null;
    }
}
