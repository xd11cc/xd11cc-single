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
public enum SystemErrorEnum implements ErrorCode {
    BAD_REQUEST(400, "请求参数有误"),
    UNAUTHORIZED(401, "未授权"),
    METHOD_NOT_ALLOWED(405, "请求方法不正确"),
    SYSTEM_ERROR(500, "系统异常，请联系管理员处理！"),
    USER_NOT_FOUND(1001001, "用户不存在！"),
    PASSWORD_ERROR(1001002, "密码错误！"),
    ILLEGAL_VISIT(1001003, "非法访问！"),
    USER_FORBIDDEN(1001004, "账户已禁用！"),
    USER_LOCKED(1001005, "密码错误超过{}次，账户已锁定！"),
    CHOOSE_RIGHT_DOMAIN(1001006, "请使用正确的域名访问！"),
    DICT_TYPE_HAVE_DATA(1001007, "{}字典类型下存在字典数据！"),
    ;

    private int errorCode;

    private String errorMsg;
}
