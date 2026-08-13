package com.xd11cc.single.utils;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ObjectUtilsTest {

    @Test
    void equalsAny_varargs_匹配到元素_返回True() {
        assertThat(ObjectUtils.equalsAny("alpha", "alpha", "beta", "gamma")).isTrue();
        assertThat(ObjectUtils.equalsAny("beta", "alpha", "beta", "gamma")).isTrue();
        assertThat(ObjectUtils.equalsAny("gamma", "alpha", "beta", "gamma")).isTrue();
    }

    @Test
    void equalsAny_varargs_未匹配到元素_返回False() {
        assertThat(ObjectUtils.equalsAny("omega", "alpha", "beta", "gamma")).isFalse();
    }

    @Test
    void equalsAny_list_匹配到元素_返回True() {
        List<String> list = Arrays.asList("alpha", "beta", "gamma");
        assertThat(ObjectUtils.equalsAny("beta", list)).isTrue();
    }

    @Test
    void equalsAny_list_未匹配到元素_返回False() {
        List<String> list = Arrays.asList("alpha", "beta", "gamma");
        assertThat(ObjectUtils.equalsAny("omega", list)).isFalse();
    }

    // ==================== 边界情况 ====================

    @Test
    void equalsAny_varargs_空数组_返回false() {
        assertThat(ObjectUtils.equalsAny("x")).isFalse();
    }

    @Test
    void equalsAny_varargs_单个元素匹配_返回true() {
        assertThat(ObjectUtils.equalsAny("only", "only")).isTrue();
    }

    @Test
    void equalsAny_list_空list_返回false() {
        assertThat(ObjectUtils.equalsAny("x", Collections.<String>emptyList())).isFalse();
    }

    @Test
    void equalsAny_varargs_包含null_匹配null返回true() {
        assertThat(ObjectUtils.equalsAny(null, "a", null, "c")).isTrue();
    }

    @Test
    void equalsAny_varargs_包含null_不匹配返回false() {
        assertThat(ObjectUtils.equalsAny(null, "a", "b", "c")).isFalse();
    }

    @Test
    void equalsAny_整型varargs() {
        assertThat(ObjectUtils.equalsAny(2, 1, 2, 3)).isTrue();
        assertThat(ObjectUtils.equalsAny(5, 1, 2, 3)).isFalse();
    }

    @Test
    void equalsAny_整型list() {
        List<Integer> list = Arrays.asList(1, 2, 3);
        assertThat(ObjectUtils.equalsAny(2, list)).isTrue();
        assertThat(ObjectUtils.equalsAny(5, list)).isFalse();
    }
}

