package com.xd11cc.single.utils;

import org.apache.commons.configuration.Configuration;
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

    /**
     * 获取配置信息
     * @return
     */
    public static PropertiesConfiguration getConfig(){
        try {
            return new PropertiesConfiguration("generator.properties");
        } catch (ConfigurationException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * MySQL数据类型转为Java类型
     * @param type
     * @return
     */
    public static String cloToJava(String type){
        Configuration config = getConfig();
        assert config != null;
        return config.getString(type, null);
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
