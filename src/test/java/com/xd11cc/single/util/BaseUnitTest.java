package com.xd11cc.single.util;

import com.xd11cc.single.config.context.TenantContextHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * 所有单元测试的基类。
 * <ul>
 *     <li>统一注入 Mockito 扩展，子类可直接用 {@code @Mock}/{@code @InjectMocks}</li>
 *     <li>每个用例执行完毕后清空多租户上下文，避免用例间污染</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
public class BaseUnitTest {

    @BeforeEach
    void setUp() {
        TenantContextHolder.clear();
    }

    @AfterEach
    void tearDown() {
        TenantContextHolder.clear();
    }
}
