package com.xd11cc.single.utils;

import java.util.Arrays;
import java.util.List;

/**
 * @author xd11cc
 * @date 2026-06-05 15:15:31
 * @description Object 工具类
 */
public class ObjectUtils {

    @SafeVarargs
    public static <T> boolean equalsAny(T obj, T ... array){
        return Arrays.asList(array).contains(obj);
    }

    public static <T> boolean equalsAny(T obj, List<T> list){
        return list.contains(obj);
    }
}
