package com.xd11cc.single.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Author: xd11cc
 * @Date: 2025/6/23 17:02
 **/
@Getter
@AllArgsConstructor
public enum SystemVisibleEnum {

    SHOW("0", "显示"),
    HIDDEN("1", "隐藏"),
    ;

    private final String code;

    private final String info;
}
