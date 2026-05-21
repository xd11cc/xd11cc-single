package com.xd11cc.single.utils;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

/**
 * @author xd11cc
 * @date 2023/4/25  16:52
 *
 * sql字段转Java
 */
public class ColumnUtil {

    private static final char SEPARATOR = '_';

    private static final PropertiesConfiguration CONFIG;

    static {
        try {
            CONFIG = new PropertiesConfiguration("generator.properties");
        } catch (ConfigurationException e) {
            throw new RuntimeException("加载generator.properties失败", e);
        }
    }

    /**
     * MySQL数据类型转为Java类型
     * @param type
     * @return
     */
    public static String cloToJava(String type){
        return CONFIG.getString(type, "String");
    }

    /**
     * 驼峰命名法工具  xxx_xxx  ----> xxxXxx
     * @param s
     * @return
     */
    public static String toCamelCase(String s){
        if (s == null){
            return null;
        }
        s = s.toLowerCase();
        StringBuilder sb = new StringBuilder(s.length());
        boolean upperCase = false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == SEPARATOR){
                upperCase = true;
            }else if (upperCase){
                sb.append(Character.toUpperCase(c));
                upperCase = false;
            }else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * 驼峰命名法工具  xxx_xxx  ----> XxxXxx
     * @param s
     * @return
     */
    public static String toCapitalizeCamlCase(String s){
        if (s == null){
            return null;
        }
        s = toCamelCase(s);
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
}
