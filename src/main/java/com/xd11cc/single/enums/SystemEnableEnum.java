package com.xd11cc.single.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @Author: xd11cc
 * @Date: 2025/6/23 17:02
 **/
@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum SystemEnableEnum {

    YES("01", "是"),
    NO("02", "否"),
    ;

    private String code;

    private String info;
}
