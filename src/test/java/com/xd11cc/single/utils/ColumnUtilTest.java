package com.xd11cc.single.utils;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ColumnUtilTest {

    @Test
    void cloToJava_常见int类型_返回Integer() {
        assertThat(ColumnUtil.cloToJava("int")).isEqualTo("Integer");
    }

    @Test
    void cloToJava_bigint类型_返回Long() {
        assertThat(ColumnUtil.cloToJava("bigint")).isEqualTo("Long");
    }

    @Test
    void cloToJava_varchar类型_返回String() {
        assertThat(ColumnUtil.cloToJava("varchar")).isEqualTo("String");
    }

    @Test
    void cloToJava_datetime类型_返回Date() {
        assertThat(ColumnUtil.cloToJava("datetime")).isEqualTo("Date");
    }

    @Test
    void cloToJava_未知类型_返回默认String() {
        assertThat(ColumnUtil.cloToJava("json")).isEqualTo("String");
        assertThat(ColumnUtil.cloToJava("unknown_type")).isEqualTo("String");
    }

    @Test
    void toCamelCase_下划线转驼峰_正确转换() {
        assertThat(ColumnUtil.toCamelCase("user_name")).isEqualTo("userName");
        assertThat(ColumnUtil.toCamelCase("create_time")).isEqualTo("createTime");
    }

    @Test
    void toCamelCase_null输入_返回null() {
        assertThat(ColumnUtil.toCamelCase(null)).isNull();
    }

    @Test
    void toCamelCase_无下划线_原样返回() {
        assertThat(ColumnUtil.toCamelCase("username")).isEqualTo("username");
    }

    @Test
    void toCapitalizeCamlCase_首字母大写驼峰_正确转换() {
        assertThat(ColumnUtil.toCapitalizeCamlCase("user_name")).isEqualTo("UserName");
        assertThat(ColumnUtil.toCapitalizeCamlCase("create_time")).isEqualTo("CreateTime");
    }

    @Test
    void toCapitalizeCamlCase_null输入_返回null() {
        assertThat(ColumnUtil.toCapitalizeCamlCase(null)).isNull();
    }
}
