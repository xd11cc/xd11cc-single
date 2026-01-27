package com.xd11cc.single.utils;

import org.springframework.util.AntPathMatcher;

import java.util.Collection;
import java.util.List;

/**
 * @author xd11cc
 * @date 2026-01-26 13:53:27
 * @description
 */
public class StringUtils extends org.apache.commons.lang3.StringUtils {

    public static final String NULLSTR = "";

    /**
     * 判断一个Collection是否为空，包含List、Set、Queue
     * @param coll Collection
     * @return true: 为空 false: 非空
     */
    public static boolean isEmpty(Collection<?> coll){
        return isNull(coll) || coll.isEmpty();
    }

    /**
     * 判断一个Collection是非空，包含List、Set、Queue
     * @param coll Collection
     * @return true: 非空 false: 为空
     */
    public static boolean isNotEmpty(Collection<?> coll){
        return !isEmpty(coll);
    }

    /**
     * 判断一个字符串是否为空串
     * @param str String
     * @return true: 为空 false: 非空
     */
    public static boolean isEmpty(String str) {
        return isNull(str) || NULLSTR.equals(str.trim());
    }

    /**
     * 判断一个字符串是否为非空串
     * @param str String
     * @return true: 非空 false: 为空
     */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    /**
     * 判断一个对象是否为空
     * @param obj Object
     * @return true: 为空 false: 非空
     */
    public static boolean isNull(Object obj) {
        return obj == null;
    }

    /**
     * 判断一个对象是否非空
     * @param obj Object
     * @return true: 非空 false: 为空
     */
    public static boolean isNotNull(Object obj) {
        return !isNull(obj);
    }

    /**
     * 查找指定字符串是否匹配指定字符串列表中的任意一个字符串
     * @param str 指定字符串
     * @param strs 需要检查的字符串数组
     * @return 是否匹配
     */
    public static boolean matches(String str, List<String> strs){
        if (isEmpty(str) || isEmpty(strs)){
            return false;
        }
        for (String pattern : strs) {
            if(isMatch(pattern, str)){
                return true;
            }
        }
        return false;
    }

    /**
     * 判断url是否与规则配置
     * ? 表示单个字符
     * * 表示一层路径内的任意字符串，不可跨层级
     * ** 表示任意层路径
     * @param pattern 匹配规则
     * @param url 需要匹配的url
     * @return
     */
    public static boolean isMatch(String pattern, String url) {
        AntPathMatcher matcher = new AntPathMatcher();
        return matcher.match(pattern, url);
    }
}
