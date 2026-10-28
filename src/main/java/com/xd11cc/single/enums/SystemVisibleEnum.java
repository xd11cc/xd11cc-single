package com.xd11cc.single.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 菜单显示/隐藏状态。
 * <p>
 * 对应 {@code system_menu.visible}（char(2)），展示文案由字典
 * {@code system_visible} 提供，本枚举只承载<b>编译期取值契约</b>。
 * </p>
 *
 * @Author: xd11cc
 * @Date: 2025/6/23 17:02
 **/
@Getter
@AllArgsConstructor
public enum SystemVisibleEnum {

    SHOW("0"),     // 显示
    HIDDEN("1"),   // 隐藏
    ;

    private final String code;

}
