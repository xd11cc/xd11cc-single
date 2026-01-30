package com.xd11cc.single.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Author: xd11cc
 * @Date: 2025/6/23 17:02
 **/
@Getter
@AllArgsConstructor
public enum SystemStatusEnum {

    NORMAL(0, "正常"),
    FORBIDDEN(1, "停用"),
    ;

    @EnumValue
    @JsonValue
    private final Integer code;

    private final String info;
}
