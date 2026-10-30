package com.xd11cc.single.common.ratelimit;

import com.xd11cc.single.common.context.TenantContextHolder;
import com.xd11cc.single.utils.IpUtils;
import com.xd11cc.single.utils.SecurityUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link RateLimitKeyResolver} 的测试。
 * <p>
 * 身份派生与限流机制分开后，这里可以独立验证「每个维度派生出的 key 长什么样」，
 * 包括租户后缀，不需要碰 Redis。
 * </p>
 *
 * @author xd11cc
 */
class RateLimitKeyResolverTest {

    private final RateLimitKeyResolver resolver = new RateLimitKeyResolver();

    private MockedStatic<IpUtils> ipUtils;
    private MockedStatic<SecurityUtils> securityUtils;

    private Method sampleMethod;

    @BeforeEach
    void setUp() throws Exception {
        ipUtils = Mockito.mockStatic(IpUtils.class);
        securityUtils = Mockito.mockStatic(SecurityUtils.class);
        sampleMethod = Sample.class.getDeclaredMethod("login", String.class);
        TenantContextHolder.setTenantId(7L);
    }

    @AfterEach
    void tearDown() {
        ipUtils.close();
        securityUtils.close();
        TenantContextHolder.clear();
    }

    @Test
    @DisplayName("IP 维度：带租户后缀")
    void resolve_ip() {
        ipUtils.when(IpUtils::getIpAddr).thenReturn("1.2.3.4");

        assertThat(resolver.resolve(Subject.IP, "", sampleMethod, new Object[]{"alice"}))
                .isEqualTo("ip:1.2.3.4:t7");
    }

    @Test
    @DisplayName("USER 维度：未登录抛 UNAUTHORIZED")
    void resolve_user_withoutLogin() {
        securityUtils.when(SecurityUtils::getUserId).thenReturn(null);

        assertThatThrownBy(() -> resolver.resolve(Subject.USER, "", sampleMethod, new Object[]{"alice"}))
                .isInstanceOf(com.xd11cc.single.common.exception.ServiceException.class);
    }

    @Test
    @DisplayName("USER 维度：已登录用户")
    void resolve_user() {
        securityUtils.when(SecurityUtils::getUserId).thenReturn(88L);

        assertThat(resolver.resolve(Subject.USER, "", sampleMethod, new Object[]{"alice"}))
                .isEqualTo("user:88:t7");
    }

    @Test
    @DisplayName("METHOD 维度：类名.方法名")
    void resolve_method() {
        assertThat(resolver.resolve(Subject.METHOD, "", sampleMethod, new Object[]{"alice"}))
                .isEqualTo("method:Sample.login:t7");
    }

    @Test
    @DisplayName("EXPRESSION 维度：按 SpEL 从参数取值")
    void resolve_expression() {
        assertThat(resolver.resolve(Subject.EXPRESSION, "#p0", sampleMethod, new Object[]{"alice"}))
                .isEqualTo("expr:alice:t7");
    }

    @Test
    @DisplayName("EXPRESSION 维度：表达式为空直接报错")
    void resolve_expression_missingExpression() {
        assertThatThrownBy(() -> resolver.resolve(Subject.EXPRESSION, "", sampleMethod, new Object[]{"alice"}))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("expression");
    }

    @Test
    @DisplayName("无租户上下文时不加后缀")
    void resolve_withoutTenant() {
        TenantContextHolder.clear();
        TenantContextHolder.setTenantAware(false);

        assertThat(resolver.resolve(Subject.METHOD, "", sampleMethod, new Object[]{"alice"}))
                .isEqualTo("method:Sample.login");
    }

    /**
     * 供反射取方法用
     */
    @SuppressWarnings("unused")
    private static class Sample {
        void login(String username) {
        }
    }
}
