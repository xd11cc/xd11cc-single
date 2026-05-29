package com.xd11cc.single.utils;

import cn.hutool.core.util.StrUtil;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

/**
 * @author xd11cc
 * @date 2023/4/25  16:52
 */
public class ColumnUtil {

    private static final PropertiesConfiguration CONFIG;

    static {
        try {
            CONFIG = new PropertiesConfiguration("generator.properties");
        } catch (ConfigurationException e) {
            throw new RuntimeException("加载generator.properties失败", e);
        }
    }

    public static String cloToJava(String type) {
        return CONFIG.getString(type, "String");
    }

    public static String toCamelCase(String s) {
        if (s == null) {
            return null;
        }
        return StrUtil.toCamelCase(s);
    }

    public static String toCapitalizeCamlCase(String s) {
        if (s == null) {
            return null;
        }
        return StrUtil.upperFirst(StrUtil.toCamelCase(s));
    }
}
