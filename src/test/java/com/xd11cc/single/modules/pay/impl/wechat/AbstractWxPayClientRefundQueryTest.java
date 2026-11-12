package com.xd11cc.single.modules.pay.impl.wechat;

import com.github.binarywang.wxpay.bean.request.WxPayRefundQueryV3Request;
import com.github.binarywang.wxpay.bean.result.WxPayRefundQueryV3Result;
import com.github.binarywang.wxpay.service.WxPayService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

/**
 * V3 退款查询入参：曾把 outTradeNo 传进 outRefundNo，导致查不到单或命中错误退款。
 *
 * @author xd11cc
 */
class AbstractWxPayClientRefundQueryTest {

    private static final String OUT_TRADE_NO = "TRADE_NO_0001";
    private static final String OUT_REFUND_NO = "REFUND_NO_0001";

    @Test
    void getRefund_v3_请求使用退款单号而不是订单号() throws Exception {
        WxPayClientConfig config = new WxPayClientConfig();
        config.setApiVersion(WxPayClientConfig.API_VERSION_V3);

        WxAppPayClient payClient = new WxAppPayClient(1L, config);

        WxPayRefundQueryV3Result result = new WxPayRefundQueryV3Result();
        // 用 PROCESSING 避开成功时间/退款单号的解析分支，只聚焦请求参数
        result.setStatus("PROCESSING");

        WxPayService wxPayService = mock(WxPayService.class);
        given(wxPayService.refundQueryV3(any(WxPayRefundQueryV3Request.class))).willReturn(result);
        inject(payClient, wxPayService);

        payClient.getRefund(OUT_TRADE_NO, OUT_REFUND_NO);

        ArgumentCaptor<WxPayRefundQueryV3Request> captor =
                ArgumentCaptor.forClass(WxPayRefundQueryV3Request.class);
        then(wxPayService).should().refundQueryV3(captor.capture());

        assertThat(captor.getValue().getOutRefundNo())
                .as("V3 退款查询必须按商户退款单号查询")
                .isEqualTo(OUT_REFUND_NO)
                .isNotEqualTo(OUT_TRADE_NO);
    }

    private static void inject(WxAppPayClient payClient, WxPayService wxPayService) throws Exception {
        Field field = AbstractWxPayClient.class.getDeclaredField("client");
        field.setAccessible(true);
        field.set(payClient, wxPayService);
    }
}
