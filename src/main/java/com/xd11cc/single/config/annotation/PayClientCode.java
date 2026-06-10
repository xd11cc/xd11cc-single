package com.xd11cc.single.config.annotation;

import com.xd11cc.single.enums.PayChannelEnum;

import java.lang.annotation.*;

/**
 * @author xd11cc
 * @date 2026-06-09 15:19:12
 * @description 标记支付客户端所属的渠道编码。
 *              工厂通过此注解自动发现并注册渠道 → 客户端的映射关系。
 *
 * 使用方式：
 * <pre>
 * @PayClientCode(PayChannelEnum.ALIPAY_WAP)
 * public class AlipayWapPayClient extends AbstractAlipayPayClient { ... }
 * </pre>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PayClientCode {

    /**
     * 渠道枚举，对应 {@link PayChannelEnum}
     * @return
     */
    PayChannelEnum value();
}
