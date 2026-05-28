package com.xd11cc.single.enums;

import com.xd11cc.single.config.exception.ErrorCode;
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
    SYSTEM_ERROR(500, "系统异常，请联系管理员处理"),
    USER_NOT_FOUND(1001001, "用户不存在"),
    PASSWORD_ERROR(1001002, "密码错误"),
    ILLEGAL_VISIT(1001003, "非法访问"),
    USER_FORBIDDEN(1001004, "账户已禁用"),
    USER_LOCKED(1001005, "密码错误超过{}次，账户已锁定"),
    CHOOSE_RIGHT_DOMAIN(1001006, "请使用正确的域名访问"),
    DICT_TYPE_HAVE_DATA(1001007, "{}字典类型下存在字典数据"),
    DICT_TYPE_EXISTS(1001008, "字典类型已存在"),
    DICT_DATA_EXISTS(1001009, "字典数据已存在"),
    DICT_TYPE_NOT_EXISTS(1001010, "字典类型不存在"),
    MENU_HAVE_CHILDREN(1001011, "当前目录或菜单下存在子集"),
    CAPTCHA_ERROR(1001012, "验证码错误"),
    CAPTCHA_EXPIRE(1001013, "验证码已过期"),
    PDF_TO_WORD_ERROR(1001014, "pdf转word失败"),
    TEMPLATE_LOADING_ERROR(1001015, "模版加载失败"),
    GENERATE_CODE_ERROR(1001016, "代码生成失败"),
    INIT_MINIO_ERROR(1001017, "初始化Minio失败"),
    MINIO_UPLOAD_FILE(1001018, "文件上传失败"),
    MINIO_GET_FILE_FILE(1001019, "获取预览文件失败"),
    MINIO_DELETE_FILE_FILE(1001020, "删除文件失败"),
    MINIO_DOWNLOAD_FILE_FILE(1001021, "文件下载失败"),
    AUTH_SOURCE_FORBIDDEN(1001022, "{}授权方式以被禁用"),
    NOT_FOUND_TENANT(1001023, "不存在租户编号"),
    SOCIAL_USER_BINDEDE(1001024, "该社交账号已绑定其他用户"),
    SOCIAL_USER_NOT_FOUND(1001025, "社交用户信息不存在"),
    SOCIAL_AUTH_NOT_SUPPORT(1001025, "社交登录不支持"),
    CONFIG_KEY_EXISTS(1001026, "配置键已存在"),
    USERNAME_EXISTS(1001027, "用户名已存在"),
    ADMIN_NOT_ALLOW_DELETE(1001028, "管理员账号不允许删除"),
    DEPT_CODE_EXISTS(1001029, "部门编码已存在"),
    DEPT_HAVE_CHILDREN(1001030, "当前部门下存在子部门"),
    DEPT_PARENT_CANNOT_SELF(1001031, "父部门不能为自身"),
    ROLE_CODE_EXISTS(1001032, "角色编码已存在"),
    ROLE_BINDEDE_USER(1001033, "角色已分配用户，不允许删除"),
    POST_CODE_EXISTS(1001034, "岗位编码已存在"),
    AUTH_SOURCE_EXISTS(1001035, "该应用类型已存在"),
    OLD_PASSWORD_ERROR(1001036, "旧密码错误"),
    TENANT_DOMAIN_EXISTS(1001037, "租户域名已存在"),

    ;

    private int errorCode;

    private String errorMsg;
}
