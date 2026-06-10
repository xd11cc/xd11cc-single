package com.xd11cc.single.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author xd11cc
 * @date 2026-06-05 15:22:57
 * @description 支付 UI 展示模式
 */
@Getter
@AllArgsConstructor
public enum PayOrderDisplayModeEnum {

    URL("url"), // Redirect 跳转方式
    IFRAME("iframe"), // Iframe 内嵌链接方式
    FORM("form"), // HTML 表单提交
    QR_CODE("qr_code"), // 二维码的文字内容
    QR_CODE_URL("qr_code_url"), // 二维码的图片链接
    BAR_CODE("bar_code"), // 条形码
    APP("app"), // 应用：Android、IOS、微信小程序、微信公众号等，需要自定义处理的
    ;

    private final String mode;
}
