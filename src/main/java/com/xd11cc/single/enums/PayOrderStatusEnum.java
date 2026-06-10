package com.xd11cc.single.enums;

import com.xd11cc.single.config.ArrayValuable;
import com.xd11cc.single.utils.ObjectUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

/**
 * @author xd11cc
 * @date 2026-06-05 14:58:05
 * @description 支付订单状态的枚举
 */
@Getter
@AllArgsConstructor
public enum PayOrderStatusEnum implements ArrayValuable<Integer> {

    WAITING(0, "未支付"),
    SUCCESS(10, "支付成功"),
    REFUND(20, "已退款"),
    CLOSED(30, "支付关闭"),
    ;

    private final Integer status;

    private final String name;


    @Override
    public Integer[] array() {
        return new Integer[0];
    }

    /**
     * 判断是否支付成功
     * @param status 状态
     * @return 是否支付成功
     */
    public static boolean isSuccess(Integer status){
        return Objects.equals(status, SUCCESS.status);
    }

    /**
     * 判断是否已退款
     * @param status 状态
     * @return 是否已退款
     */
    public static boolean isRefund(Integer status){
        return Objects.equals(status, REFUND.status);
    }

    /**
     * 判断是否支付成功或已退款
     * @param status 状态
     * @return 是否支付成功或已退款
     */
    public static boolean isSuccessOrRefund(Integer status){
        return ObjectUtils.equalsAny(status, SUCCESS.status, REFUND.status);
    }

    /**
     * 判断是否支付关闭
     * @param status 状态
     * @return 是否支付关闭
     */
    public static boolean isClosed(Integer status){
        return Objects.equals(status, CLOSED.status);
    }
}
