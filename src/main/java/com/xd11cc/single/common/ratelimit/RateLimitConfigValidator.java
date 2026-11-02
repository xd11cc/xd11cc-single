package com.xd11cc.single.common.ratelimit;

import com.xd11cc.single.common.annotation.RateLimit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * {@code @RateLimit} 的启动期配置校验。
 * <p>
 * 注解里的 {@code argField} 是字符串，写错的话运行期要么找不到字段、要么限错了对象，
 * 而这类错误往往要等真实流量打到那个接口才暴露。这里把所有注解在**应用启动时**扫一遍，
 * 有任何配置问题直接启动失败，并一次性列出全部问题。
 * </p>
 * <p>
 * 校验规则：
 * </p>
 * <ul>
 *     <li>{@code permits} / {@code windowSeconds} 必须大于 0</li>
 *     <li>{@code subject} 与 {@code argField} 至少配置一个，否则等于没有限流</li>
 *     <li>{@code argField} 必须能解析：优先匹配参数名，其次在参数对象里找同名字段；
 *         多个参数都带同名字段视为歧义，直接报错</li>
 *     <li>解析逻辑与运行期共用同一实现（{@code RateLimitKeyResolver.ArgTarget#locate}），
 *         保证「校验通过」与「实际取值」行为一致</li>
 * </ul>
 *
 * @author xd11cc
 */
@Slf4j
@Component
public class RateLimitConfigValidator implements SmartInitializingSingleton {

    private final ApplicationContext applicationContext;

    public RateLimitConfigValidator(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterSingletonsInstantiated() {
        List<String> problems = new ArrayList<>();
        Set<String> visited = new LinkedHashSet<>();

        for (String beanName : applicationContext.getBeanDefinitionNames()) {
            Class<?> type;
            try {
                type = applicationContext.getType(beanName);
            } catch (Exception e) {
                // 某些 FactoryBean 的类型在这里取不到，跳过即可
                continue;
            }
            if (type == null) {
                continue;
            }
            Class<?> userClass = ClassUtils.getUserClass(type);
            if (!visited.add(userClass.getName())) {
                continue;
            }
            for (Method method : allDeclaredMethods(userClass)) {
                RateLimit rateLimit = AnnotationUtils.findAnnotation(method, RateLimit.class);
                if (rateLimit != null) {
                    validate(userClass, method, rateLimit, problems);
                }
            }
        }

        if (!problems.isEmpty()) {
            String report = String.join(System.lineSeparator(), problems);
            log.error("[ratelimit] @RateLimit 配置有误，应用启动中止：{}{}", System.lineSeparator(), report);
            throw new IllegalStateException("@RateLimit 配置有误，共 " + problems.size() + " 处："
                    + System.lineSeparator() + report);
        }
    }

    private void validate(Class<?> owner, Method method, RateLimit rateLimit, List<String> problems) {
        String where = owner.getSimpleName() + "." + method.getName();

        if (rateLimit.permits() <= 0) {
            problems.add(where + "：permits 必须大于 0");
        }
        if (rateLimit.windowSeconds() <= 0) {
            problems.add(where + "：windowSeconds 必须大于 0");
        }

        String argField = rateLimit.argField() == null ? "" : rateLimit.argField().trim();
        boolean hasSubject = rateLimit.subject() != null && rateLimit.subject().length > 0;

        if (!hasSubject && argField.isEmpty()) {
            problems.add(where + "：至少需要一个限流维度（subject 或 argField）");
            return;
        }

        if (argField.isEmpty()) {
            return;
        }

        // 复用运行期的解析逻辑，保证"校验通过"与"运行期取值"一致
        try {
            RateLimitKeyResolver.ArgTarget.locate(method, argField);
        } catch (IllegalStateException e) {
            problems.add(where + "：" + e.getMessage());
        }
    }

    /**
     * 收集类及其父类上声明的方法（限流可能配在抽象基类上）
     */
    private static List<Method> allDeclaredMethods(Class<?> type) {
        List<Method> methods = new ArrayList<>();
        Class<?> current = type;
        while (current != null && current != Object.class) {
            methods.addAll(Arrays.asList(current.getDeclaredMethods()));
            current = current.getSuperclass();
        }
        return methods;
    }
}
