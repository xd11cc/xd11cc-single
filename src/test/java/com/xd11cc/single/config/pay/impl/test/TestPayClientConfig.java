package com.xd11cc.single.config.pay.impl.test;

import com.xd11cc.single.config.pay.PayClientConfig;
import lombok.Data;

import javax.validation.Validator;

/**
 * 单测用 PayClientConfig 实现，无业务字段。
 * <p>
 * 继承本类（而非直接使用 PayClientConfig）是为了让 Hutool {@code ReflectUtil.newInstance}
 * 在解析构造函数参数类型时能精确匹配 {@link PayClientTestStub} 的
 * {@code (Long, TestPayClientConfig)} 构造函数。
 * </p>
 */
@Data
public class TestPayClientConfig implements PayClientConfig {

    @Override
    public void validate(Validator validator) {
        // no-op for unit test
    }
}
