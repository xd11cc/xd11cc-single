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

    NO("01", "否"),
    YES("02", "是"),
    ;

    private String code;

    private String info;
}
