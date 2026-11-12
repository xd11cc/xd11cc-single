package com.xd11cc.single.modules.pay;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import javax.validation.Validator;

/**
 * 支付渠道配置的公共契约，各渠道用子类承载自己的字段。
 * <p>
 * 配置若按本接口类型做 JSON 序列化/反序列化，必须连同具体子类信息一起写入
 * （{@code @JsonTypeInfo} 产生的 {@code @class}），否则读回来只能得到接口本身、
 * 渠道字段全部丢失；{@code @JsonIgnoreProperties} 则允许渠道参数增删后，
 * 旧 JSON 仍能解析而不是直接反序列化失败。
 * </p>
 *
 * @author xd11cc
 * @date 2026-06-05 16:46:59
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@JsonIgnoreProperties(ignoreUnknown = true)
public interface PayClientConfig {

    /**
     * 参数校验
     * @param validator 校验对象
     */
    void validate(Validator validator);
}
