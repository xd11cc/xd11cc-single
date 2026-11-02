package com.xd11cc.single.common.ratelimit;

import com.xd11cc.single.common.context.TenantContextHolder;
import com.xd11cc.single.common.exception.ServiceException;
import com.xd11cc.single.enums.SystemErrorEnum;
import com.xd11cc.single.utils.IpUtils;
import com.xd11cc.single.utils.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 限流身份派生。
 * <p>
 * 有两条正交的身份来源：
 * </p>
 * <ul>
 *     <li>{@link Subject} —— 谁在调用：来源 IP、登录用户、接口本身</li>
 *     <li>{@code argField} —— 调用的是谁的数据：登录用户名、手机号这类请求内容</li>
 * </ul>
 * <p>
 * 所有 key 都带租户后缀（多租户 SaaS 下，不同租户的同名身份不应共享配额）。
 * </p>
 *
 * @author xd11cc
 */
@Slf4j
@Component
public class RateLimitKeyResolver {

    /**
     * 参数取值器缓存，避免每次请求都做反射查找。key = 方法签名 + argField
     */
    private final Map<String, ArgTarget> targetCache = new ConcurrentHashMap<>();

    /**
     * 按「谁在调用」派生标识。
     */
    public String resolve(Subject subject, Method method) {
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
            default:
                throw new IllegalStateException("未知限流维度: " + subject);
        }
        return identity + tenantSuffix();
    }

    /**
     * 按「请求内容」派生标识。
     *
     * @param argField 参数名或参数对象的字段名
     * @return 标识；取到的值为空时返回 null，由调用方跳过该维度
     */
    public String resolveArg(String argField, Method method, Object[] args) {
        ArgTarget target = targetCache.computeIfAbsent(
                method.toGenericString() + "#" + argField,
                k -> ArgTarget.locate(method, argField));
        Object raw = target.read(args);
        if (raw == null || String.valueOf(raw).trim().isEmpty()) {
            log.warn("[ratelimit] 请求内容维度的值为空，跳过该维度 method={}, argField={}",
                    method.getName(), argField);
            return null;
        }
        return "arg:" + String.valueOf(raw).trim() + tenantSuffix();
    }

    private String tenantSuffix() {
        if (!TenantContextHolder.isTenantAware()) {
            return "";
        }
        Long tenantId = TenantContextHolder.getTenantId();
        return tenantId == null ? "" : ":t" + tenantId;
    }

    /**
     * 参数取值器：记录参数下标；{@code field} 非空表示要取该参数对象的字段。
     * <p>
     * 解析规则（启动期校验复用同一套逻辑，保证校验与实际取值一致）：
     * 优先按参数名匹配，其次在参数对象里找同名字段。
     * </p>
     */
    static final class ArgTarget {

        private final int index;
        private final Field field;

        private ArgTarget(int index, Field field) {
            this.index = index;
            this.field = field;
        }

        static ArgTarget locate(Method method, String argField) {
            Parameter[] parameters = method.getParameters();
            for (int i = 0; i < parameters.length; i++) {
                if (argField.equals(parameters[i].getName())) {
                    return new ArgTarget(i, null);
                }
            }

            Field matched = null;
            int matchedIndex = -1;
            Class<?>[] types = method.getParameterTypes();
            for (int i = 0; i < types.length; i++) {
                Field candidate = findField(types[i], argField);
                if (candidate != null) {
                    if (matched != null) {
                        throw new IllegalStateException(
                                "有多个参数都带字段 " + argField + "，无法确定取哪一个");
                    }
                    candidate.setAccessible(true);
                    matched = candidate;
                    matchedIndex = i;
                }
            }
            if (matched == null) {
                throw new IllegalStateException(
                        "参数中找不到名为 " + argField + " 的参数或字段，请检查拼写");
            }
            return new ArgTarget(matchedIndex, matched);
        }

        Object read(Object[] args) {
            if (args == null || index >= args.length) {
                return null;
            }
            if (field == null) {
                return args[index];
            }
            try {
                return field.get(args[index]);
            } catch (IllegalAccessException e) {
                throw new IllegalStateException("读取限流字段失败: " + field.getName(), e);
            }
        }

        static Field findField(Class<?> type, String name) {
            Class<?> current = type;
            while (current != null && current != Object.class) {
                try {
                    return current.getDeclaredField(name);
                } catch (NoSuchFieldException ignored) {
                    current = current.getSuperclass();
                }
            }
            return null;
        }
    }
}
