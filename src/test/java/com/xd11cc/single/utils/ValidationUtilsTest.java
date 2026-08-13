package com.xd11cc.single.utils;

import org.junit.jupiter.api.Test;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ValidationUtilsTest {

    static class ValidBean {
        @NotBlank
        String name;

        @NotNull
        @Size(min = 3, max = 20)
        String code;

        ValidBean(String name, String code) {
            this.name = name;
            this.code = code;
        }
    }

    static class InvalidBean {
        @NotBlank
        String name = "";
    }

    @Test
    void isMobile_国内手机号_返回True() {
        assertThat(ValidationUtils.isMobile("13800138000")).isTrue();
        assertThat(ValidationUtils.isMobile("15912345678")).isTrue();
        assertThat(ValidationUtils.isMobile("+8613800138000")).isTrue();
    }

    @Test
    void isMobile_非法手机号_返回False() {
        assertThat(ValidationUtils.isMobile("1234567")).isFalse();
        assertThat(ValidationUtils.isMobile("abcdefghijk")).isFalse();
        assertThat(ValidationUtils.isMobile(null)).isFalse();
        assertThat(ValidationUtils.isMobile("")).isFalse();
        assertThat(ValidationUtils.isMobile("   ")).isFalse();
    }

    @Test
    void isURL_合法URL_返回True() {
        assertThat(ValidationUtils.isURL("http://example.com")).isTrue();
        assertThat(ValidationUtils.isURL("https://example.com/path?q=1")).isTrue();
        assertThat(ValidationUtils.isURL("ftp://files.example.com")).isTrue();
    }

    @Test
    void isURL_非法URL_返回False() {
        assertThat(ValidationUtils.isURL("example.com")).isFalse();
        assertThat(ValidationUtils.isURL("htp://bad.com")).isFalse();
        assertThat(ValidationUtils.isURL(null)).isFalse();
        assertThat(ValidationUtils.isURL("")).isFalse();
    }

    @Test
    void isXmlNCName_合法XML名称_返回True() {
        assertThat(ValidationUtils.isXmlNCName("valid_name")).isTrue();
        assertThat(ValidationUtils.isXmlNCName("_underscore")).isTrue();
        assertThat(ValidationUtils.isXmlNCName("a")).isTrue();
        assertThat(ValidationUtils.isXmlNCName("valid-name.1a$")).isTrue();
    }

    @Test
    void isXmlNCName_非法XML名称_返回False() {
        assertThat(ValidationUtils.isXmlNCName("123abc")).isFalse();
        assertThat(ValidationUtils.isXmlNCName("has space")).isFalse();
        assertThat(ValidationUtils.isXmlNCName("")).isFalse();
        assertThat(ValidationUtils.isXmlNCName(null)).isFalse();
    }

    @Test
    void validate_合法对象_通过() {
        ValidBean bean = new ValidBean("test", "abc");
        // 合法对象不抛异常
        ValidationUtils.validate(bean);
    }

    @Test
    void validate_非法对象_抛ConstraintViolationException() {
        InvalidBean bean = new InvalidBean();
        assertThrows(javax.validation.ConstraintViolationException.class,
                () -> ValidationUtils.validate(bean));
    }
}
