package com.xd11cc.single.utils;

import org.springframework.util.AntPathMatcher;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.List;

/**
 * @author xd11cc
 * @date 2026-01-26 13:53:27
 */
public class StringUtils extends org.apache.commons.lang3.StringUtils {

    private static final AntPathMatcher ANT_PATH_MATCHER = new AntPathMatcher();

    /**
     * @deprecated 使用 {@link CollectionUtils#isEmpty(Collection)}
     */
    @Deprecated
    public static boolean isEmpty(Collection<?> coll) {
        return CollectionUtils.isEmpty(coll);
    }

    /**
     * @deprecated 使用 {@code !CollectionUtils.isEmpty(coll)}
     */
    @Deprecated
    public static boolean isNotEmpty(Collection<?> coll) {
        return !CollectionUtils.isEmpty(coll);
    }

    /**
     * 查找指定字符串是否匹配指定字符串列表中的任意一个字符串
     * @param str 指定字符串
     * @param strs 需要检查的字符串数组
     * @return 是否匹配
     */
    public static boolean matches(String str, List<String> strs) {
        if (isBlank(str) || CollectionUtils.isEmpty(strs)) {
            return false;
        }
        for (String pattern : strs) {
            if (isMatch(pattern, str)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断url是否与规则匹配
     * ? 表示单个字符
     * * 表示一层路径内的任意字符串，不可跨层级
     * ** 表示任意层路径
     */
    public static boolean isMatch(String pattern, String url) {
        return ANT_PATH_MATCHER.match(pattern, url);
    }
}
