package com.xd11cc.single.enums;

import com.xd11cc.single.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @Author: xd11cc
 * @Date: 2025/6/15 10:41
 * 一共 7 位，分成三段
 * 第一段，1 位，类型
 *      1 - 业务级别异常
 *      x - 预留
 * 第二段，3 位，模块
 *      001 - 系统模块
 * 第四段，3 位，错误码
 *       不限制规则。
 *       一般建议，每个服务自增。
 **/
@NoArgsConstructor
@AllArgsConstructor
@Getter
public enum SingleErrorEnum implements ErrorCode {
    UNAUTHORIZED(401, "未授权"),
    USER_NOT_FOUND(1001001, "用户不存在！")
    ;

    private int errorCode;

    private String errorMsg;
}
