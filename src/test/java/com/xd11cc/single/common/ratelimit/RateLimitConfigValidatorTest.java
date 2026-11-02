package com.xd11cc.single.common.ratelimit;

import com.xd11cc.single.common.annotation.RateLimit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.context.ApplicationContext;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doReturn;

/**
 * {@link RateLimitConfigValidator} 的测试。
 * <p>
 * 校验器的价值在于把"字符串字段名写错"从运行期失效变成启动期失败，
 * 所以这里逐条覆盖各种配置错误。
 * </p>
 *
 * @author xd11cc
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RateLimitConfigValidatorTest {

    @Mock
    private ApplicationContext applicationContext;

    private void scanning(Class<?>... types) {
        String[] names = new String[types.length];
        for (int i = 0; i < types.length; i++) {
            names[i] = "bean" + i;
            doReturn(types[i]).when(applicationContext).getType("bean" + i);
        }
        doReturn(names).when(applicationContext).getBeanDefinitionNames();
    }

    private RateLimitConfigValidator validator() {
        return new RateLimitConfigValidator(applicationContext);
    }

    @Test
    @DisplayName("合法配置：IP 维度 + 请求内容字段")
    void valid_ipAndArgField() {
        scanning(ValidSample.class);

        assertThatCode(() -> validator().afterSingletonsInstantiated()).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("合法配置：只按请求内容维度（参数名匹配）")
    void valid_argFieldOnly_byParamName() {
        scanning(SimpleParamSample.class);

        assertThatCode(() -> validator().afterSingletonsInstantiated()).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("permits <= 0：启动失败")
    void invalid_permits() {
        scanning(InvalidPermitsSample.class);

        assertThatThrownBy(() -> validator().afterSingletonsInstantiated())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("permits");
    }

    @Test
    @DisplayName("既没有 subject 也没有 argField：启动失败")
    void invalid_noDimension() {
        scanning(NoDimensionSample.class);

        assertThatThrownBy(() -> validator().afterSingletonsInstantiated())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("至少需要一个限流维度");
    }

    @Test
    @DisplayName("argField 拼错：启动失败并提示找不到")
    void invalid_argField_typo() {
        scanning(WrongFieldSample.class);

        assertThatThrownBy(() -> validator().afterSingletonsInstantiated())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("找不到");
    }

    @Test
    @DisplayName("多个参数都带同名字段：启动失败并提示歧义")
    void invalid_argField_ambiguous() {
        scanning(AmbiguousSample.class);

        assertThatThrownBy(() -> validator().afterSingletonsInstantiated())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("无法确定");
    }

    // ==================== 夹具 ====================

    static class LoginVO {
        private String username;
    }

    static class ValidSample {
        @RateLimit(permits = 5, windowSeconds = 60,
                subject = {Subject.IP}, argField = "username")
        void login(LoginVO vo) {
        }
    }

    static class SimpleParamSample {
        @RateLimit(permits = 5, windowSeconds = 60, subject = {}, argField = "phone")
        void send(String phone) {
        }
    }

    static class InvalidPermitsSample {
        @RateLimit(permits = 0, argField = "username")
        void bad(LoginVO vo) {
        }
    }

    static class NoDimensionSample {
        @RateLimit(permits = 5, subject = {})
        void bad(String phone) {
        }
    }

    static class WrongFieldSample {
        @RateLimit(permits = 5, argField = "userName")
        void bad(LoginVO vo) {
        }
    }

    static class AmbiguousSample {
        @RateLimit(permits = 5, argField = "username")
        void bad(LoginVO first, LoginVO second) {
        }
    }
}
