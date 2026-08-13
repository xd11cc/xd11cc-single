package com.xd11cc.single.config.pay.impl.test;

import com.xd11cc.single.config.pay.PayClient;
import com.xd11cc.single.config.pay.PayClientConfig;
import com.xd11cc.single.config.pay.impl.AbstractPayClient;
import com.xd11cc.single.entity.dto.PayOrderRespDTO;
import com.xd11cc.single.entity.dto.PayOrderUnifiedReqDTO;
import com.xd11cc.single.entity.dto.PayRefundRespDTO;
import com.xd11cc.single.entity.dto.PayRefundUnifiedReqDTO;
import lombok.Data;

import javax.validation.Validator;
import java.util.Map;

/**
 * PayClientFactoryImpl 单测用桩客户端
 * <p>
 * 构造函数 {@code (Long, TestPayClientConfig)} 与 Hutool {@code ReflectUtil.newInstance}
 * 的参数类型对齐，确保 {@code createPayClient} 的反射调用能匹配。
 * </p>
 */
public class PayClientTestStub extends AbstractPayClient<PayClientConfig> {

    public PayClientTestStub(Long channelId, TestPayClientConfig config) {
        super(channelId, "test_stub", config);
    }

    @Override
    protected void doInit() {
        // no-op for unit test
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
