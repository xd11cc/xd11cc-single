package com.xd11cc.single.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @Author: xd11cc
 * @Date: 2025/6/21 22:46
 **/
@NoArgsConstructor
@AllArgsConstructor
@Getter
public enum LoginWayEnum {

    PC(0, "PC端"),
    APP(1, "移动端"),
    ;

    private int code;

    private String info;
}
