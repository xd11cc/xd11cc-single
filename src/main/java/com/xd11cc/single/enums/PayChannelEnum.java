package com.xd11cc.single.enums;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.xd11cc.single.config.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

/**
 * @author xd11cc
 * @date 2026-06-08 09:36:45
 * @description
 */
@Getter
@AllArgsConstructor
public enum PayChannelEnum implements ArrayValuable<String> {

    WX_PUB("wx_pub", "微信 JSAPI 支付"),
    WX_LITE("wx_lite", "微信小程序支付"),
    WX_APP("wx_app", "微信 App 支付"),
    WX_NATIVE("wx_native", "微信 Native 支付"),
    WX_WAP("wx_wap", "微信 Wap 网站支付"),
    WX_BAR("wx_bar", "微信付款码支付"),

    ALIPAY_PC("alipay_pc", "支付宝 PC 网站支付"),
    ALIPAY_WAP("alipay_wap", "支付宝 Wap 网站支付"),
    ALIPAY_APP("alipay_app", "支付宝 App 支付"),
    ALIPAY_QR("alipay_qr", "支付宝扫码支付"),
    ALIPAY_BAR("alipay_bar", "支付宝付款码支付"),

    WALLET("wallet", "钱包支付"),
    ;

    public static final String[] ARRAYS = Arrays.stream(values()).map(PayChannelEnum::getCode).toArray(String[]::new);

    /**
     * 编码
     *
     * 参考 <a href="https://www.pingxx.com/api/支付渠道属性值.html">支付渠道属性值</a>
     */
    private final String code;

    private final String name;

    @Override
    public String[] array() {
        return ARRAYS;
    }

    public static PayChannelEnum getByCode(String code){
        return ArrayUtil.firstMatch(e ->e.getCode().equals(code), values());
    }

    public static boolean isAliPay(String channelCode){
        return StringUtils.startsWith(channelCode, "alipay_");
    }

    public static boolean isWeChatPay(String channelCode){
        return StringUtils.startsWith(channelCode, "wx_");
    }
}
