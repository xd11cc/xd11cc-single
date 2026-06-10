package com.xd11cc.single.config.pay;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import javax.validation.Validator;

/**
 * @author xd11cc
 * @date 2026-06-05 16:46:59
 * @description 支付客户端的配置，本质是支付渠道的配置
 *              每个不同的渠道，需要不同的配置，通过子类定义
 *              @JsonTypeInfo 注解的作用：Jackson多态
 *              1.序列化到数据库时，增加 @class 属性
 *              2.反序列化到内存对象时，通过 @class 属性，可以创建正确的类型
 *              @JsonIgnoreProperties 注解的作用：忽略未知的属性，避免反序列化失败
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
