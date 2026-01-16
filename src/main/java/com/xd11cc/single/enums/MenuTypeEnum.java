package com.xd11cc.single.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author xd11cc
 * @date 2026-01-15 09:45:00
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum MenuTypeEnum {

    DIR("M", "目录"),
    MENU("C", "菜单"),
    BUTTON("B", "按钮")

    ;

    private String code;

    private String info;
}
