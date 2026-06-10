package com.xd11cc.single.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

/**
 * @author xd11cc
 * @date 2026-06-05 16:17:37
 * @description
 */
@Getter
@AllArgsConstructor
public enum PayRefundStatusEnum {

    WAITING(0, "未退款"),
    SUCCESS(10, "退款成功"),
    FAILURE(20, "退款失败"),
    ;

    private final Integer status;

    private final String name;

    public static boolean isSuccess(Integer status){
        return Objects.equals(status, SUCCESS.status);
    }

    public static boolean isFailure(Integer status){
        return Objects.equals(status, FAILURE.status);
    }
}
