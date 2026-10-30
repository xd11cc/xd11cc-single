package com.xd11cc.single.common.ratelimit;

import com.xd11cc.single.common.context.TenantContextHolder;
import com.xd11cc.single.common.exception.ServiceException;
import com.xd11cc.single.enums.SystemErrorEnum;
import com.xd11cc.single.utils.IpUtils;
import com.xd11cc.single.utils.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * 限流身份派生：把「维度」翻译成一个稳定的业务标识。
 * <p>
 * 从切面里拆出来单独一个模块，是为了让切面只做"策略"（超限怎么办），
 * 而"谁是调用方"这件事有独立的接缝，可以单独测试。
 * </p>
 * <p>
 * 所有 key 都带租户后缀（多租户 SaaS 下，不同租户的同名身份不应共享配额）。
 * </p>
 *
 * @author xd11cc
 */
@Slf4j
@Component
public class RateLimitKeyResolver {

    private static final ExpressionParser PARSER = new SpelExpressionParser();

    /**
     * 派生一个维度的业务标识。
     *
     * @param subject    维度
     * @param expression EXPRESSION 维度使用的 SpEL
     * @param method     被限流的方法
     * @param args       方法实参
     */
    public String resolve(Subject subject, String expression, Method method, Object[] args) {
        String identity;
        switch (subject) {
            case IP:
                identity = "ip:" + IpUtils.getIpAddr();
                break;
            case USER:
                Long userId = SecurityUtils.getUserId();
                if (userId == null) {
                    throw new ServiceException(SystemErrorEnum.UNAUTHORIZED);
                }
                identity = "user:" + userId;
                break;
            case METHOD:
                identity = "method:" + method.getDeclaringClass().getSimpleName() + "." + method.getName();
                break;
            case EXPRESSION:
                identity = "expr:" + evaluate(expression, method, args);
                break;
            default:
                throw new IllegalStateException("未知限流维度: " + subject);
        }
        return identity + tenantSuffix();
    }

    /**
     * SpEL 取值，例如 {@code #p0.username} 取第一个参数的 username 属性
     */
    private String evaluate(String expression, Method method, Object[] args) {
        if (expression == null || expression.trim().isEmpty()) {
            throw new IllegalStateException("subject 含 EXPRESSION 时必须提供 expression");
        }
        EvaluationContext context = new StandardEvaluationContext();
        for (int i = 0; i < args.length; i++) {
            context.setVariable("p" + i, args[i]);
        }
        Expression parsed = PARSER.parseExpression(expression);
        Object value = parsed.getValue(context);
        if (value == null) {
            throw new IllegalStateException("限流表达式取值失败: " + expression);
        }
        return String.valueOf(value);
    }

    private String tenantSuffix() {
        if (!TenantContextHolder.isTenantAware()) {
            return "";
        }
        Long tenantId = TenantContextHolder.getTenantId();
        return tenantId == null ? "" : ":t" + tenantId;
    }
}
