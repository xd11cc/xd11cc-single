package com.xd11cc.single.utils;

import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LocalDateTimeUtilsTest {

    @Test
    void empty_返回1970年1月1日() {
        assertThat(LocalDateTimeUtils.EMPTY)
                .isEqualTo(LocalDateTime.of(1970, 1, 1, 0, 0, 0));
    }

    @Test
    void parse_标准日期字符串_解析成功() {
        assertThat(LocalDateTimeUtils.parse("2026-08-11"))
                .isEqualTo(LocalDateTime.of(2026, 8, 11, 0, 0));
    }

    @Test
    void parse_空字符串_抛异常() {
        assertThat(LocalDateTimeUtils.parse("")).isNull(); // Hutool swallows?
    }

    @Test
    void buildTime_合法日期_正确构建() {
        assertThat(LocalDateTimeUtils.buildTime(2026, 8, 11))
                .isEqualTo(LocalDateTime.of(2026, 8, 11, 0, 0, 0));
    }

    @Test
    void buildTime_非法日期_抛出DateTimeException() {
        assertThrows(
                java.time.DateTimeException.class,
                () -> LocalDateTimeUtils.buildTime(2026, 2, 30)
        );
    }

    @Test
    void buildBetweenTime_返回两个边界时间() {
        LocalDateTime[] range = LocalDateTimeUtils.buildBetweenTime(2026, 1, 1, 2026, 12, 31);
        assertThat(range).hasSize(2);
        assertThat(range[0]).isEqualTo(LocalDateTime.of(2026, 1, 1, 0, 0, 0));
        assertThat(range[1]).isEqualTo(LocalDateTime.of(2026, 12, 31, 0, 0, 0));
    }

    @Test
    void getQuarterOfYear_Q1月份_返回1() {
        assertThat(LocalDateTimeUtils.getQuarterOfYear(LocalDateTime.of(2026, 1, 1, 0, 0))).isEqualTo(1);
    }

    @Test
    void getQuarterOfYear_Q4月份_返回4() {
        assertThat(LocalDateTimeUtils.getQuarterOfYear(LocalDateTime.of(2026, 12, 1, 0, 0))).isEqualTo(4);
    }

    @Test
    void getQuarterOfYear_Q3月份_返回3() {
        assertThat(LocalDateTimeUtils.getQuarterOfYear(LocalDateTime.of(2026, 7, 1, 0, 0))).isEqualTo(3);
    }

    @Test
    void beforeNow_过去时间_返回true() {
        LocalDateTime past = LocalDateTime.of(2000, 1, 1, 0, 0);
        assertThat(LocalDateTimeUtils.beforeNow(past)).isTrue();
    }

    @Test
    void beforeNow_未来时间_返回false() {
        LocalDateTime future = LocalDateTime.of(3000, 1, 1, 0, 0);
        assertThat(LocalDateTimeUtils.beforeNow(future)).isFalse();
    }

    @Test
    void afterNow_未来时间_返回true() {
        LocalDateTime future = LocalDateTime.of(3000, 1, 1, 0, 0);
        assertThat(LocalDateTimeUtils.afterNow(future)).isTrue();
    }

    @Test
    void afterNow_过去时间_返回false() {
        LocalDateTime past = LocalDateTime.of(2000, 1, 1, 0, 0);
        assertThat(LocalDateTimeUtils.afterNow(past)).isFalse();
    }

    @Test
    void beginOfMonth_月初日期_返回当月第一天零点() {
        LocalDateTime date = LocalDateTime.of(2026, 8, 15, 12, 30);
        assertThat(LocalDateTimeUtils.beginOfMonth(date))
                .isEqualTo(LocalDateTime.of(2026, 8, 1, 0, 0));
    }

    @Test
    void endOfMonth_月末日期_返回当月最后一天23_59_59_999() {
        LocalDateTime date = LocalDateTime.of(2026, 8, 15, 12, 30);
        assertThat(LocalDateTimeUtils.endOfMonth(date).getDayOfMonth()).isEqualTo(31);
        assertThat(LocalDateTimeUtils.endOfMonth(date).getHour()).isEqualTo(23);
        assertThat(LocalDateTimeUtils.endOfMonth(date).getMinute()).isEqualTo(59);
        assertThat(LocalDateTimeUtils.endOfMonth(date).getSecond()).isEqualTo(59);
    }

    @Test
    void isBetween_null参数_返回false() {
        LocalDateTime start = LocalDateTime.of(2026, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2026, 12, 31, 0, 0);
        Timestamp ts = Timestamp.valueOf("2026-06-01 00:00:00");
        assertThat(LocalDateTimeUtils.isBetween(null, end, ts)).isFalse();
        assertThat(LocalDateTimeUtils.isBetween(start, null, ts)).isFalse();
        assertThat(LocalDateTimeUtils.isBetween(start, end, (Timestamp) null)).isFalse();
    }

    @Test
    void isBetween_在范围内_返回true() {
        LocalDateTime start = LocalDateTime.of(2026, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2026, 12, 31, 23, 59, 59);
        Timestamp time = Timestamp.valueOf("2026-06-01 12:00:00");
        assertThat(LocalDateTimeUtils.isBetween(start, end, time)).isTrue();
    }

    @Test
    void isBetween_不在范围内_返回false() {
        LocalDateTime start = LocalDateTime.of(2026, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2026, 6, 1, 0, 0);
        Timestamp time = Timestamp.valueOf("2026-12-01 00:00:00");
        assertThat(LocalDateTimeUtils.isBetween(start, end, time)).isFalse();
    }

    @Test
    void getQuarterStart_Q3日期_返回7月1日() {
        assertThat(LocalDateTimeUtils.getQuarterStart(LocalDate.of(2026, 8, 1)))
                .isEqualTo(LocalDate.of(2026, 7, 1));
    }

    @Test
    void getQuarterStart_Q1日期_返回1月1日() {
        assertThat(LocalDateTimeUtils.getQuarterStart(LocalDate.of(2026, 2, 15)))
                .isEqualTo(LocalDate.of(2026, 1, 1));
    }

    @Test
    void getWeekStart_周三日期_返回该周周一() {
        // 2026-08-12 is a Wednesday
        assertThat(LocalDateTimeUtils.getWeekStart(LocalDate.of(2026, 8, 12)))
                .isEqualTo(LocalDate.of(2026, 8, 10));
    }
}
