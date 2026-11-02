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

        assertThat(resolver.resolve(Subject.IP, sampleMethod))
                .isEqualTo("ip:1.2.3.4:t7");
    }

    @Test
    @DisplayName("USER 维度：未登录抛 UNAUTHORIZED")
    void resolve_user_withoutLogin() {
        securityUtils.when(SecurityUtils::getUserId).thenReturn(null);

        assertThatThrownBy(() -> resolver.resolve(Subject.USER, sampleMethod))
                .isInstanceOf(com.xd11cc.single.common.exception.ServiceException.class);
    }

    @Test
    @DisplayName("USER 维度：已登录用户")
    void resolve_user() {
        securityUtils.when(SecurityUtils::getUserId).thenReturn(88L);

        assertThat(resolver.resolve(Subject.USER, sampleMethod))
                .isEqualTo("user:88:t7");
    }

    @Test
    @DisplayName("METHOD 维度：类名.方法名")
    void resolve_method() {
        assertThat(resolver.resolve(Subject.METHOD, sampleMethod))
                .isEqualTo("method:Sample.login:t7");
    }

    @Test
    @DisplayName("请求内容维度：按参数名取值（简单参数）")
    void resolveArg_byParamName() throws Exception {
        Method m = Sample.class.getDeclaredMethod("sendSms", String.class);

        assertThat(resolver.resolveArg("phone", m, new Object[]{"13800000000"}))
                .isEqualTo("arg:13800000000:t7");
    }

    @Test
    @DisplayName("请求内容维度：按参数对象字段取值")
    void resolveArg_byFieldOfObject() throws Exception {
        Method m = Sample.class.getDeclaredMethod("loginVo", Sample.LoginVO.class);
        Sample.LoginVO vo = new Sample.LoginVO();
        vo.username = "bob";

        assertThat(resolver.resolveArg("username", m, new Object[]{vo}))
                .isEqualTo("arg:bob:t7");
    }

    @Test
    @DisplayName("请求内容维度：取值为空时返回 null，由调用方跳过该维度")
    void resolveArg_nullValue() throws Exception {
        Method m = Sample.class.getDeclaredMethod("loginVo", Sample.LoginVO.class);

        assertThat(resolver.resolveArg("username", m, new Object[]{new Sample.LoginVO()}))
                .isNull();
    }

    @Test
    @DisplayName("请求内容维度：字段不存在时报出可读错误")
    void resolveArg_fieldNotFound() throws Exception {
        Method m = Sample.class.getDeclaredMethod("loginVo", Sample.LoginVO.class);

        assertThatThrownBy(() -> resolver.resolveArg("userName", m, new Object[]{new Sample.LoginVO()}))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("找不到");
    }

    @Test
    @DisplayName("无租户上下文时不加后缀")
    void resolve_withoutTenant() {
        TenantContextHolder.clear();
        TenantContextHolder.setTenantAware(false);

        assertThat(resolver.resolve(Subject.METHOD, sampleMethod))
                .isEqualTo("method:Sample.login");
    }

    /**
     * 供反射取方法用
     */
    @SuppressWarnings("unused")
    private static class Sample {
        void login(String username) {
        }

        void loginVo(LoginVO vo) {
        }

        void sendSms(String phone) {
        }

        static class LoginVO {
            private String username;
        }
    }
}
